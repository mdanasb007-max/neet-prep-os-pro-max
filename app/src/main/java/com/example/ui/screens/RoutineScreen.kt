package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RoutineTask
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun RoutineScreen(viewModel: NeetViewModel) {
    val tasks by viewModel.routineTasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var editingTask by remember { mutableStateOf<RoutineTask?>(null) }

    val totalCompletedQuestions = tasks.filter { it.isCompleted }.sumOf { it.targetQuestions }
    val totalTargetQuestions = tasks.sumOf { it.targetQuestions }.coerceAtLeast(390)

    val totalPlannedMinutes = tasks.sumOf { it.plannedMinutes }.coerceAtLeast(510)
    val totalActualMinutes = tasks.sumOf { if (it.isCompleted) it.plannedMinutes else it.actualMinutes }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size.coerceAtLeast(10)

    val plannedHours = totalPlannedMinutes / 60
    val plannedMins = totalPlannedMinutes % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routine_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY ROUTINE BLOCKS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = CyanAccentGlow
                            )
                            Text(
                                text = "Target: 390 Questions • ${plannedHours}h ${plannedMins}m / Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF064E3B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$completedCount / $totalCount DONE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress indicators
                    val progressFloat = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = EmeraldSuccess,
                        trackColor = MedicalBorder
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Questions Done", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                "$totalCompletedQuestions / $totalTargetQuestions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Planned Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                "${totalActualMinutes}m / ${totalPlannedMinutes}m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccentGlow
                            )
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "10 ROUTINE BLOCKS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
        }

        // List of 10 tasks
        items(tasks) { task ->
            RoutineBlockCard(
                task = task,
                onToggle = { viewModel.toggleRoutineTask(task.id, task.isCompleted) },
                onEdit = { editingTask = task }
            )
        }
    }

    // Edit Routine Block Dialog
    editingTask?.let { task ->
        EditRoutineTaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { updated ->
                viewModel.updateRoutineTask(updated)
                editingTask = null
            }
        )
    }
}

@Composable
private fun RoutineBlockCard(
    task: RoutineTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_block_${task.blockOrder}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0F172A) else MedicalSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.isCompleted) EmeraldSuccess.copy(alpha = 0.5f) else MedicalBorder
            )
        )
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
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = EmeraldSuccess,
                        uncheckedColor = TextMuted
                    ),
                    modifier = Modifier.testTag("routine_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${task.blockOrder}. ${task.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) TextSecondary else TextPrimary
                    )

                    if (task.chapter.isNotEmpty()) {
                        Text(
                            text = "TODAY'S TOPIC: ${task.chapter} → ${task.topic}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccentGlow,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_routine_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Block",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MedicalBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Details row: Questions & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TARGET: ",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${task.targetQuestions} Questions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PLANNED: ",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${task.plannedMinutes} mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccentGlow
                    )
                }

                Text(
                    text = if (task.isCompleted) "COMPLETED" else "PENDING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (task.isCompleted) EmeraldSuccess else TextMuted
                )
            }
        }
    }
}

@Composable
private fun EditRoutineTaskDialog(
    task: RoutineTask,
    onDismiss: () -> Unit,
    onSave: (RoutineTask) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var targetQuestions by remember { mutableStateOf(task.targetQuestions.toString()) }
    var plannedMinutes by remember { mutableStateOf(task.plannedMinutes.toString()) }
    var chapter by remember { mutableStateOf(task.chapter) }
    var topic by remember { mutableStateOf(task.topic) }
    var notes by remember { mutableStateOf(task.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Routine Block", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetQuestions,
                        onValueChange = { targetQuestions = it },
                        label = { Text("Questions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = plannedMinutes,
                        onValueChange = { plannedMinutes = it },
                        label = { Text("Planned Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = targetQuestions.toIntOrNull() ?: task.targetQuestions
                    val m = plannedMinutes.toIntOrNull() ?: task.plannedMinutes
                    onSave(
                        task.copy(
                            title = title,
                            targetQuestions = q,
                            plannedMinutes = m,
                            chapter = chapter,
                            topic = topic,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow),
                modifier = Modifier.testTag("save_routine_edit_btn")
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
