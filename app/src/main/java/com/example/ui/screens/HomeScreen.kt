package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyPlanTopic
import com.example.data.entity.RoutineTask
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun HomeScreen(
    viewModel: NeetViewModel,
    onNavigate: (AppTab) -> Unit
) {
    val planTopics by viewModel.todayPlanTopics.collectAsState()
    val yesterdayPendingCount by viewModel.yesterdayPendingCount.collectAsState()
    val routineTasks by viewModel.routineTasks.collectAsState()
    val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsState()
    val todayPomodoros by viewModel.todayPomodoros.collectAsState()
    val todayAttempted by viewModel.todayAttempted.collectAsState()
    val todayCorrect by viewModel.todayCorrect.collectAsState()
    val dueRevisions by viewModel.dueRevisions.collectAsState()

    var topicToLogQuestions by remember { mutableStateOf<DailyPlanTopic?>(null) }

    // Plan Progress
    val totalPlanTopics = planTopics.size
    val completedPlanTopics = planTopics.count { it.status == "COMPLETED" }
    val pendingTopicsCount = planTopics.count { it.status != "COMPLETED" }

    // Format "Aaj itna padha" from Room study sessions
    val hours = todayStudyMinutes / 60
    val minutes = todayStudyMinutes % 60
    val aajItnaPadhaText = "${hours}h ${String.format("%02d", minutes)}m"

    // Accuracy
    val accuracyText = if (todayAttempted > 0) {
        "${((todayCorrect.toFloat() / todayAttempted.toFloat()) * 100).toInt()}%"
    } else "--"

    // Group plan topics by Subject & Class for the structured view
    val groupedTopics = remember(planTopics) {
        planTopics.groupBy { "${it.subject.uppercase()} — CLASS ${it.classLevel}" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= MAIN CARD: TODAY'S PLAN =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_plan_main_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(CyanAccentGlow.copy(alpha = 0.5f), EmeraldSuccess.copy(alpha = 0.5f))
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S PLAN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = CyanAccentGlow,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "OFFICIAL NEET UG 2026 • FIXED & LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F2B48))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$completedPlanTopics / $totalPlanTopics Topics",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccentGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    val progressRatio = if (totalPlanTopics > 0) completedPlanTopics.toFloat() / totalPlanTopics.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldSuccess,
                        trackColor = MedicalBorder
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Grid (Progress, Today's Study, Pomodoros, Questions, Accuracy, Pending)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlanMetricTile("Today's Study", aajItnaPadhaText, CyanAccentGlow)
                        PlanMetricTile("Pomodoros", "$todayPomodoros completed", EmeraldSuccess)
                        PlanMetricTile("Accuracy", accuracyText, if (todayAttempted > 0 && todayCorrect * 10 >= todayAttempted * 8) EmeraldSuccess else AmberWarning)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlanMetricTile("Questions", "$todayAttempted / 390", AmberWarning)
                        PlanMetricTile("Pending", "$pendingTopicsCount Topics", if (pendingTopicsCount > 0) AmberWarning else EmeraldSuccess)
                        PlanMetricTile("Progress", "$completedPlanTopics / $totalPlanTopics Done", VioletRoyal)
                    }
                }
            }
        }

        // ================= YESTERDAY PENDING BANNER =================
        if (yesterdayPendingCount > 0) {
            item {
                Surface(
                    color = Color(0xFF451A03),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AmberWarning, CrimsonError))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yesterday Pending: $yesterdayPendingCount topics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                            Text(
                                text = "Carried forward into today's plan without disrupting new syllabus.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }
            }
        }

        // ================= QUICK ACTIONS =================
        item {
            Text(
                text = "STUDY SYSTEMS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        title = "Pomodoro Timer",
                        subtitle = "25m Study / 5m Break",
                        icon = Icons.Default.Timer,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        testTag = "home_quick_pomodoro"
                    ) { onNavigate(AppTab.POMODORO) }

                    QuickActionTile(
                        title = "Daily Routine",
                        subtitle = "10 Routine Blocks",
                        icon = Icons.Default.Checklist,
                        accentColor = CyanAccentGlow,
                        modifier = Modifier.weight(1f),
                        testTag = "home_quick_routine"
                    ) { onNavigate(AppTab.ROUTINE) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        title = "Revision R1-R7",
                        subtitle = "${dueRevisions.size} Due Today",
                        icon = Icons.Default.Autorenew,
                        accentColor = VioletRoyal,
                        modifier = Modifier.weight(1f),
                        testTag = "home_quick_revision"
                    ) { onNavigate(AppTab.REVISION) }

                    QuickActionTile(
                        title = "Study Tracker",
                        subtitle = "Manual Logs & PYQ",
                        icon = Icons.Default.EditNote,
                        accentColor = AmberWarning,
                        modifier = Modifier.weight(1f),
                        testTag = "home_quick_tracker"
                    ) { onNavigate(AppTab.TRACKER) }
                }
            }
        }

        // ================= TODAY'S MUST DO (FIXED OFFICIAL TOPIC PLAN) =================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S MUST DO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "NMC / NTA NEET UG 2026 Syllabus Progression",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccentGlow
                    )
                }

                TextButton(onClick = { onNavigate(AppTab.SYLLABUS) }) {
                    Text("FULL SYLLABUS", color = CyanAccentGlow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Render each category with its official topics
        groupedTopics.forEach { (groupHeader, topics) ->
            item {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = groupHeader,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanAccentGlow,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            items(topics, key = { it.id }) { topic ->
                FixedPlanTopicCard(
                    topic = topic,
                    onStartPomodoro = { viewModel.startPomodoroForTopic(topic) },
                    onToggleCompleted = { viewModel.togglePlanTopicCompletion(topic) },
                    onOpenQuestionDialog = { topicToLogQuestions = topic }
                )
            }
        }
    }

    // Question Logging Dialog for Topic
    topicToLogQuestions?.let { topic ->
        LogTopicQuestionsDialog(
            topic = topic,
            onDismiss = { topicToLogQuestions = null },
            onSave = { attempted, correct, notes ->
                viewModel.logQuestionsForPlanTopic(topic, attempted, correct, notes)
                topicToLogQuestions = null
            }
        )
    }
}

@Composable
private fun PlanMetricTile(
    label: String,
    value: String,
    accentColor: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = accentColor)
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FixedPlanTopicCard(
    topic: DailyPlanTopic,
    onStartPomodoro: () -> Unit,
    onToggleCompleted: () -> Unit,
    onOpenQuestionDialog: () -> Unit
) {
    val isCompleted = topic.status == "COMPLETED"
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_topic_card_${topic.id}")
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF062820) else MedicalSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCompleted) EmeraldSuccess.copy(alpha = 0.6f) else MedicalBorder
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Subject, Branch, Pending status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubjectBadge(subject = if (topic.branch.isNotEmpty() && topic.branch != "General") "${topic.subject} (${topic.branch})" else topic.subject)
                Spacer(modifier = Modifier.width(8.dp))

                if (topic.isPendingFromYesterday) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF78350F), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("⚠️ YESTERDAY PENDING", fontSize = 9.sp, fontWeight = FontWeight.Black, color = AmberWarning)
                    }
                } else if (isCompleted) {
                    Text("✓ COMPLETED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = EmeraldSuccess)
                } else {
                    Text("⚠️ PENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("plan_topic_expand_btn_${topic.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Open Topic Details",
                        tint = CyanAccentGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter & Topic
            Text(
                text = topic.chapter,
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccentGlow,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = topic.topic,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) TextSecondary else TextPrimary
            )

            if (topic.subtopic.isNotEmpty()) {
                Text(
                    text = topic.subtopic,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (topic.unit.isNotEmpty()) {
                    Text(
                        text = "Official Unit: ${topic.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
                if (topic.routineBlockOrder != null) {
                    Text(
                        text = "Linked to Daily Routine Block #${topic.routineBlockOrder}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MedicalBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Stats row (Study minutes + Questions Attempted & Accuracy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Study: ", fontSize = 11.sp, color = TextMuted)
                    Text("${topic.studyMinutes}m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccentGlow)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Questions: ", fontSize = 11.sp, color = TextMuted)
                    Text(
                        "${topic.questionsAttempted}/${topic.questionsTarget} Qs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning
                    )
                    val accText = if (topic.questionsAttempted > 0) "${topic.accuracy.toInt()}%" else "--"
                    Text(
                        " ($accText)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (topic.questionsAttempted > 0 && topic.accuracy >= 80) EmeraldSuccess else AmberWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: [▶ START POMODORO], [LOG QUESTIONS], [✓ MARK COMPLETED]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pomodoro Button
                OutlinedButton(
                    onClick = onStartPomodoro,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topic_start_pomodoro_${topic.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MedicalSurfaceVariant),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyanAccentGlow)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CyanAccentGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("POMODORO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccentGlow)
                }

                // Log Questions Button
                OutlinedButton(
                    onClick = onOpenQuestionDialog,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topic_log_questions_${topic.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MedicalSurfaceVariant),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AmberWarning)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("QUESTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                }

                // Mark Completed Toggle Button
                Button(
                    onClick = onToggleCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topic_mark_complete_${topic.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else EmeraldSuccess
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isCompleted) EmeraldSuccess else Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "DONE" else "COMPLETE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) EmeraldSuccess else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun LogTopicQuestionsDialog(
    topic: DailyPlanTopic,
    onDismiss: () -> Unit,
    onSave: (attempted: Int, correct: Int, notes: String) -> Unit
) {
    var attemptedText by remember { mutableStateOf(if (topic.questionsAttempted > 0) topic.questionsAttempted.toString() else topic.questionsTarget.toString()) }
    var correctText by remember { mutableStateOf(if (topic.questionsCorrect > 0) topic.questionsCorrect.toString() else "") }
    var notesText by remember { mutableStateOf("") }

    val attempted = attemptedText.toIntOrNull() ?: 0
    val correct = correctText.toIntOrNull() ?: 0
    val incorrect = (attempted - correct).coerceAtLeast(0)
    val acc = if (attempted > 0) ((correct.toFloat() / attempted) * 100).toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Log Topic Questions", fontWeight = FontWeight.Bold)
                Text(topic.topic, fontSize = 12.sp, color = CyanAccentGlow)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Target: ${topic.questionsTarget} Questions", fontSize = 12.sp, color = TextSecondary)

                OutlinedTextField(
                    value = attemptedText,
                    onValueChange = { attemptedText = it },
                    label = { Text("Questions Attempted") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correctText,
                    onValueChange = { correctText = it },
                    label = { Text("Questions Correct") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Calculated stats preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Incorrect: $incorrect", fontSize = 12.sp, color = CrimsonError, fontWeight = FontWeight.Bold)
                    Text("Accuracy: $acc%", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Weakness observation") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(attempted, correct.coerceIn(0, attempted), notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow)
            ) {
                Text("Save Questions", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
