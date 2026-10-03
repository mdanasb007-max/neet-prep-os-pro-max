package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.entity.StudySession
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun TrackerScreen(viewModel: NeetViewModel) {
    val todayDate = viewModel.todayDate
    val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsState()
    val todayAttempted by viewModel.todayAttempted.collectAsState()
    val todayCorrect by viewModel.todayCorrect.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()

    // Form fields
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var minutesText by remember { mutableStateOf("45") }
    var questionsText by remember { mutableStateOf("30") }
    var correctText by remember { mutableStateOf("25") }
    var notes by remember { mutableStateOf("") }

    // Accuracy calculations: accuracy = correct / attempted * 100. If attempted = 0, show "--"
    val incorrect = (todayAttempted - todayCorrect).coerceAtLeast(0)
    val accuracy = if (todayAttempted > 0) {
        (todayCorrect.toFloat() / todayAttempted.toFloat()) * 100f
    } else 0f
    val accuracyDisplay = if (todayAttempted > 0) String.format("%.1f%%", accuracy) else "--"

    val subjects = listOf("Physics", "Chemistry", "Biology")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= ACCURACY & METRICS CARD =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accuracy_stats_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "STUDY & ACCURACY SUMMARY (TODAY)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = CyanAccentGlow
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = accuracyDisplay,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (todayAttempted == 0) TextMuted else if (accuracy >= 80f) EmeraldSuccess else if (accuracy >= 60f) AmberWarning else CrimsonError
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Aaj itna padha", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            val h = todayStudyMinutes / 60
                            val m = todayStudyMinutes % 60
                            Text(
                                text = "${h}h ${String.format("%02d", m)}m",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MedicalBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccuracyMiniTile("Attempted", "$todayAttempted", TextPrimary)
                        AccuracyMiniTile("Correct", "$todayCorrect", EmeraldSuccess)
                        AccuracyMiniTile("Incorrect", "$incorrect", CrimsonError)
                    }
                }
            }
        }

        // ================= LOG NEW STUDY SESSION FORM =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_entry_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "LOG MANUAL STUDY SESSION",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Subject Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tracker_subject_${s.lowercase()}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter (e.g. Thermodynamics)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_chapter_input")
                    )

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic (e.g. Carnot Cycle & PYQs)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_topic_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minutesText,
                            onValueChange = { minutesText = it },
                            label = { Text("Study Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tracker_minutes_input")
                        )

                        OutlinedTextField(
                            value = questionsText,
                            onValueChange = { questionsText = it },
                            label = { Text("Questions") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tracker_questions_input")
                        )

                        OutlinedTextField(
                            value = correctText,
                            onValueChange = { correctText = it },
                            label = { Text("Correct") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tracker_correct_input")
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Observations (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_notes_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val mins = minutesText.toIntOrNull() ?: 0
                            val q = questionsText.toIntOrNull() ?: 0
                            val c = correctText.toIntOrNull() ?: 0
                            viewModel.logStudySession(
                                subject = subject,
                                chapter = chapter.ifBlank { "General $subject" },
                                topic = topic.ifBlank { "Practice Session" },
                                durationMinutes = mins,
                                questionsAttempted = q,
                                questionsCorrect = c,
                                date = todayDate,
                                notes = notes
                            )
                            // Clear inputs
                            chapter = ""
                            topic = ""
                            notes = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("tracker_save_btn")
                    ) {
                        Text(
                            text = "SAVE STUDY SESSION",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // ================= RECENT SESSIONS HISTORY =================
        item {
            Text(
                text = "RECENT STUDY SESSIONS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
        }

        if (recentSessions.isEmpty()) {
            item {
                Surface(
                    color = MedicalSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No study sessions recorded yet. Log your first session above or start a Pomodoro!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentSessions) { session ->
                SessionHistoryCard(
                    session = session,
                    onDelete = { viewModel.deleteStudySession(session) }
                )
            }
        }
    }
}

@Composable
private fun AccuracyMiniTile(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun SessionHistoryCard(
    session: StudySession,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectBadge(subject = session.subject)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${session.durationMinutes} mins • ${session.date}",
                        fontSize = 11.sp,
                        color = CyanAccentGlow,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.chapter}: ${session.topic}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (session.notes.isNotEmpty()) {
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_session_btn_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
