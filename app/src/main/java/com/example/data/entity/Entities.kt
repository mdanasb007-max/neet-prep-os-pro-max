package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 10 daily routine blocks for a specific calendar date (YYYY-MM-DD).
 */
@Entity(tableName = "routine_tasks")
data class RoutineTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val blockOrder: Int, // 1 to 10
    val title: String,
    val targetQuestions: Int,
    val completedQuestions: Int = 0,
    val plannedMinutes: Int,
    val actualMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val chapter: String = "",
    val topic: String = "",
    val notes: String = "",
    val dailyPlanTopicId: Long? = null
)

/**
 * The fixed daily study plan container for a specific calendar date.
 * Once created for date X, isLocked = true and it will NEVER regenerate.
 */
@Entity(tableName = "daily_plans")
data class DailyPlan(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val generatedTimestamp: Long = System.currentTimeMillis(),
    val isLocked: Boolean = true,
    val notes: String = ""
)

/**
 * An individual topic allocated in Today's Fixed Plan.
 */
@Entity(tableName = "daily_plan_topics")
data class DailyPlanTopic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val syllabusTopicId: Long = 0,
    val subject: String, // "Chemistry", "Physics", "Biology"
    val classLevel: Int, // 11 or 12
    val branch: String = "", // "Physical", "Organic", "Inorganic", "Botany", "Zoology", "General"
    val unit: String = "",
    val chapter: String,
    val topic: String,
    val subtopic: String = "",
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val isPendingFromYesterday: Boolean = false,
    val questionsTarget: Int = 45,
    val questionsAttempted: Int = 0,
    val questionsCorrect: Int = 0,
    val studyMinutes: Int = 0,
    val routineBlockOrder: Int? = null,
    val completedTimestamp: Long? = null
) {
    val questionsIncorrect: Int get() = (questionsAttempted - questionsCorrect).coerceAtLeast(0)
    val accuracy: Float get() = if (questionsAttempted > 0) (questionsCorrect.toFloat() / questionsAttempted) * 100f else 0f
    val isCompleted: Boolean get() = status == "COMPLETED"
}

/**
 * Completed study sessions (Pomodoro or manual study timer).
 * Only STUDY session types count towards "Aaj itna padha".
 */
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val sessionType: String = "STUDY", // "STUDY", "POMODORO", "CUSTOM"
    val subject: String,
    val chapter: String = "",
    val topic: String = "",
    val notes: String = "",
    val dailyPlanTopicId: Long? = null
)

/**
 * Recorded Pomodoro intervals (Study and Breaks).
 */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val mode: String, // "STUDY", "SHORT_BREAK", "LONG_BREAK"
    val subject: String,
    val chapter: String = "",
    val topic: String = "",
    val dailyPlanTopicId: Long? = null,
    val isCompleted: Boolean = true
)

/**
 * Question records for accuracy calculation:
 * Accuracy = (Correct / Attempted) * 100
 */
@Entity(tableName = "question_records")
data class QuestionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val chapter: String = "",
    val topic: String = "",
    val attempted: Int,
    val correct: Int,
    val notes: String = "",
    val dailyPlanTopicId: Long? = null
) {
    val incorrect: Int get() = (attempted - correct).coerceAtLeast(0)
    val accuracy: Float get() = if (attempted > 0) (correct.toFloat() / attempted) * 100f else 0f
}

/**
 * Official NEET UG 2026 syllabus topic.
 * Subject -> Class -> Unit -> Chapter -> Topic -> Subtopic
 */
@Entity(tableName = "syllabus_topics")
data class SyllabusTopic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String, // "Biology", "Physics", "Chemistry"
    val classLevel: Int, // 11 or 12
    val branch: String = "General", // "Physical", "Organic", "Inorganic", "Botany", "Zoology", "General"
    val unit: String = "", // Official NTA unit name
    val chapter: String,
    val topic: String,
    val subtopic: String = "",
    val orderIndex: Int = 0,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val completedDate: String? = null,
    val notes: String = ""
)

/**
 * Spaced revision stages R1 to R7 for completed topics.
 */
@Entity(tableName = "revision_items")
data class RevisionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicId: Long? = null,
    val subject: String,
    val chapter: String,
    val topic: String,
    val currentStage: Int = 1, // 1..7 for R1..R7
    val dueDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = false,
    val lastReviewedDate: String? = null,
    val notes: String = ""
)

/**
 * Mistake book records.
 */
@Entity(tableName = "mistake_items")
data class MistakeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val chapter: String,
    val topic: String = "",
    val questionSummary: String,
    val mistakeType: String, // "Conceptual", "Calculation", "Silly Mistake", "Memory", "Question Reading"
    val correctConcept: String,
    val status: String = "UNRESOLVED", // "UNRESOLVED", "REVIEWED", "MASTERED"
    val notes: String = ""
)

/**
 * App key-value settings.
 */
@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String
)
