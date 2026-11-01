package com.sarashina.study.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
