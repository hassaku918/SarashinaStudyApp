package com.sarashina.study.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sarashina.study.data.db.StudyNoteEntity
import com.sarashina.study.util.UiStrings

@Composable
fun NotesScreen(
    notes: List<StudyNoteEntity>,
    strings: UiStrings,
    onSave: (id: Long, title: String, body: String, enabled: Boolean) -> Unit,
    onDelete: (StudyNoteEntity) -> Unit,
    onToggle: (StudyNoteEntity) -> Unit
) {
    var editing by remember { mutableStateOf<StudyNoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(strings.notes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            strings.notesHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.enabled)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = {
                        editing = note
                        showEditor = true
                    }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(note.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                note.body.take(100),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                        Switch(checked = note.enabled, onCheckedChange = { onToggle(note) })
                        IconButton(onClick = { onDelete(note) }) {
                            Icon(Icons.Default.Delete, contentDescription = strings.delete)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editing = null
                showEditor = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.newNote)
        }
    }

    if (showEditor) {
        var title by remember(editing) { mutableStateOf(editing?.title.orEmpty()) }
        var body by remember(editing) { mutableStateOf(editing?.body.orEmpty()) }
        var enabled by remember(editing) { mutableStateOf(editing?.enabled ?: true) }
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editing == null) strings.newNote else strings.notes) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.noteTitle) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(strings.noteBody) },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        maxLines = 12
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ON", modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSave(editing?.id ?: 0L, title, body, enabled)
                    showEditor = false
                }) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text(strings.cancel) }
            }
        )
    }
}
