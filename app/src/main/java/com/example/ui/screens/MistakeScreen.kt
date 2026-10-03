package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MistakeItem
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun MistakeScreen(viewModel: NeetViewModel) {
    val mistakes by viewModel.mistakes.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMistake by remember { mutableStateOf<MistakeItem?>(null) }
    var filterType by remember { mutableStateOf("ALL") }

    val mistakeTypes = listOf("Conceptual", "Calculation", "Silly Mistake", "Memory", "Question Reading")

    val filteredMistakes = remember(mistakes, filterType) {
        if (filterType == "ALL") mistakes else mistakes.filter { it.mistakeType.equals(filterType, ignoreCase = true) }
    }

    val unresolvedCount = mistakes.count { it.status == "UNRESOLVED" }

    Scaffold(
        containerColor = MedicalDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CrimsonError,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_mistake_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Mistake")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mistake_header_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEET MISTAKE NOTEBOOK",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = CrimsonError
                        )
                        Text(
                            text = "Analyze conceptual & calculation flaws",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "$unresolvedCount Unresolved",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (unresolvedCount > 0) CrimsonError else EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips by Mistake Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ALL", "Conceptual", "Calculation", "Silly", "Memory").forEach { type ->
                    val isSelected = filterType.startsWith(type, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterType = if (type == "Silly") "Silly Mistake" else type },
                        label = { Text(type, fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_mistake_${type.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredMistakes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mistakes logged yet. Tap + to record question errors.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMistakes, key = { it.id }) { item ->
                        MistakeCard(
                            item = item,
                            onToggleStatus = {
                                val nextStatus = when (item.status) {
                                    "UNRESOLVED" -> "REVIEWED"
                                    "REVIEWED" -> "MASTERED"
                                    else -> "UNRESOLVED"
                                }
                                viewModel.updateMistake(item.copy(status = nextStatus))
                            },
                            onEdit = { editingMistake = item },
                            onDelete = { viewModel.deleteMistake(item) }
                        )
                    }
                }
            }
        }
    }

    // Add Mistake Dialog
    if (showAddDialog) {
        var subject by remember { mutableStateOf("Physics") }
        var chapter by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("") }
        var questionSummary by remember { mutableStateOf("") }
        var mistakeType by remember { mutableStateOf("Conceptual") }
        var correctConcept by remember { mutableStateOf("") }
        val subjects = listOf("Physics", "Chemistry", "Biology")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Question Mistake", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Subject:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = questionSummary,
                        onValueChange = { questionSummary = it },
                        label = { Text("Question Summary / Problem") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Mistake Type:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mistakeTypes.take(3).forEach { t ->
                            FilterChip(
                                selected = mistakeType == t,
                                onClick = { mistakeType = t },
                                label = { Text(t, fontSize = 9.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mistakeTypes.drop(3).forEach { t ->
                            FilterChip(
                                selected = mistakeType == t,
                                onClick = { mistakeType = t },
                                label = { Text(t, fontSize = 9.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = correctConcept,
                        onValueChange = { correctConcept = it },
                        label = { Text("Correct Concept / Learning Point") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (questionSummary.isNotBlank()) {
                            viewModel.addMistake(
                                subject = subject,
                                chapter = chapter.ifBlank { "General $subject" },
                                topic = topic,
                                questionSummary = questionSummary,
                                mistakeType = mistakeType,
                                correctConcept = correctConcept
                            )
                        }
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Edit Mistake Dialog
    editingMistake?.let { item ->
        var questionSummary by remember { mutableStateOf(item.questionSummary) }
        var correctConcept by remember { mutableStateOf(item.correctConcept) }
        var mistakeType by remember { mutableStateOf(item.mistakeType) }

        AlertDialog(
            onDismissRequest = { editingMistake = null },
            title = { Text("Edit Mistake") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = questionSummary,
                        onValueChange = { questionSummary = it },
                        label = { Text("Question Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = correctConcept,
                        onValueChange = { correctConcept = it },
                        label = { Text("Correct Concept") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMistake(
                            item.copy(
                                questionSummary = questionSummary,
                                correctConcept = correctConcept,
                                mistakeType = mistakeType
                            )
                        )
                        editingMistake = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMistake = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MistakeCard(
    item: MistakeItem,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (item.status) {
        "MASTERED" -> EmeraldSuccess
        "REVIEWED" -> AmberWarning
        else -> CrimsonError
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mistake_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubjectBadge(subject = item.subject)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.mistakeType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberWarning
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.testTag("toggle_status_btn_${item.id}")
                ) {
                    Text(
                        text = item.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.chapter} • ${item.date}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.questionSummary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (item.correctConcept.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MedicalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Concept: ${item.correctConcept}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccentGlow,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
