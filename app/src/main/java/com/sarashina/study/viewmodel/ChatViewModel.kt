package com.sarashina.study.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sarashina.study.data.db.ChatMessageEntity
import com.sarashina.study.data.repository.ChatRepository
import com.sarashina.study.ui.theme.ThemeMode
import com.sarashina.study.util.OcrHelper
import com.sarashina.study.util.PdfHelper
import com.sarashina.study.util.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusMessage: String? = null,
    val apiKey: String = "",
    val reasoningEffort: String = "high",
    val systemPrompt: String = SettingsRepository.DEFAULT_SYSTEM_PROMPT,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ChatRepository(application)
    private val settings = SettingsRepository(application)

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _status = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatUiState> = combine(
        repo.messagesFlow(),
        settings.apiKeyFlow,
        settings.reasoningEffortFlow,
        settings.systemPromptFlow,
        settings.themeModeFlow
    ) { messages, apiKey, effort, prompt, theme ->
        ChatUiState(
            messages = messages,
            isLoading = _isLoading.value,
            error = _error.value,
            statusMessage = _status.value,
            apiKey = apiKey,
            reasoningEffort = effort,
            systemPrompt = prompt,
            themeMode = ThemeMode.fromString(theme)
        )
    }.combine(_isLoading) { state, loading ->
        state.copy(isLoading = loading)
    }.combine(_error) { state, error ->
        state.copy(error = error)
    }.combine(_status) { state, status ->
        state.copy(statusMessage = status)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChatUiState()
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val key = uiState.value.apiKey
        if (key.isBlank()) {
            _error.value = "設定画面でSarashina API Keyを入力してください"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _status.value = "思考中… (sarashina3.2-mini-thinking)"
            val result = repo.sendMessage(
                userContent = text,
                apiKey = key,
                systemPrompt = uiState.value.systemPrompt,
                reasoningEffort = uiState.value.reasoningEffort
            )
            _isLoading.value = false
            _status.value = null
            result.onFailure { e ->
                _error.value = e.message ?: "送信に失敗しました"
            }
        }
    }

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _status.value = "画像をOCR中…"
            _error.value = null
            try {
                val text = OcrHelper.recognizeFromUri(getApplication(), uri)
                if (text.isBlank()) {
                    _error.value = "文字を認識できませんでした"
                } else {
                    repo.saveDocument("スキャン画像", text, "image")
                    sendMessage("以下はスキャンした問題・資料です。内容を整理して解説・解答を教えてください。\n\n$text")
                }
            } catch (e: Exception) {
                _error.value = "OCR失敗: ${e.message}"
            } finally {
                _isLoading.value = false
                _status.value = null
            }
        }
    }

    fun processPdf(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val text = PdfHelper.extractTextFromPdf(
                    context = getApplication(),
                    uri = uri,
                    maxPages = 8
                ) { current, total ->
                    _status.value = "PDF処理中… $current / $total ページ"
                }
                if (text.isBlank()) {
                    _error.value = "PDFから文字を抽出できませんでした"
                } else {
                    repo.saveDocument("PDF資料", text, "pdf")
                    sendMessage("以下はPDFから抽出したテキストです。内容を整理して解説・解答を教えてください。\n\n$text")
                }
            } catch (e: Exception) {
                _error.value = "PDF処理失敗: ${e.message}"
            } finally {
                _isLoading.value = false
                _status.value = null
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repo.clearHistory()
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch { settings.saveApiKey(key.trim()) }
    }

    fun saveReasoningEffort(effort: String) {
        viewModelScope.launch { settings.saveReasoningEffort(effort) }
    }

    fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val value = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            settings.saveThemeMode(value)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
