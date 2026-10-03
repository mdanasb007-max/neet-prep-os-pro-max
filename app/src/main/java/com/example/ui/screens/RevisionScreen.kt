package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RevisionItem
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun RevisionScreen(viewModel: NeetViewModel) {
    val dueRevisions by viewModel.dueRevisions.collectAsState()
    val upcomingRevisions by viewModel.upcomingRevisions.collectAsState()
    val completedRevisions by viewModel.completedRevisions.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Due Today (${dueRevisions.size})",
        "Upcoming (${upcomingRevisions.size})",
        "Mastered (${completedRevisions.size})"
    )

    Scaffold(
        containerColor = MedicalDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanAccentGlow,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_revision_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Revision Topic")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revision_header_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "SPACED REVISION SYSTEM (R1 — R7)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = CyanAccentGlow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R1: 1d • R2: 3d • R3: 7d • R4: 14d • R5: 21d • R6: 30d • R7: 60d",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Revision Category Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MedicalSurface,
                contentColor = CyanAccentGlow
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentList = when (selectedTabIndex) {
                0 -> dueRevisions
                1 -> upcomingRevisions
                else -> completedRevisions
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "No revisions due today! Great pace. 🎯"
                            1 -> "No upcoming revisions scheduled."
                            else -> "No topics completed all 7 revision stages yet."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentList, key = { it.id }) { item ->
                        RevisionCard(
                            item = item,
                            onCompleteStage = { viewModel.completeRevisionStage(item) },
                            onDelete = { viewModel.deleteRevision(item) }
                        )
                    }
                }
            }
        }
    }

    // Add Manual Revision Dialog
    if (showAddDialog) {
        var subject by remember { mutableStateOf("Physics") }
        var chapter by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("") }
        var stage by remember { mutableStateOf(1) }
        val subjects = listOf("Physics", "Chemistry", "Biology")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Spaced Revision", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic to Revise") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Starting Stage: R$stage", style = MaterialTheme.typography.labelSmall, color = CyanAccentGlow)
                    Slider(
                        value = stage.toFloat(),
                        onValueChange = { stage = it.toInt() },
                        valueRange = 1f..7f,
                        steps = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topic.isNotBlank()) {
                            viewModel.addManualRevision(
                                subject = subject,
                                chapter = chapter.ifBlank { "General $subject" },
                                topic = topic,
                                stage = stage
                            )
                        }
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow)
                ) {
                    Text("Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun RevisionCard(
    item: RevisionItem,
    onCompleteStage: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revision_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Badge (R1..R7)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else CyanAccentGlow.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.isCompleted) "✓" else "R${item.currentStage}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (item.isCompleted) EmeraldSuccess else CyanAccentGlow
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectBadge(subject = item.subject)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Due: ${item.dueDate}",
                        fontSize = 11.sp,
                        color = AmberWarning,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.chapter}: ${item.topic}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (item.lastReviewedDate != null) {
                    Text(
                        text = "Last reviewed: ${item.lastReviewedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            if (!item.isCompleted) {
                Button(
                    onClick = onCompleteStage,
                    modifier = Modifier.testTag("complete_revision_btn_${item.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Stage",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.currentStage >= 7) "MASTER" else "R${item.currentStage} DONE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_revision_btn_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
