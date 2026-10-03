package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.NeetRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    HOME,
    ROUTINE,
    SYLLABUS,
    TRACKER,
    REVISION,
    ANALYTICS,
    POMODORO,
    MISTAKE_BOOK,
    SETTINGS
}

enum class TimerMode(val defaultMinutes: Int, val label: String) {
    STUDY(25, "Study Session"),
    SHORT_BREAK(5, "Short Break"),
    LONG_BREAK(20, "Long Break") // 20 min as requested in spec
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NeetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NeetRepository

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Navigation State
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Date selection for history/inspection
    private val _selectedDate = MutableStateFlow(todayDate)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // ================= FIXED DAILY PLAN =================
    val todayPlan: StateFlow<DailyPlan?>
    val todayPlanTopics: StateFlow<List<DailyPlanTopic>>
    val yesterdayPendingCount: StateFlow<Int>

    // Routine Tasks for selected date
    val routineTasks: StateFlow<List<RoutineTask>>

    // Today's Study Minutes ("Aaj itna padha")
    val todayStudyMinutes: StateFlow<Int>

    // Today's Pomodoros Completed
    val todayPomodoros: StateFlow<Int>

    // Questions Attempted & Correct for Today
    val todayAttempted: StateFlow<Int>
    val todayCorrect: StateFlow<Int>

    // Syllabus progress (completed, total)
    val syllabusProgress: StateFlow<Pair<Int, Int>>
    val allSyllabusTopics: StateFlow<List<SyllabusTopic>>
    val unfinishedTopics: StateFlow<List<SyllabusTopic>>

    // Revision items
    val dueRevisions: StateFlow<List<RevisionItem>>
    val upcomingRevisions: StateFlow<List<RevisionItem>>
    val completedRevisions: StateFlow<List<RevisionItem>>

    // Mistake Book
    val mistakes: StateFlow<List<MistakeItem>>

    // Recent Sessions
    val recentSessions: StateFlow<List<StudySession>>

    // 7-day data for analytics
    val last7DaysDates: List<String>
    val pastSessions: StateFlow<List<StudySession>>
    val pastQuestions: StateFlow<List<QuestionRecord>>

    // ================= TIMER STATE =================
    private val _timerMode = MutableStateFlow(TimerMode.STUDY)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerSecondsRemaining = MutableStateFlow(25 * 60)
    val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

    private val _timerIsRunning = MutableStateFlow(false)
    val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

    private val _timerSubject = MutableStateFlow("Physics")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private val _timerChapter = MutableStateFlow("")
    val timerChapter: StateFlow<String> = _timerChapter.asStateFlow()

    private val _timerTopic = MutableStateFlow("")
    val timerTopic: StateFlow<String> = _timerTopic.asStateFlow()

    private val _activePlanTopicId = MutableStateFlow<Long?>(null)
    val activePlanTopicId: StateFlow<Long?> = _activePlanTopicId.asStateFlow()

    private val _timerSessionsCount = MutableStateFlow(0)
    val timerSessionsCount: StateFlow<Int> = _timerSessionsCount.asStateFlow()

    private var timerJob: Job? = null

    // Snackbar / Feedback message
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = NeetRepository(db, application)

        // Seed database and ensure today's fixed plan exists & locked
        viewModelScope.launch {
            repository.initializeIfEmpty()
            repository.ensureDailyPlanExists(todayDate)
        }

