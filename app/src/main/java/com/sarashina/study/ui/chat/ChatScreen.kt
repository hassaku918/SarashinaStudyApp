package com.sarashina.study.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.sarashina.study.ui.components.MessageBubble
import com.sarashina.study.ui.notes.NotesScreen
import com.sarashina.study.ui.settings.LockSettingsState
import com.sarashina.study.ui.settings.SettingsDialog
import com.sarashina.study.util.AppLockManager
import com.sarashina.study.util.AppStrings
import com.sarashina.study.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

private enum class MainDest { Chat, Notes }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = AppStrings.of(uiState.uiLang)
    var inputText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var dest by remember { mutableStateOf(MainDest.Chat) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val lockManager = remember { AppLockManager(context) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scale = when (uiState.fontScale) { "small" -> 0.9f; "large" -> 1.2f; else -> 1.0f }
    val density = LocalDensity.current

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.processImage(it) }
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.processPdf(it) }
    }

    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * scale)) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(strings.appTitle, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    NavigationDrawerItem(
                        label = { Text(strings.chat) }, selected = dest == MainDest.Chat,
                        onClick = { dest = MainDest.Chat; scope.launch { drawerState.close() } },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text(strings.notes) }, selected = dest == MainDest.Notes,
                        onClick = { dest = MainDest.Notes; scope.launch { drawerState.close() } },
                        icon = { Icon(Icons.Default.NoteAlt, null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text(strings.settings) }, selected = false,
                        onClick = { showSettings = true; scope.launch { drawerState.close() } },
                        icon = { Icon(Icons.Default.Settings, null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text(strings.clearHistory) }, selected = false,
                        onClick = { showClearConfirm = true; scope.launch { drawerState.close() } },
                        icon = { Icon(Icons.Default.ClearAll, null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "menu")
                            }
                        },
                        title = {
                            Column {
                                Text(strings.appTitle, fontWeight = FontWeight.Bold)
                                Text(strings.appSubtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = strings.settings)
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
                    when (dest) {
                        MainDest.Notes -> NotesScreen(
                            notes = uiState.notes, strings = strings,
                            onSave = viewModel::saveNote, onDelete = viewModel::deleteNote, onToggle = viewModel::toggleNote
                        )
                        MainDest.Chat -> Column(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f)) {
                                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                                    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(strings.emptyTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(strings.emptyBody, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(uiState.messages, key = { it.id }) { MessageBubble(message = it) }
                                    }
                                }
                            }
                            uiState.error?.let { err ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                        Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                        TextButton(onClick = viewModel::clearError) { Text("OK") }
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = uiState.webSearchEnabled,
                                    onClick = { viewModel.setWebSearch(!uiState.webSearchEnabled) },
                                    label = { Text(if (uiState.webSearchEnabled) strings.webSearchOn else strings.webSearch) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(16.dp)) }
                                )
                            }
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Bottom) {
                                IconButton(onClick = { imagePicker.launch("image/*") }, enabled = !uiState.isLoading) {
                                    Icon(Icons.Default.Image, contentDescription = strings.scanImage)
                                }
                                IconButton(onClick = { pdfPicker.launch("application/pdf") }, enabled = !uiState.isLoading) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = strings.scanPdf)
                                }
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { if (it.length <= 8000) inputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text(strings.askPlaceholder) },
                                    maxLines = 5,
                                    shape = RoundedCornerShape(20.dp),
                                    enabled = !uiState.isLoading
                                )
                                Spacer(Modifier.width(8.dp))
                                FloatingActionButton(
                                    onClick = {
                                        if (inputText.isNotBlank() && !uiState.isLoading) {
                                            viewModel.sendMessage(inputText); inputText = ""
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "send") }
                            }
                        }
                    }
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                            Card(Modifier.fillMaxWidth().padding(horizontal = 28.dp), shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(Modifier.size(48.dp), strokeWidth = 4.dp)
                                    Spacer(Modifier.height(20.dp))
                                    Text(strings.responding, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Text(uiState.statusMessage ?: strings.responding, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.respondingHint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(strings.clearConfirmTitle) },
            text = { Text(strings.clearConfirmBody) },
            confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClearConfirm = false }) { Text(strings.delete) } },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text(strings.cancel) } }
        )
    }
    if (showSettings) {
        SettingsDialog(
            currentApiKey = uiState.apiKey,
            currentEffort = uiState.reasoningEffort,
            currentThemeMode = uiState.themeMode,
            currentFontScale = uiState.fontScale,
            currentSystemPrompt = uiState.systemPrompt,
            currentAutoBiometric = uiState.autoBiometric,
            currentUiLang = uiState.uiLang,
            currentWebSearchDefault = uiState.webSearchEnabled,
            strings = strings,
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
            onSaveFontScale = viewModel::saveFontScale,
            onSaveSystemPrompt = viewModel::saveSystemPrompt,
            onSaveAutoBiometric = viewModel::saveAutoBiometric,
            onSaveUiLang = viewModel::saveUiLang,
            onSaveWebSearchDefault = viewModel::saveWebSearchDefault,
            onSaveLock = { enabled, useBio, pin ->
                lockManager.lockEnabled = enabled
                lockManager.useBiometric = useBio
                if (pin != null) try { lockManager.setPin(pin) } catch (_: Exception) {}
            }
        )
    }
}
