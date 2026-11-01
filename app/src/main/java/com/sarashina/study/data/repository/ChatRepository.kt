package com.sarashina.study.data.repository

import android.content.Context
import com.sarashina.study.data.api.ChatCompletionRequest
import com.sarashina.study.data.api.Message
import com.sarashina.study.data.api.SarashinaClient
import com.sarashina.study.data.db.AppDatabase
import com.sarashina.study.data.db.ChatMessageEntity
import com.sarashina.study.data.db.DocumentEntity
import com.sarashina.study.data.db.StudyNoteEntity
import com.sarashina.study.util.ApiErrorMapper
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).chatDao()
    private val api = SarashinaClient.api

    fun messagesFlow(sessionId: String = "default"): Flow<List<ChatMessageEntity>> =
        dao.getMessages(sessionId)

    fun notesFlow(): Flow<List<StudyNoteEntity>> = dao.getNotes()

    suspend fun sendMessage(
        userContent: String,
        apiKey: String,
        systemPrompt: String,
        reasoningEffort: String = "high",
        sessionId: String = "default",
        extraContext: String? = null
    ): Result<String> {
        val trimmed = userContent.take(MAX_USER_CHARS)
        dao.insertMessage(
            ChatMessageEntity(
                role = "user",
                content = trimmed,
                sessionId = sessionId
            )
        )

        val history = dao.getMessagesOnce(sessionId)
        val recent = if (history.size > MAX_HISTORY_MESSAGES) {
            history.takeLast(MAX_HISTORY_MESSAGES)
        } else {
            history
        }

        val notesBlock = buildNotesBlock()
        val fullSystem = buildString {
            append(systemPrompt)
            if (notesBlock.isNotBlank()) {
                append("\n\n")
                append(notesBlock)
            }
            if (!extraContext.isNullOrBlank()) {
                append("\n\n")
                append(extraContext)
            }
        }

        val apiMessages = mutableListOf<Message>()
        apiMessages.add(Message(role = "system", content = fullSystem))
        recent.forEach { entity ->
            if (entity.role == "system") return@forEach
            apiMessages.add(
                Message(
                    role = entity.role,
                    content = entity.content.take(MAX_USER_CHARS),
                    reasoning = entity.reasoning
                )
            )
        }

        val request = ChatCompletionRequest(
            model = "sarashina3.2-mini-thinking",
            messages = apiMessages,
            maxCompletionTokens = 4000,
            reasoningEffort = reasoningEffort
        )

        return try {
            val response = api.chatCompletions(
                authorization = "Bearer $apiKey",
                request = request
            )
            if (response.isSuccessful) {
                val body = response.body()
                val choice = body?.choices?.firstOrNull()
                val assistantContent = choice?.message?.content
                if (assistantContent.isNullOrBlank()) {
                    val reason = choice?.finishReason ?: "unknown"
                    return Result.failure(
                        Exception("応答が空でした（finish_reason=$reason）。トークン上限やモデル側の中断の可能性があります。")
                    )
                }
                val reasoning = choice.message?.reasoning
                dao.insertMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = assistantContent,
                        reasoning = reasoning,
                        sessionId = sessionId
                    )
                )
                Result.success(assistantContent)
            } else {
                val errorBody = response.errorBody()?.string() ?: "（本文なし）"
                Result.failure(Exception("API Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorMapper.toUserMessage(e), e))
        }
    }

    private suspend fun buildNotesBlock(): String {
        val notes = dao.getEnabledNotes()
        if (notes.isEmpty()) return ""
        return buildString {
            appendLine("【学習メモ（ユーザーが管理する常時参照メモ）】")
            appendLine("以下のメモを必ず尊重して解説・解答してください。")
            notes.forEachIndexed { i, n ->
                appendLine("--- メモ${i + 1}: ${n.title} ---")
                appendLine(n.body.take(3000))
            }
        }
    }

    suspend fun clearHistory(sessionId: String = "default") {
        dao.clearSession(sessionId)
    }

    suspend fun saveDocument(title: String, text: String, sourceType: String) {
        dao.insertDocument(
            DocumentEntity(
                title = title,
                extractedText = text.take(MAX_USER_CHARS),
                sourceType = sourceType
            )
        )
    }

    suspend fun upsertNote(note: StudyNoteEntity): Long {
        return if (note.id == 0L) dao.insertNote(note)
        else {
            dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun deleteNote(note: StudyNoteEntity) {
        dao.deleteNote(note)
    }

    fun documentsFlow(): Flow<List<DocumentEntity>> = dao.getDocuments()

    companion object {
        const val MAX_HISTORY_MESSAGES = 20
        const val MAX_USER_CHARS = 12_000
    }
}
