package com.sarashina.study.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessages(sessionId: String = "default"): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesOnce(sessionId: String = "default"): List<ChatMessageEntity>

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: String = "default")

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Insert
    suspend fun insertDocument(doc: DocumentEntity): Long

    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM study_notes ORDER BY updatedAt DESC")
    fun getNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE enabled = 1 ORDER BY updatedAt DESC")
    suspend fun getEnabledNotes(): List<StudyNoteEntity>

    @Insert
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)
}
