package com.sarashina.study.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sarashina.study.ui.theme.ThemeMode
import com.sarashina.study.util.AppLang
import com.sarashina.study.util.UiStrings

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
    currentUiLang: AppLang,
    currentWebSearchDefault: Boolean,
    strings: UiStrings,
    lockState: LockSettingsState,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveEffort: (String) -> Unit,
    onSaveThemeMode: (ThemeMode) -> Unit,
    onSaveFontScale: (String) -> Unit,
    onSaveSystemPrompt: (String) -> Unit,
    onSaveAutoBiometric: (Boolean) -> Unit,
    onSaveUiLang: (AppLang) -> Unit,
    onSaveWebSearchDefault: (Boolean) -> Unit,
    onSaveLock: (enabled: Boolean, useBiometric: Boolean, newPin: String?) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var effort by remember { mutableStateOf(currentEffort) }
    var themeMode by remember { mutableStateOf(currentThemeMode) }
    var fontScale by remember { mutableStateOf(currentFontScale) }
    var systemPrompt by remember { mutableStateOf(currentSystemPrompt) }
    var autoBiometric by remember { mutableStateOf(currentAutoBiometric) }
    var uiLang by remember { mutableStateOf(currentUiLang) }
    var webDefault by remember { mutableStateOf(currentWebSearchDefault) }
    var lockEnabled by remember { mutableStateOf(lockState.enabled) }
    var useBiometric by remember { mutableStateOf(lockState.useBiometric) }
    var newPin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPromptEditor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.settings) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(strings.language, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                AppLang.entries.chunked(3).forEach { row ->
                    Row {
                        row.forEach { lang ->
                            FilterChip(
                                selected = uiLang == lang,
                                onClick = { uiLang = lang },
                                label = { Text(lang.label) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.theme, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row {
                    listOf(ThemeMode.SYSTEM to "SYS", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (m, l) ->
                        FilterChip(selected = themeMode == m, onClick = { themeMode = m }, label = { Text(l) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.fontSize, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row {
                    listOf("small" to "S", "medium" to "M", "large" to "L").forEach { (id, l) ->
                        FilterChip(selected = fontScale == id, onClick = { fontScale = id }, label = { Text(l) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.apiKey, style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), placeholder = { Text("sk-...") })
                Spacer(Modifier.height(12.dp))
                Text(strings.reasoning, style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf("low", "medium", "high").forEach { level ->
                        FilterChip(selected = effort == level, onClick = { effort = level }, label = { Text(level) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.webSearch, modifier = Modifier.weight(1f))
                    Switch(checked = webDefault, onCheckedChange = { webDefault = it })
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.systemPrompt, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { showPromptEditor = !showPromptEditor }) { Text(if (showPromptEditor) "▲" else "▼") }
                if (showPromptEditor) {
                    OutlinedTextField(value = systemPrompt, onValueChange = { systemPrompt = it }, modifier = Modifier.fillMaxWidth().height(140.dp), maxLines = 10)
                }
                Spacer(Modifier.height(12.dp))
                Text(strings.appLock, style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.appLock, modifier = Modifier.weight(1f))
                    Switch(checked = lockEnabled, onCheckedChange = { lockEnabled = it })
                }
                if (lockEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Auto biometric", modifier = Modifier.weight(1f))
                        Switch(checked = autoBiometric, onCheckedChange = { autoBiometric = it })
                    }
                    if (lockState.canBiometric) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Biometric", modifier = Modifier.weight(1f))
                            Switch(checked = useBiometric, onCheckedChange = { useBiometric = it })
                        }
                    }
                    OutlinedTextField(value = newPin, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPin = it }, label = { Text("PIN") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pinConfirm, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pinConfirm = it }, label = { Text("PIN confirm") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (pinError != null) Text(pinError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (lockEnabled && newPin.isNotEmpty()) {
                    if (newPin.length !in 4..8) { pinError = "PIN 4-8"; return@TextButton }
                    if (newPin != pinConfirm) { pinError = "mismatch"; return@TextButton }
                }
                if (lockEnabled && !lockState.hasPin && newPin.isEmpty() && !(useBiometric && lockState.canBiometric)) {
                    pinError = "PIN or biometric required"; return@TextButton
                }
                onSaveApiKey(apiKey); onSaveEffort(effort); onSaveThemeMode(themeMode)
                onSaveFontScale(fontScale); onSaveSystemPrompt(systemPrompt)
                onSaveAutoBiometric(autoBiometric); onSaveUiLang(uiLang); onSaveWebSearchDefault(webDefault)
                onSaveLock(lockEnabled, useBiometric, newPin.ifBlank { null })
                onDismiss()
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
