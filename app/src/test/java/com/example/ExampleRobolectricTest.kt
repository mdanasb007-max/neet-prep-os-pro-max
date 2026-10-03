package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.NeetDefaultData
import com.example.data.entity.*
import com.example.data.repository.NeetRepository
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.NeetViewModel
import com.example.ui.viewmodel.TimerMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: NeetRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NeetRepository(database, context)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("NEET PREP OS", appName)
    }

    @Test
    fun test1_BottomNavigationTabs() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = NeetViewModel(app)

        assertEquals(AppTab.HOME, viewModel.currentTab.value)

        val testTabs = listOf(
            AppTab.ROUTINE,
            AppTab.SYLLABUS,
            AppTab.TRACKER,
            AppTab.REVISION,
            AppTab.ANALYTICS,
            AppTab.POMODORO,
            AppTab.SETTINGS,
            AppTab.HOME
        )

        testTabs.forEach { tab ->
            viewModel.navigateTo(tab)
            assertEquals("Tab should update correctly to $tab", tab, viewModel.currentTab.value)
        }
    }

    @Test
    fun test2_RoutineCheckboxToggleAndPersistence() = runBlocking {
        val date = "2026-10-03"
        repository.ensureDailyPlanExists(date)

        val tasks = database.routineDao().getTasksForDate(date).first()
        assertEquals(10, tasks.size)

        val task = tasks[0]
        assertFalse("Task starts pending", task.isCompleted)

        // Toggle checkbox on
        repository.toggleTaskCompletion(task.id, true)
        val afterCheck = database.routineDao().getTasksForDate(date).first()
        assertTrue("Task should be completed in Room", afterCheck[0].isCompleted)

        // Verify linked plan topic also marked completed
        val planTopics = database.dailyPlanDao().getPlanTopicsForDateSync(date)
        val linkedTopic = planTopics.find { it.routineBlockOrder == task.blockOrder }
        assertNotNull(linkedTopic)
        assertEquals("COMPLETED", linkedTopic!!.status)

        // Toggle checkbox off
        repository.toggleTaskCompletion(task.id, false)
        val afterUncheck = database.routineDao().getTasksForDate(date).first()
        assertFalse("Task should be pending again in Room", afterUncheck[0].isCompleted)
    }

    @Test
    fun test3_TodayPlanToggleAndRestartPersistence() = runBlocking {
        val date = "2026-10-03"
        repository.ensureDailyPlanExists(date)

        val topics = database.dailyPlanDao().getPlanTopicsForDateSync(date)
        assertTrue(topics.isNotEmpty())

        val target = topics[0]
        assertEquals("NOT_STARTED", target.status)

        // Complete topic
        repository.updatePlanTopicStatus(target, "COMPLETED")
        val updatedTopics = database.dailyPlanDao().getPlanTopicsForDateSync(date)
        assertEquals("COMPLETED", updatedTopics[0].status)
        assertTrue(updatedTopics[0].isCompleted)

        // Simulate app restart: re-read from Room database
        val reloadedTopics = database.dailyPlanDao().getPlanTopicsForDateSync(date)
        assertEquals("COMPLETED", reloadedTopics[0].status)
    }

    @Test
    fun test4_PomodoroStudyAndCompleteSession() = runBlocking {
        val date = "2026-10-03"
        // Record completed study pomodoro
        repository.recordPomodoroSession(
            date = date,
            durationMinutes = 25,
            mode = "STUDY",
            subject = "Physics",
            chapter = "Kinematics",
            topic = "Projectiles",
            dailyPlanTopicId = null
        )

        // Record break session (MUST NOT count towards Aaj itna padha)
        repository.recordPomodoroSession(
            date = date,
            durationMinutes = 5,
            mode = "SHORT_BREAK",
            subject = "Break",
            chapter = "",
            topic = "",
            dailyPlanTopicId = null
        )

        val totalMinutes = database.studyDao().getStudyMinutesForDate(date).first()
        assertEquals(25, totalMinutes) // Only 25 mins, break excluded

        val pomodoroCount = database.pomodoroDao().getCompletedStudyPomodoroCount(date).first()
        assertEquals(1, pomodoroCount)
    }

    @Test
    fun test5_TrackerAccuracyFormulaAndZeroHandling() = runBlocking {
        val date = "2026-10-03"

        // Case 1: 0 attempted
        val zeroAttemptedRecord = QuestionRecord(
            date = date,
            subject = "Biology",
            attempted = 0,
            correct = 0
        )
        assertEquals(0f, zeroAttemptedRecord.accuracy, 0.001f)
        val displayWhenZero = if (zeroAttemptedRecord.attempted > 0) "${zeroAttemptedRecord.accuracy}%" else "--"
        assertEquals("--", displayWhenZero)

        // Case 2: 40 attempted, 30 correct -> 75%
        val validRecord = QuestionRecord(
            date = date,
            subject = "Chemistry",
            attempted = 40,
            correct = 30
        )
        database.questionDao().insertQuestionRecord(validRecord)
        assertEquals(10, validRecord.incorrect)
        assertEquals(75.0f, validRecord.accuracy, 0.001f)

        // Case 3: Delete works
        val allBefore = database.questionDao().getQuestionsForDate(date).first()
        assertEquals(1, allBefore.size)
        database.questionDao().deleteQuestionRecord(allBefore[0])
        val allAfter = database.questionDao().getQuestionsForDate(date).first()
        assertTrue(allAfter.isEmpty())
    }

    @Test
    fun test6_SyllabusSubjectClassChapterTopicNavigation() = runBlocking {
        repository.initializeIfEmpty()

        val allTopics = database.syllabusDao().getAllTopics().first()
        assertTrue("Syllabus topics populated", allTopics.size > 50)

        // Subject filter: Chemistry
        val chemTopics = allTopics.filter { it.subject == "Chemistry" }
        assertTrue(chemTopics.isNotEmpty())

        // Class filter: Class 11
        val chemClass11 = chemTopics.filter { it.classLevel == 11 }
        assertTrue(chemClass11.isNotEmpty())

        // Chapter filter: Some Basic Concepts of Chemistry
        val chapters = chemClass11.map { it.chapter }.distinct()
        assertTrue(chapters.isNotEmpty())
        val firstChapter = chapters.first()
        val chapterTopics = chemClass11.filter { it.chapter == firstChapter }
        assertTrue(chapterTopics.isNotEmpty())

        // Topic completion updates progress
        val firstTopic = chapterTopics.first()
        assertEquals("NOT_STARTED", firstTopic.status)

        repository.updateTopicStatus(firstTopic.id, "COMPLETED")
        val updated = database.syllabusDao().getAllTopics().first().find { it.id == firstTopic.id }
        assertEquals("COMPLETED", updated!!.status)

        // Revision R1 should be created automatically
        val revisions = database.revisionDao().getAllRevisions().first()
        assertTrue("Revision scheduled on completion", revisions.any { it.topic == firstTopic.topic })
    }

    @Test
    fun test7_RevisionR1toR7Progression() = runBlocking {
        val item = RevisionItem(
            subject = "Physics",
            chapter = "Laws of Motion",
            topic = "Friction",
            currentStage = 1,
            dueDate = "2026-10-03"
        )
        val id = database.revisionDao().insertRevision(item)
        val saved = database.revisionDao().getAllRevisions().first().find { it.id == id }!!

        assertEquals(1, saved.currentStage)
        assertFalse(saved.isCompleted)

        // Advance to Stage 2
        repository.advanceRevisionStage(saved)
        val stage2 = database.revisionDao().getAllRevisions().first().find { it.id == id }!!
        assertEquals(2, stage2.currentStage)
        assertFalse(stage2.isCompleted)

        // Advance through Stage 7 to Mastered
        var current = stage2
        for (st in 3..7) {
            repository.advanceRevisionStage(current)
            current = database.revisionDao().getAllRevisions().first().find { it.id == id }!!
        }
        assertEquals(7, current.currentStage)

        // Advance final stage
        repository.advanceRevisionStage(current)
        val mastered = database.revisionDao().getAllRevisions().first().find { it.id == id }!!
        assertTrue(mastered.isCompleted)
    }

    @Test
    fun test8_AnalyticsRoomCalculations() = runBlocking {
        val date = "2026-10-03"
        // Add 2 study sessions
        database.studyDao().insertSession(
            StudySession(
                date = date,
                durationMinutes = 45,
                sessionType = "STUDY",
                subject = "Biology",
                chapter = "Cell Biology",
                topic = "Mitochondria"
            )
        )
        database.studyDao().insertSession(
            StudySession(
                date = date,
                durationMinutes = 60,
                sessionType = "STUDY",
                subject = "Physics",
                chapter = "Work Energy",
                topic = "Power"
            )
        )

        val totalMinutes = database.studyDao().getStudyMinutesForDate(date).first()
        assertEquals(105, totalMinutes)

        // Add questions
        database.questionDao().insertQuestionRecord(
            QuestionRecord(
                date = date,
                subject = "Biology",
                attempted = 50,
                correct = 45
            )
        )
        val totalAttempted = database.questionDao().getTotalAttemptedForDate(date).first()
        val totalCorrect = database.questionDao().getTotalCorrectForDate(date).first()
        assertEquals(50, totalAttempted)
        assertEquals(45, totalCorrect)
        val accuracy = (totalCorrect.toFloat() / totalAttempted.toFloat()) * 100f
        assertEquals(90.0f, accuracy, 0.001f)
    }

    @Test
    fun test9_DashboardValuesFromRoomDatabase() = runBlocking {
        val date = "2026-10-03"
        repository.ensureDailyPlanExists(date)

        val defaultTasks = database.routineDao().getTasksForDate(date).first()
        val targetQuestions = defaultTasks.sumOf { it.targetQuestions }
        assertEquals(390, targetQuestions)

        val targetPlannedMinutes = defaultTasks.sumOf { it.plannedMinutes }
        assertEquals(510, targetPlannedMinutes)

        // Complete 1 routine block
        repository.toggleTaskCompletion(defaultTasks[0].id, true)
        val updatedTasks = database.routineDao().getTasksForDate(date).first()
        val completedCount = updatedTasks.count { it.isCompleted }
        assertEquals(1, completedCount)
    }

    @Test
    fun test10_TimerStateManagement() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = NeetViewModel(app)

        assertEquals(TimerMode.STUDY, viewModel.timerMode.value)
        assertEquals(25 * 60, viewModel.timerTotalSeconds.value)
        assertEquals(25 * 60, viewModel.timerSecondsRemaining.value)
        assertFalse(viewModel.timerIsRunning.value)

        // Start
        viewModel.startTimer()
        assertTrue(viewModel.timerIsRunning.value)

        // Pause
        viewModel.pauseTimer()
        assertFalse(viewModel.timerIsRunning.value)

        // Resume
        viewModel.resumeTimer()
        assertTrue(viewModel.timerIsRunning.value)

        // Reset
        viewModel.resetTimer()
        assertFalse(viewModel.timerIsRunning.value)
        assertEquals(viewModel.timerTotalSeconds.value, viewModel.timerSecondsRemaining.value)

        // Presets
        viewModel.setTimerPreset(50, TimerMode.STUDY)
        assertEquals(50 * 60, viewModel.timerTotalSeconds.value)
        assertEquals(50 * 60, viewModel.timerSecondsRemaining.value)
    }
}
