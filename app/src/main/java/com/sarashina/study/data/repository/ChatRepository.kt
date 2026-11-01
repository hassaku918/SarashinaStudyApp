package com.sarashina.study.data.repository

import android.content.Context
import com.sarashina.study.data.api.ChatCompletionRequest
import com.sarashina.study.data.api.Message
import com.sarashina.study.data.api.SarashinaClient
import com.sarashina.study.data.db.AppDatabase
import com.sarashina.study.data.db.ChatMessageEntity
import com.sarashina.study.data.db.DocumentEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).chatDao()
    private val api = SarashinaClient.api

    fun messagesFlow(sessionId: String = "default"): Flow<List<ChatMessageEntity>> =
        dao.getMessages(sessionId)

    suspend fun sendMessage(
        userContent: String,
        apiKey: String,
        systemPrompt: String,
        reasoningEffort: String = "high",
        sessionId: String = "default"
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

        val apiMessages = mutableListOf<Message>()
        apiMessages.add(Message(role = "system", content = systemPrompt))
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
                val assistantContent = choice?.message?.content ?: "（応答が空でした）"
                val reasoning = choice?.message?.reasoning
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
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    fun documentsFlow(): Flow<List<DocumentEntity>> = dao.getDocuments()

    companion object {
        const val MAX_HISTORY_MESSAGES = 20
        const val MAX_USER_CHARS = 12_000
    }
}
