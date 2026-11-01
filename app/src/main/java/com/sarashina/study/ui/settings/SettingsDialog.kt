package com.sarashina.study.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sarashina.study.ui.theme.ThemeMode

@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentEffort: String,
    currentThemeMode: ThemeMode,
    onDismiss: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveEffort: (String) -> Unit,
    onSaveThemeMode: (ThemeMode) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var effort by remember { mutableStateOf(currentEffort) }
    var themeMode by remember { mutableStateOf(currentThemeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("設定") },
        text = {
            Column {
                Text(
                    "テーマ",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
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

                Spacer(Modifier.height(20.dp))

                Text(
                    "Sarashina API Key",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "https://sarashina.ai でGoogleログイン後、APIキーを発行してください（無償ベータ）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("sk-...") }
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "推論の深さ (reasoning_effort)",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "high = 最も深い思考（数学・難問向け）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveApiKey(apiKey)
                onSaveEffort(effort)
                onSaveThemeMode(themeMode)
                onDismiss()
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
