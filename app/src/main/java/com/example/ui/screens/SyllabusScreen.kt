package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.entity.SyllabusTopic
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NeetViewModel

@Composable
fun SyllabusScreen(
    viewModel: NeetViewModel,
    onNavigate: (AppTab) -> Unit
) {
    val allTopics by viewModel.allSyllabusTopics.collectAsState()
    val syllabusProgress by viewModel.syllabusProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("ALL") }
    var selectedClass by remember { mutableStateOf(0) } // 0 = all, 11 = class 11, 12 = class 12
    var selectedChapter by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }

    val availableChapters = remember(allTopics, selectedSubject, selectedClass) {
        val pool = allTopics.filter { topic ->
            val matchSubject = selectedSubject == "ALL" || topic.subject.equals(selectedSubject, ignoreCase = true)
            val matchClass = selectedClass == 0 || topic.classLevel == selectedClass
            matchSubject && matchClass
        }
        listOf("ALL") + pool.map { it.chapter }.distinct()
    }

    LaunchedEffect(availableChapters) {
        if (selectedChapter !in availableChapters) {
            selectedChapter = "ALL"
        }
    }

    val filteredTopics = remember(allTopics, searchQuery, selectedSubject, selectedClass, selectedChapter, selectedStatus) {
        allTopics.filter { topic ->
            val matchQuery = searchQuery.isBlank() ||
                    topic.chapter.contains(searchQuery, ignoreCase = true) ||
                    topic.topic.contains(searchQuery, ignoreCase = true) ||
                    topic.subtopic.contains(searchQuery, ignoreCase = true)
            val matchSubject = selectedSubject == "ALL" || topic.subject.equals(selectedSubject, ignoreCase = true)
            val matchClass = selectedClass == 0 || topic.classLevel == selectedClass
            val matchChapter = selectedChapter == "ALL" || topic.chapter.equals(selectedChapter, ignoreCase = true)
            val matchStatus = selectedStatus == "ALL" || topic.status == selectedStatus
            matchQuery && matchSubject && matchClass && matchChapter && matchStatus
        }
    }

    val groupedByChapter = remember(filteredTopics) {
        filteredTopics.groupBy { it.chapter }
    }

    val completedCount = syllabusProgress.first
    val totalCount = syllabusProgress.second
    val progressPercent = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall Syllabus Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syllabus_progress_header_card"),
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
                                text = "NCERT NEET SYLLABUS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = CyanAccentGlow
                            )
                            Text(
                                text = "Physics • Chemistry • Biology (11 & 12)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "$progressPercent%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = EmeraldSuccess,
                        trackColor = MedicalBorder
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$completedCount of $totalCount topics completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search chapter, topic, subtopic...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("syllabus_search_input")
            )
        }

        // Filter: Subjects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "Physics", "Chemistry", "Biology").forEach { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_subject_${subj.lowercase()}")
                    )
                }
            }
        }

        // Filter: Class 11 vs 12
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(Pair(0, "All Classes"), Pair(11, "Class 11"), Pair(12, "Class 12")).forEach { (cls, label) ->
                    FilterChip(
                        selected = selectedClass == cls,
                        onClick = { selectedClass = cls },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_class_$cls")
                    )
                }
            }
        }

        // Filter: Chapter (Subject -> Class -> Chapter navigation)
        if (availableChapters.size > 2) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CHAPTER FILTER (${availableChapters.size - 1} AVAILABLE)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccentGlow
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("syllabus_chapter_row")
                    ) {
                        items(availableChapters) { ch ->
                            FilterChip(
                                selected = selectedChapter == ch,
                                onClick = { selectedChapter = ch },
                                label = { Text(if (ch == "ALL") "All Chapters" else ch, fontSize = 11.sp) },
                                modifier = Modifier.testTag("filter_chapter_${ch.lowercase().take(12)}")
                            )
                        }
                    }
                }
            }
        }

        // Filter: Status (All, Not Started, In Progress, Completed)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("ALL", "All"),
                    Pair("NOT_STARTED", "Not Started"),
                    Pair("IN_PROGRESS", "In Progress"),
                    Pair("COMPLETED", "Done")
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(label, fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_status_${status.lowercase()}")
                    )
                }
            }
        }

        item {
            Text(
                text = "${filteredTopics.size} TOPICS FOUND IN ${groupedByChapter.size} CHAPTERS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        // Render topics grouped by Chapter (Subject -> Class -> Chapter -> Topic)
        groupedByChapter.forEach { (chapterName, chapterTopics) ->
            val completedInChapter = chapterTopics.count { it.status == "COMPLETED" }
            item(key = "header_$chapterName") {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chapter_header_${chapterName.take(15).lowercase().replace(' ', '_')}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chapterName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanAccentGlow,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${chapterTopics.firstOrNull()?.subject ?: ""} • Class ${chapterTopics.firstOrNull()?.classLevel ?: ""}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (completedInChapter == chapterTopics.size && chapterTopics.isNotEmpty()) Color(0xFF064E3B) else MedicalDarkBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$completedInChapter / ${chapterTopics.size} Done",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completedInChapter == chapterTopics.size && chapterTopics.isNotEmpty()) EmeraldSuccess else AmberWarning
                            )
                        }
                    }
                }
            }

            items(chapterTopics, key = { it.id }) { topic ->
                SyllabusTopicCard(
                    topic = topic,
                    onStatusChange = { newStatus ->
                        viewModel.updateSyllabusTopicStatus(topic, newStatus)
                    },
                    onStartPomodoro = {
                        viewModel.setTimerTags(topic.subject, topic.chapter, topic.topic)
                        onNavigate(AppTab.POMODORO)
                    }
                )
            }
        }
    }
}

@Composable
private fun SyllabusTopicCard(
    topic: SyllabusTopic,
    onStatusChange: (String) -> Unit,
    onStartPomodoro: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("syllabus_card_${topic.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (topic.status == "COMPLETED") Color(0xFF0D1E1A) else MedicalSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when (topic.status) {
                    "COMPLETED" -> EmeraldSuccess.copy(alpha = 0.5f)
                    "IN_PROGRESS" -> AmberWarning.copy(alpha = 0.5f)
                    else -> MedicalBorder
                }
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
                SubjectBadge(subject = topic.subject)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Class ${topic.classLevel}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onStartPomodoro,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Study this topic",
                        tint = CyanAccentGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${topic.chapter}: ${topic.topic}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (topic.subtopic.isNotEmpty()) {
                Text(
                    text = topic.subtopic,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MedicalBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Status Selector Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusButton(
                    label = "Not Started",
                    isSelected = topic.status == "NOT_STARTED",
                    selectedColor = TextMuted,
                    modifier = Modifier.weight(1f),
                    testTag = "status_not_started_${topic.id}"
                ) { onStatusChange("NOT_STARTED") }

                StatusButton(
                    label = "In Progress",
                    isSelected = topic.status == "IN_PROGRESS",
                    selectedColor = AmberWarning,
                    modifier = Modifier.weight(1f),
                    testTag = "status_in_progress_${topic.id}"
                ) { onStatusChange("IN_PROGRESS") }

                StatusButton(
                    label = "Completed",
                    isSelected = topic.status == "COMPLETED",
                    selectedColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    testTag = "status_completed_${topic.id}"
                ) { onStatusChange("COMPLETED") }
            }
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.2f) else MedicalSurfaceVariant,
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(selectedColor)) else null,
        modifier = modifier.testTag(testTag)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                color = if (isSelected) selectedColor else TextMuted
            )
        }
    }
}
