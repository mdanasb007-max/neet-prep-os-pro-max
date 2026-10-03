package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun AnalyticsScreen(viewModel: NeetViewModel) {
    val todayStudyMinutes by viewModel.todayStudyMinutes.collectAsState()
    val todayPomodoros by viewModel.todayPomodoros.collectAsState()
    val todayAttempted by viewModel.todayAttempted.collectAsState()
    val todayCorrect by viewModel.todayCorrect.collectAsState()

    val routineTasks by viewModel.routineTasks.collectAsState()
    val syllabusProgress by viewModel.syllabusProgress.collectAsState()
    val dueRevisions by viewModel.dueRevisions.collectAsState()
    val completedRevisions by viewModel.completedRevisions.collectAsState()
    val mistakes by viewModel.mistakes.collectAsState()

    val pastSessions by viewModel.pastSessions.collectAsState()
    val last7DaysDates = viewModel.last7DaysDates

    // Compute 7-day study minutes per date
    val dayMinutesMap = remember(pastSessions, last7DaysDates) {
        val map = mutableMapOf<String, Int>()
        last7DaysDates.forEach { map[it] = 0 }
        pastSessions.filter { it.sessionType == "STUDY" }.forEach { s ->
            val curr = map[s.date] ?: 0
            map[s.date] = curr + s.durationMinutes
        }
        map
    }

    val total7DayMinutes = dayMinutesMap.values.sum()
    val maxDailyMinutes = dayMinutesMap.values.maxOrNull()?.coerceAtLeast(60) ?: 60

    val completedRoutines = routineTasks.count { it.isCompleted }
    val totalRoutines = routineTasks.size.coerceAtLeast(10)

    val accuracy = if (todayAttempted > 0) (todayCorrect.toFloat() / todayAttempted.toFloat()) * 100f else 0f

    val hasAnyData = todayStudyMinutes > 0 || todayAttempted > 0 || completedRoutines > 0 ||
            syllabusProgress.first > 0 || mistakes.isNotEmpty() || total7DayMinutes > 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "NEET PERFORMANCE ANALYTICS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = CyanAccentGlow,
                letterSpacing = 1.sp
            )
            Text(
                text = "Calculated strictly from Room database logs",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        if (!hasAnyData) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_empty_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // ================= 7-DAY STUDY CHART =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("seven_day_chart_card"),
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
                        Text(
                            text = "7-DAY STUDY TIME",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${total7DayMinutes / 60}h ${total7DayMinutes % 60}m total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccentGlow
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simple Native Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        last7DaysDates.forEach { date ->
                            val mins = dayMinutesMap[date] ?: 0
                            val heightFraction = if (maxDailyMinutes > 0) (mins.toFloat() / maxDailyMinutes.toFloat()).coerceIn(0.05f, 1f) else 0.05f
                            val dayLabel = date.takeLast(5) // MM-DD

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (mins > 0) {
                                    Text(
                                        text = "${mins}m",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccentGlow
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (mins > 0) CyanAccentGlow else MedicalSurfaceVariant)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = dayLabel,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= METRICS GRID =================
        item {
            Text(
                text = "KEY METRICS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Today's Study",
                    value = "${todayStudyMinutes / 60}h ${todayStudyMinutes % 60}m",
                    subtitle = "$todayPomodoros Pomodoros",
                    accentColor = CyanAccentGlow,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Accuracy",
                    value = if (todayAttempted > 0) String.format("%.1f%%", accuracy) else "--",
                    subtitle = "$todayAttempted Qs Attempted",
                    accentColor = if (todayAttempted == 0) TextMuted else if (accuracy >= 80f) EmeraldSuccess else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Routine Blocks",
                    value = "$completedRoutines / $totalRoutines",
                    subtitle = "${(completedRoutines * 100) / totalRoutines}% Completed",
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Syllabus Covered",
                    value = "${syllabusProgress.first} Topics",
                    subtitle = if (syllabusProgress.second > 0) "${(syllabusProgress.first * 100) / syllabusProgress.second}% of total" else "0%",
                    accentColor = VioletRoyal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ================= MISTAKE BREAKDOWN =================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mistake_analytics_card"),
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
                        text = "MISTAKES BREAKDOWN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (mistakes.isEmpty()) {
                        Text(
                            text = "No recorded mistakes yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        val types = listOf("Conceptual", "Calculation", "Silly Mistake", "Memory", "Question Reading")
                        types.forEach { type ->
                            val count = mistakes.count { it.mistakeType.equals(type, ignoreCase = true) }
                            val frac = if (mistakes.isNotEmpty()) count.toFloat() / mistakes.size else 0f

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = type, fontSize = 12.sp, color = TextPrimary)
                                    Text(text = "$count (${(frac * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonError)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { frac },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = CrimsonError,
                                    trackColor = MedicalBorder
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
