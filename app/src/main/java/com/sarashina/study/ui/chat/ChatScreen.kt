package com.sarashina.study.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sarashina.study.ui.components.MessageBubble
import com.sarashina.study.ui.settings.LockSettingsState
import com.sarashina.study.ui.settings.SettingsDialog
import com.sarashina.study.util.AppLockManager
import com.sarashina.study.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val lockManager = remember { AppLockManager(context) }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.processImage(it) } }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.processPdf(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sarashina Study", fontWeight = FontWeight.Bold)
                        Text(
                            "受験勉強AI · 推論モデル",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "履歴クリア")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    EmptyState()
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.messages, key = { it.id }) { msg ->
                            MessageBubble(message = msg)
                        }
                    }
                }

                if (uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.statusMessage ?: "処理中…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "バックグラウンドでも継続します",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.error?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Image, contentDescription = "画像スキャン")
                }
                IconButton(
                    onClick = { pdfPicker.launch("application/pdf") },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 8000) inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("質問を入力…") },
                    maxLines = 5,
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() && !uiState.isLoading) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "送信")
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("履歴を削除") },
            text = { Text("会話履歴をすべて削除します。この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("キャンセル") }
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentApiKey = uiState.apiKey,
            currentEffort = uiState.reasoningEffort,
            currentThemeMode = uiState.themeMode,
            lockState = LockSettingsState(
                enabled = lockManager.lockEnabled,
                useBiometric = lockManager.useBiometric,
                hasPin = lockManager.hasPin(),
                canBiometric = lockManager.canUseBiometric()
            ),
            onDismiss = { showSettings = false },
            onSaveApiKey = viewModel::saveApiKey,
            onSaveEffort = viewModel::saveReasoningEffort,
            onSaveThemeMode = viewModel::saveThemeMode,
            onSaveLock = { enabled, useBio, pin ->
                lockManager.lockEnabled = enabled
                lockManager.useBiometric = useBio
                if (pin != null) {
                    try {
                        lockManager.setPin(pin)
                    } catch (_: Exception) { }
                }
            }
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "問題をスキャンするか質問してみよう",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "画像・PDFのOCR → 推論モデルが解説\n会話は端末に保存されます",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