        todayPlan = _selectedDate.flatMapLatest { date ->
            repository.getDailyPlan(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        todayPlanTopics = _selectedDate.flatMapLatest { date ->
            repository.getDailyPlanTopics(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        yesterdayPendingCount = _selectedDate.flatMapLatest { date ->
            repository.getYesterdayPendingTopicsCount(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        routineTasks = _selectedDate.flatMapLatest { date ->
            repository.getTasksForDate(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayStudyMinutes = _selectedDate.flatMapLatest { date ->
            repository.getTodayStudyMinutes(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        todayPomodoros = _selectedDate.flatMapLatest { date ->
            repository.getTodayPomodoroCount(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        todayAttempted = _selectedDate.flatMapLatest { date ->
            repository.getTodayQuestionsAttempted(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        todayCorrect = _selectedDate.flatMapLatest { date ->
            repository.getTodayQuestionsCorrect(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        syllabusProgress = repository.getSyllabusProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

        allSyllabusTopics = repository.getAllSyllabusTopics()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        unfinishedTopics = repository.getUnfinishedTopics()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dueRevisions = repository.getDueRevisions(todayDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        upcomingRevisions = repository.getUpcomingRevisions(todayDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        completedRevisions = repository.getCompletedRevisions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        mistakes = repository.getAllMistakes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recentSessions = repository.getRecentSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        last7DaysDates = repository.getLast7DaysDates()
        val startDate7DaysAgo = last7DaysDates.firstOrNull() ?: todayDate
        pastSessions = repository.getSessionsFromDate(startDate7DaysAgo)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        pastQuestions = repository.getQuestionsFromDate(startDate7DaysAgo)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun navigateTo(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureDailyPlanExists(date)
        }
    }

    // ================= FIXED DAILY PLAN TOPIC ACTIONS =================

    fun startPomodoroForTopic(topic: DailyPlanTopic) {
        setTimerTags(
            subject = topic.subject,
            chapter = topic.chapter,
            topic = topic.topic,
            planTopicId = topic.id
        )
        setTimerPreset(25, TimerMode.STUDY)
        startTimer()
        navigateTo(AppTab.POMODORO)
    }

    fun togglePlanTopicCompletion(topic: DailyPlanTopic) {
        val newStatus = if (topic.status == "COMPLETED") "NOT_STARTED" else "COMPLETED"
        viewModelScope.launch {
            repository.updatePlanTopicStatus(topic, newStatus)
            if (newStatus == "COMPLETED") {
                _messageEvent.emit("✓ Completed: ${topic.topic}! Scheduled R1 revision.")
            } else {
                _messageEvent.emit("Marked '${topic.topic}' as Pending")
            }
        }
    }

    fun markPlanTopicCompleted(topic: DailyPlanTopic) {
        viewModelScope.launch {
            repository.updatePlanTopicStatus(topic, "COMPLETED")
            _messageEvent.emit("✓ Completed: ${topic.topic}! Scheduled R1 revision.")
        }
    }

    fun setPlanTopicInProgress(topic: DailyPlanTopic) {
        viewModelScope.launch {
            repository.updatePlanTopicStatus(topic, "IN_PROGRESS")
            _messageEvent.emit("Marked '${topic.topic}' as In Progress")
        }
    }

    fun logQuestionsForPlanTopic(
        topic: DailyPlanTopic,
        attempted: Int,
        correct: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.logQuestionsForPlanTopic(topic, attempted, correct, notes)
            val acc = if (attempted > 0) (correct.toFloat() / attempted * 100).toInt() else 0
            _messageEvent.emit("Logged $attempted Qs ($acc% accuracy) for ${topic.topic}")
        }
    }

    // ================= ROUTINE ACTIONS =================
    fun toggleRoutineTask(taskId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, !currentCompleted)
        }
    }

    fun updateRoutineTask(task: RoutineTask) {
        viewModelScope.launch {
            repository.updateTask(task)
            _messageEvent.emit("Updated routine: ${task.title}")
        }
    }

    // ================= POMODORO & TIMER ACTIONS =================
    fun setTimerPreset(minutes: Int, mode: TimerMode = TimerMode.STUDY) {
        pauseTimer()
        _timerMode.value = mode
        _timerTotalSeconds.value = minutes * 60
        _timerSecondsRemaining.value = minutes * 60
    }

    fun setTimerTags(subject: String, chapter: String, topic: String, planTopicId: Long? = null) {
        _timerSubject.value = subject
        _timerChapter.value = chapter
        _timerTopic.value = topic
        _activePlanTopicId.value = planTopicId
    }

    fun startTimer() {
        if (_timerIsRunning.value) return
        _timerIsRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0 && _timerIsRunning.value) {
                delay(1000)
                _timerSecondsRemaining.value -= 1
            }
            if (_timerSecondsRemaining.value <= 0 && _timerIsRunning.value) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _timerIsRunning.value = false
        timerJob?.cancel()
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSecondsRemaining.value = _timerTotalSeconds.value
    }

    fun completeCurrentSession() {
        pauseTimer()
        val elapsedSeconds = (_timerTotalSeconds.value - _timerSecondsRemaining.value).coerceAtLeast(0)
        // If at least 60 seconds elapsed, save actual minutes studied; otherwise use full preset duration
        val minutesStudied = if (elapsedSeconds >= 60) {
            elapsedSeconds / 60
        } else {
            (_timerTotalSeconds.value / 60).coerceAtLeast(1)
        }
        if (_timerMode.value == TimerMode.STUDY) {
            viewModelScope.launch {
                repository.recordPomodoroSession(
                    date = todayDate,
                    durationMinutes = minutesStudied,
                    mode = "STUDY",
                    subject = _timerSubject.value,
                    chapter = _timerChapter.value,
                    topic = _timerTopic.value,
                    dailyPlanTopicId = _activePlanTopicId.value
                )
                val newCount = _timerSessionsCount.value + 1
                _timerSessionsCount.value = newCount
                _messageEvent.emit("Study session completed! +$minutesStudied mins added to Aaj itna padha")
                if (newCount % 4 == 0) {
                    setTimerPreset(20, TimerMode.LONG_BREAK)
                } else {
                    setTimerPreset(5, TimerMode.SHORT_BREAK)
                }
            }
        } else {
            viewModelScope.launch {
                _messageEvent.emit("Break concluded! Ready for next session.")
            }
            setTimerPreset(25, TimerMode.STUDY)
        }
    }

    fun skipTimer() {
        pauseTimer()
        when (_timerMode.value) {
            TimerMode.STUDY -> {
                val newCount = _timerSessionsCount.value + 1
                _timerSessionsCount.value = newCount
                if (newCount % 4 == 0) {
                    setTimerPreset(20, TimerMode.LONG_BREAK) // 20 min long break
                } else {
                    setTimerPreset(5, TimerMode.SHORT_BREAK)
                }
            }
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> {
                setTimerPreset(25, TimerMode.STUDY)
            }
        }
    }

    private fun onTimerFinished() {
        _timerIsRunning.value = false
        val currentMode = _timerMode.value
        val totalMinutes = _timerTotalSeconds.value / 60

        if (currentMode == TimerMode.STUDY) {
            // ONLY study sessions count towards "Aaj itna padha"
            viewModelScope.launch {
                repository.recordPomodoroSession(
                    date = todayDate,
                    durationMinutes = totalMinutes,
                    mode = "STUDY",
                    subject = _timerSubject.value,
                    chapter = _timerChapter.value,
                    topic = _timerTopic.value,
                    dailyPlanTopicId = _activePlanTopicId.value
                )

                val newCount = _timerSessionsCount.value + 1
                _timerSessionsCount.value = newCount

                NotificationHelper.sendTimerNotification(
                    getApplication(),
                    "Pomodoro Study Finished! 🎉",
                    "Completed $totalMinutes minutes of ${_timerSubject.value}. Take a well-earned break!"
                )
                _messageEvent.emit("Pomodoro completed! +$totalMinutes mins added to Aaj itna padha")

                // Auto switch to break mode (20m long break after 4 pomodoros)
                if (newCount % 4 == 0) {
                    setTimerPreset(20, TimerMode.LONG_BREAK)
                } else {
                    setTimerPreset(5, TimerMode.SHORT_BREAK)
                }
            }
        } else {
            // Break finished
            NotificationHelper.sendTimerNotification(
                getApplication(),
                "Break Finished! ⏱️",
                "Ready for your next NEET study block? Let's focus!"
            )
            viewModelScope.launch {
                _messageEvent.emit("Break finished! Time for the next study session.")
            }
            setTimerPreset(25, TimerMode.STUDY)
        }
    }

    // ================= MANUAL STUDY TRACKER =================
    fun logStudySession(
        subject: String,
        chapter: String,
        topic: String,
        durationMinutes: Int,
        questionsAttempted: Int,
        questionsCorrect: Int,
        date: String = todayDate,
        notes: String = ""
    ) {
        viewModelScope.launch {
            if (durationMinutes > 0) {
                repository.saveStudySession(
                    StudySession(
                        date = date,
                        durationMinutes = durationMinutes,
                        sessionType = "STUDY",
                        subject = subject,
                        chapter = chapter,
                        topic = topic,
                        notes = notes
                    )
                )
            }
            if (questionsAttempted > 0) {
                repository.saveQuestionRecord(
                    QuestionRecord(
                        date = date,
                        subject = subject,
                        chapter = chapter,
                        topic = topic,
                        attempted = questionsAttempted,
                        correct = questionsCorrect.coerceIn(0, questionsAttempted),
                        notes = notes
                    )
                )
            }
            _messageEvent.emit("Logged $durationMinutes mins & $questionsAttempted questions!")
        }
    }

    fun deleteStudySession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
            _messageEvent.emit("Session deleted")
        }
    }

    // ================= SYLLABUS ACTIONS =================
    fun updateSyllabusTopicStatus(topic: SyllabusTopic, newStatus: String) {
        viewModelScope.launch {
            repository.updateTopicStatus(topic.id, newStatus)
            if (newStatus == "COMPLETED") {
                repository.addTopicToRevision(topic, startingStage = 1)
                _messageEvent.emit("Marked '${topic.topic}' completed & scheduled R1 revision!")
            } else {
                _messageEvent.emit("Updated status to $newStatus")
            }
        }
    }

    // ================= REVISION ACTIONS =================
    fun completeRevisionStage(item: RevisionItem) {
        viewModelScope.launch {
            repository.advanceRevisionStage(item)
            val nextStage = item.currentStage + 1
            if (nextStage > 7) {
                _messageEvent.emit("Mastered R7 revision for ${item.topic}! 🏆")
            } else {
                _messageEvent.emit("Promoted to Stage R$nextStage!")
            }
        }
    }

    fun addManualRevision(subject: String, chapter: String, topic: String, stage: Int = 1) {
        viewModelScope.launch {
            repository.addCustomRevision(subject, chapter, topic, stage)
            _messageEvent.emit("Scheduled R$stage revision for $topic")
        }
    }

    fun deleteRevision(item: RevisionItem) {
        viewModelScope.launch {
            repository.deleteRevision(item)
            _messageEvent.emit("Revision removed")
        }
    }

    // ================= MISTAKE BOOK ACTIONS =================
    fun addMistake(
        subject: String,
        chapter: String,
        topic: String,
        questionSummary: String,
        mistakeType: String,
        correctConcept: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addMistake(
                MistakeItem(
                    date = todayDate,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    questionSummary = questionSummary,
                    mistakeType = mistakeType,
                    correctConcept = correctConcept,
                    status = "UNRESOLVED",
                    notes = notes
                )
            )
            _messageEvent.emit("Mistake logged to Mistake Book")
        }
    }

    fun updateMistake(item: MistakeItem) {
        viewModelScope.launch {
            repository.updateMistake(item)
            _messageEvent.emit("Mistake updated")
        }
    }

    fun deleteMistake(item: MistakeItem) {
        viewModelScope.launch {
            repository.deleteMistake(item)
            _messageEvent.emit("Mistake deleted")
        }
    }

    // ================= BACKUP & RESET =================
    suspend fun exportDataJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun importDataJson(json: String): Boolean {
        return repository.importBackupJson(json)
    }

    fun resetTodayProgress() {
        viewModelScope.launch {
            repository.resetTodayProgress(todayDate)
            _messageEvent.emit("Today's progress has been reset.")
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _messageEvent.emit("All data has been reset to defaults.")
        }
    }
}
