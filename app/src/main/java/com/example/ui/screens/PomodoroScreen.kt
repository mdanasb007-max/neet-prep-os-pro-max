package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel
import com.example.ui.viewmodel.TimerMode

@Composable
fun PomodoroScreen(viewModel: NeetViewModel) {
    val timerMode by viewModel.timerMode.collectAsState()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
    val timerIsRunning by viewModel.timerIsRunning.collectAsState()
    val timerSessionsCount by viewModel.timerSessionsCount.collectAsState()

    val timerSubject by viewModel.timerSubject.collectAsState()
    val timerChapter by viewModel.timerChapter.collectAsState()
    val timerTopic by viewModel.timerTopic.collectAsState()

    var showCustomDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    // Time calculations
    val minutes = timerSecondsRemaining / 60
    val seconds = timerSecondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = if (timerTotalSeconds > 0) {
        timerSecondsRemaining.toFloat() / timerTotalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")

    val modeColor = when (timerMode) {
        TimerMode.STUDY -> CyanAccentGlow
        TimerMode.SHORT_BREAK -> EmeraldSuccess
        TimerMode.LONG_BREAK -> AmberWarning
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TimerMode.values().forEach { mode ->
                FilterChip(
                    selected = timerMode == mode,
                    onClick = {
                        val defaultMins = when (mode) {
                            TimerMode.STUDY -> 25
                            TimerMode.SHORT_BREAK -> 5
                            TimerMode.LONG_BREAK -> 20
                        }
                        viewModel.setTimerPreset(defaultMins, mode)
                    },
                    label = {
                        Text(
                            text = mode.label,
                            fontWeight = if (timerMode == mode) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = modeColor.copy(alpha = 0.2f),
                        selectedLabelColor = modeColor
                    ),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag("timer_mode_chip_${mode.name.lowercase()}")
                )
            }
        }

        // Subject & Topic Tag Pill
        Surface(
            color = MedicalSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("timer_tag_surface")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOCUSING ON: $timerSubject",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccentGlow
                    )
                    Text(
                        text = if (timerChapter.isNotEmpty()) "$timerChapter • $timerTopic" else "General Prep / Practice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = { showTagDialog = true },
                    modifier = Modifier.testTag("edit_timer_tag_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Tags",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Circular Countdown Clock
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("circular_timer_container"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track
                drawCircle(
                    color = MedicalSurfaceVariant,
                    radius = size.minDimension / 2 - 12.dp.toPx(),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress Arc
                drawArc(
                    color = modeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (timerMode == TimerMode.STUDY) "STUDY FOCUS" else "BREAK TIME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cycle: #${timerSessionsCount + 1}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control Buttons: START/PAUSE/RESUME, RESET, SKIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MedicalSurface)
                        .testTag("timer_reset_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset Timer",
                        tint = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("RESET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }

            // Play / Pause / Resume
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        if (timerIsRunning) viewModel.pauseTimer() else viewModel.startTimer()
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .testTag("timer_play_pause_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (timerIsRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerIsRunning) "Pause" else "Start",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (timerIsRunning) "PAUSE" else if (timerSecondsRemaining < timerTotalSeconds) "RESUME" else "START",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = modeColor
                )
            }

            // Skip
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.skipTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MedicalSurface)
                        .testTag("timer_skip_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Session",
                        tint = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("SKIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Complete Session Action Button
        Button(
            onClick = { viewModel.completeCurrentSession() },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("timer_complete_session_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (timerMode == TimerMode.STUDY) "COMPLETE STUDY SESSION" else "COMPLETE BREAK",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Duration Presets (25 min, 50 min, 90 min, Custom)
        Text(
            text = "STUDY PRESETS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(25, 50, 90).forEach { mins ->
                OutlinedButton(
                    onClick = { viewModel.setTimerPreset(mins, TimerMode.STUDY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preset_${mins}m_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (timerTotalSeconds == mins * 60 && timerMode == TimerMode.STUDY) {
                            CyanAccentGlow.copy(alpha = 0.2f)
                        } else MedicalSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (timerTotalSeconds == mins * 60 && timerMode == TimerMode.STUDY) CyanAccentGlow else MedicalBorder
                        )
                    )
                ) {
                    Text(
                        text = "${mins}m",
                        color = if (timerTotalSeconds == mins * 60 && timerMode == TimerMode.STUDY) CyanAccentGlow else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = { showCustomDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("preset_custom_btn"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MedicalSurface),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MedicalBorder)
                )
            ) {
                Text("Custom", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Info notice: Only study time counts towards "Aaj itna padha"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CyanAccentGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Only completed STUDY sessions count toward 'Aaj itna padha'. Breaks are excluded.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }

    // Custom Duration Dialog
    if (showCustomDialog) {
        var customMinsText by remember { mutableStateOf("45") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Study Duration") },
            text = {
                OutlinedTextField(
                    value = customMinsText,
                    onValueChange = { customMinsText = it },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMinsText.toIntOrNull() ?: 25
                        if (mins > 0) {
                            viewModel.setTimerPreset(mins, TimerMode.STUDY)
                        }
                        showCustomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow)
                ) {
                    Text("Set", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Tag Editor Dialog (Subject, Chapter, Topic)
    if (showTagDialog) {
        var subject by remember { mutableStateOf(timerSubject) }
        var chapter by remember { mutableStateOf(timerChapter) }
        var topic by remember { mutableStateOf(timerTopic) }
        val subjects = listOf("Physics", "Chemistry", "Biology")

        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Tag Study Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Subject:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
                        label = { Text("Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setTimerTags(subject, chapter, topic)
                        showTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccentGlow)
                ) {
                    Text("Save Tags", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
