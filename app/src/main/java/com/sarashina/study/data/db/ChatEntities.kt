package com.sarashina.study.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val reasoning: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = "default"
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val extractedText: String,
    val sourceType: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** 学習メモ（AIが毎回参照する指示・暗記事項など） */
@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val enabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
