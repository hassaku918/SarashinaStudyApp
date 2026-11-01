package com.sarashina.study.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sarashina.study.ui.theme.ThemeMode

data class LockSettingsState(
    val enabled: Boolean,
    val useBiometric: Boolean,
    val hasPin: Boolean,
    val canBiometric: Boolean
)

@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentEffort: String,
    currentThemeMode: ThemeMode,
    currentFontScale: String,
    currentSystemPrompt: String,
    currentAutoBiometric: Boolean,
    lockState: LockSettingsState,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveEffort: (String) -> Unit,
    onSaveThemeMode: (ThemeMode) -> Unit,
    onSaveFontScale: (String) -> Unit,
    onSaveSystemPrompt: (String) -> Unit,
    onSaveAutoBiometric: (Boolean) -> Unit,
    onSaveLock: (enabled: Boolean, useBiometric: Boolean, newPin: String?) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var effort by remember { mutableStateOf(currentEffort) }
    var themeMode by remember { mutableStateOf(currentThemeMode) }
    var fontScale by remember { mutableStateOf(currentFontScale) }
    var systemPrompt by remember { mutableStateOf(currentSystemPrompt) }
    var autoBiometric by remember { mutableStateOf(currentAutoBiometric) }
    var lockEnabled by remember { mutableStateOf(lockState.enabled) }
    var useBiometric by remember { mutableStateOf(lockState.useBiometric) }
    var newPin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPromptEditor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("設定") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Section("テーマ")
                Row {
                    listOf(
                        ThemeMode.SYSTEM to "システム",
                        ThemeMode.LIGHT to "ライト",
                        ThemeMode.DARK to "ダーク"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { themeMode = mode },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Section("文字サイズ")
                Row {
                    listOf("small" to "小", "medium" to "中", "large" to "大").forEach { (id, label) ->
                        FilterChip(
                            selected = fontScale == id,
                            onClick = { fontScale = id },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Section("Sarashina API Key")
                Text("暗号化して端末に保存", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("sk-...") }
                )

                Spacer(Modifier.height(16.dp))
                Section("推論の深さ")
                Row {
                    listOf("low", "medium", "high").forEach { level ->
                        FilterChip(
                            selected = effort == level,
                            onClick = { effort = level },
                            label = { Text(level) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Section("システムプロンプト")
                TextButton(onClick = { showPromptEditor = !showPromptEditor }) {
                    Text(if (showPromptEditor) "閉じる" else "編集する")
                }
                if (showPromptEditor) {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 12
                    )
                }

                Spacer(Modifier.height(16.dp))
                Section("アプリロック")
                Text("起動時にすぐ指紋認証を出します（自動）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("ロックを有効にする", modifier = Modifier.weight(1f))
                    Switch(checked = lockEnabled, onCheckedChange = { lockEnabled = it })
                }
                if (lockEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("起動時に自動で生体認証", modifier = Modifier.weight(1f))
                        Switch(checked = autoBiometric, onCheckedChange = { autoBiometric = it })
                    }
                    if (lockState.canBiometric) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("生体認証 / 端末PIN", modifier = Modifier.weight(1f))
                            Switch(checked = useBiometric, onCheckedChange = { useBiometric = it })
                        }
                    }
                    Text(if (lockState.hasPin) "PINを変更（空欄で据え置き）" else "アプリPIN（4〜8桁）", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("新しいPIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pinConfirm = it },
                        label = { Text("PIN確認") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) Text(pinError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (lockEnabled && newPin.isNotEmpty()) {
                    if (newPin.length !in 4..8) { pinError = "PINは4〜8桁"; return@TextButton }
                    if (newPin != pinConfirm) { pinError = "PINが一致しません"; return@TextButton }
                }
                if (lockEnabled && !lockState.hasPin && newPin.isEmpty() && !(useBiometric && lockState.canBiometric)) {
                    pinError = "PINを設定するか、生体認証を有効にしてください"
                    return@TextButton
                }
                onSaveApiKey(apiKey)
                onSaveEffort(effort)
                onSaveThemeMode(themeMode)
                onSaveFontScale(fontScale)
                onSaveSystemPrompt(systemPrompt)
                onSaveAutoBiometric(autoBiometric)
                onSaveLock(lockEnabled, useBiometric, newPin.ifBlank { null })
                onDismiss()
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
}
