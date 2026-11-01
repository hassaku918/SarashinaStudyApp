package com.sarashina.study.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val API_KEY = stringPreferencesKey("sarashina_api_key")
    val REASONING_EFFORT = stringPreferencesKey("reasoning_effort")
    val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    val THEME_MODE = stringPreferencesKey("theme_mode")
}

class SettingsRepository(private val context: Context) {

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.API_KEY] ?: ""
    }

    val reasoningEffortFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.REASONING_EFFORT] ?: "high"
    }

    val systemPromptFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SettingsKeys.THEME_MODE] ?: "system"
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[SettingsKeys.API_KEY] = key }
    }

    suspend fun saveReasoningEffort(effort: String) {
        context.dataStore.edit { it[SettingsKeys.REASONING_EFFORT] = effort }
    }

    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { it[SettingsKeys.SYSTEM_PROMPT] = prompt }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { it[SettingsKeys.THEME_MODE] = mode }
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """
あなたは日本の大学受験（特に難関国立・私立）に詳しい優秀な家庭教師です。
数学・英語・国語・理科・社会など全科目に対応し、段階的でわかりやすい解説を心がけてください。
計算過程や文法根拠も丁寧に示し、最終的に自分で解けるように導いてください。
問題文がスキャンされたものであれば、まず内容を整理してから解説を始めてください。
""".trimIndent()
    }
}
