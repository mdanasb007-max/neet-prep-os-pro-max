package com.example.data.repository

import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import com.example.data.database.NeetDefaultData
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NeetRepository(
    private val database: AppDatabase,
    private val appContext: Context
) {
    private val routineDao = database.routineDao()
    private val dailyPlanDao = database.dailyPlanDao()
    private val pomodoroDao = database.pomodoroDao()
    private val studyDao = database.studyDao()
    private val questionDao = database.questionDao()
    private val syllabusDao = database.syllabusDao()
    private val revisionDao = database.revisionDao()
    private val mistakeDao = database.mistakeDao()
    private val settingDao = database.settingDao()

    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    suspend fun initializeIfEmpty() {
        withContext(Dispatchers.IO) {
            database.populateInitialData()
        }
    }

    // ================= FIXED DAILY PLAN =================

    fun getDailyPlan(date: String = getTodayDate()): Flow<DailyPlan?> {
        return dailyPlanDao.getPlanByDate(date)
    }

    fun getDailyPlanTopics(date: String = getTodayDate()): Flow<List<DailyPlanTopic>> {
        return dailyPlanDao.getPlanTopicsForDate(date)
    }

    fun getYesterdayPendingTopicsCount(date: String = getTodayDate()): Flow<Int> {
        return dailyPlanDao.getPlanTopicsForDate(date).map { list ->
            list.count { it.isPendingFromYesterday && it.status != "COMPLETED" }
        }
    }

    /**
     * Ensures Today's Plan exists and is LOCKED.
     * If already generated, it is NEVER modified or re-shuffled.
     * If generating for a new date, carries forward yesterday's pending topics
     * and selects the next sequential topics from the official NEET UG 2026 syllabus.
     */
    suspend fun ensureDailyPlanExists(date: String = getTodayDate()) {
        withContext(Dispatchers.IO) {
            // First ensure syllabus database is initialized
            if (syllabusDao.getTopicCountSync() == 0) {
                syllabusDao.insertTopics(NeetDefaultData.getInitialSyllabusTopics())
            }

            // Also ensure routine tasks container exists for this date
            ensureTodayRoutineExists(date)

            // Check if plan already exists for this date
            val existingPlan = dailyPlanDao.getPlanByDateSync(date)
            if (existingPlan != null && existingPlan.isLocked) {
                // FIXED MEANS FIXED: Do NOT change today's topics!
                return@withContext
            }

            val newPlanTopics = mutableListOf<DailyPlanTopic>()

            // Step 1: Check for pending topics from the latest previous plan
            val prevDate = dailyPlanDao.getLatestDateBeforeSync(date)
            if (prevDate != null) {
                val unfinished = dailyPlanDao.getUnfinishedPlanTopicsForDateSync(prevDate)
                for (item in unfinished) {
                    newPlanTopics.add(
                        DailyPlanTopic(
                            date = date,
                            syllabusTopicId = item.syllabusTopicId,
                            subject = item.subject,
                            classLevel = item.classLevel,
                            branch = item.branch,
                            unit = item.unit,
                            chapter = item.chapter,
                            topic = item.topic,
                            subtopic = item.subtopic,
                            status = "IN_PROGRESS",
                            isPendingFromYesterday = true,
                            questionsTarget = item.questionsTarget,
                            routineBlockOrder = null
                        )
                    )
                }
            }

            // Step 2: Allocate sequential topics for each of the 10 routine blocks
            val defaultTasks = NeetDefaultData.getDefaultRoutineTasks(date)

            for (task in defaultTasks) {
                val candidateTopic: SyllabusTopic? = when (task.blockOrder) {
                    1 -> syllabusDao.getNextUnfinishedByBranch("Chemistry", 11, "Physical", 1).firstOrNull()
                    2 -> syllabusDao.getNextUnfinishedByBranch("Chemistry", 12, "Physical", 1).firstOrNull()
                    3 -> syllabusDao.getNextUnfinishedByBranch("Chemistry", 11, "Organic", 1).firstOrNull()
                        ?: syllabusDao.getNextUnfinishedByBranch("Chemistry", 12, "Organic", 1).firstOrNull()
                    4 -> syllabusDao.getNextUnfinishedByBranch("Chemistry", 11, "Inorganic", 1).firstOrNull()
                        ?: syllabusDao.getNextUnfinishedByBranch("Chemistry", 12, "Inorganic", 1).firstOrNull()
                    5 -> syllabusDao.getNextUnfinishedBySubjectAndClass("Physics", 11, 1).firstOrNull()
                    6 -> syllabusDao.getNextUnfinishedBySubjectAndClass("Physics", 12, 1).firstOrNull()
                    7 -> syllabusDao.getNextUnfinishedByBranch("Biology", 11, "Zoology", 1).firstOrNull()
                    8 -> syllabusDao.getNextUnfinishedByBranch("Biology", 11, "Botany", 1).firstOrNull()
                    9 -> syllabusDao.getNextUnfinishedByBranch("Biology", 12, "Zoology", 1).firstOrNull()
                    10 -> syllabusDao.getNextUnfinishedByBranch("Biology", 12, "Botany", 1).firstOrNull()
                    else -> null
                }

                val finalSyllabusTopic = candidateTopic ?: SyllabusTopic(
                    subject = if (task.title.contains("Physics")) "Physics" else if (task.title.contains("Chemistry")) "Chemistry" else "Biology",
                    classLevel = if (task.title.contains("11th")) 11 else 12,
                    chapter = task.chapter,
                    topic = task.topic,
                    subtopic = "Comprehensive Revision & Practice"
                )

                newPlanTopics.add(
                    DailyPlanTopic(
                        date = date,
                        syllabusTopicId = finalSyllabusTopic.id,
                        subject = finalSyllabusTopic.subject,
                        classLevel = finalSyllabusTopic.classLevel,
                        branch = finalSyllabusTopic.branch,
                        unit = finalSyllabusTopic.unit,
                        chapter = finalSyllabusTopic.chapter,
                        topic = finalSyllabusTopic.topic,
                        subtopic = finalSyllabusTopic.subtopic,
                        status = "NOT_STARTED",
                        isPendingFromYesterday = false,
                        questionsTarget = task.targetQuestions,
                        routineBlockOrder = task.blockOrder
                    )
                )
            }

            // Insert locked plan
            dailyPlanDao.insertPlan(DailyPlan(date = date, isLocked = true))
            dailyPlanDao.insertPlanTopics(newPlanTopics)

            // Sync routine tasks with assigned topics
            val savedPlanTopics = dailyPlanDao.getPlanTopicsForDateSync(date)
            for (pt in savedPlanTopics) {
                if (pt.routineBlockOrder != null) {
                    routineDao.updateTaskPlanTopic(
                        date = date,
                        blockOrder = pt.routineBlockOrder,
                        chapter = pt.chapter,
                        topic = pt.topic,
                        planTopicId = pt.id
                    )
                }
            }

            notifyWidget(appContext)
        }
    }

    suspend fun updatePlanTopicStatus(planTopic: DailyPlanTopic, newStatus: String) {
        withContext(Dispatchers.IO) {
            val timestamp = if (newStatus == "COMPLETED") System.currentTimeMillis() else null
            dailyPlanDao.updatePlanTopicStatus(planTopic.id, newStatus, timestamp)

            if (newStatus == "COMPLETED") {
                // Update syllabus topic status
                if (planTopic.syllabusTopicId > 0) {
                    syllabusDao.updateTopicStatus(planTopic.syllabusTopicId, "COMPLETED", getTodayDate())
                }

                // Automatically schedule Spaced Revision (R1 to R7)
                addCustomRevision(
                    subject = planTopic.subject,
                    chapter = planTopic.chapter,
                    topic = planTopic.topic,
                    stage = 1
                )

                // If linked to a routine block, complete that routine task too
                if (planTopic.routineBlockOrder != null) {
                    val tasks = routineDao.getTasksForDateSync(planTopic.date)
                    val matchedTask = tasks.find { it.blockOrder == planTopic.routineBlockOrder }
                    if (matchedTask != null) {
                        routineDao.updateTaskCompletion(matchedTask.id, true)
                    }
                }
            } else if (newStatus == "NOT_STARTED") {
                // Uncomplete linked routine task
                if (planTopic.routineBlockOrder != null) {
                    val tasks = routineDao.getTasksForDateSync(planTopic.date)
                    val matchedTask = tasks.find { it.blockOrder == planTopic.routineBlockOrder }
                    if (matchedTask != null) {
                        routineDao.updateTaskCompletion(matchedTask.id, false)
                    }
                }
            }

            notifyWidget(appContext)
        }
    }

    suspend fun logQuestionsForPlanTopic(
        planTopic: DailyPlanTopic,
        attempted: Int,
        correct: Int,
        notes: String = ""
    ) {
        withContext(Dispatchers.IO) {
            dailyPlanDao.updatePlanTopicQuestions(planTopic.id, attempted, correct)

            // Save question record
            questionDao.insertQuestionRecord(
                QuestionRecord(
                    date = planTopic.date,
                    subject = planTopic.subject,
                    chapter = planTopic.chapter,
                    topic = planTopic.topic,
                    attempted = attempted,
                    correct = correct,
                    notes = notes,
                    dailyPlanTopicId = planTopic.id
                )
            )

            // Update routine task completed questions if linked
            if (planTopic.routineBlockOrder != null) {
                val tasks = routineDao.getTasksForDateSync(planTopic.date)
                val matched = tasks.find { it.blockOrder == planTopic.routineBlockOrder }
                if (matched != null) {
                    routineDao.updateTask(matched.copy(completedQuestions = attempted))
                }
            }

            notifyWidget(appContext)
        }
    }

    suspend fun recordPomodoroSession(
        date: String,
        durationMinutes: Int,
        mode: String,
        subject: String,
        chapter: String,
        topic: String,
        dailyPlanTopicId: Long?
    ) {
        withContext(Dispatchers.IO) {
            pomodoroDao.insertPomodoro(
                PomodoroSession(
                    date = date,
                    durationMinutes = durationMinutes,
                    mode = mode,
                    subject = subject,
                    chapter = chapter,
                    topic = topic,
                    dailyPlanTopicId = dailyPlanTopicId
                )
            )

            // ONLY study sessions count toward "Aaj itna padha"
            if (mode == "STUDY") {
                studyDao.insertSession(
                    StudySession(
                        date = date,
                        durationMinutes = durationMinutes,
                        sessionType = "STUDY",
                        subject = subject,
                        chapter = chapter,
                        topic = topic,
                        dailyPlanTopicId = dailyPlanTopicId,
                        notes = "Pomodoro study session"
                    )
                )

                if (dailyPlanTopicId != null) {
                    dailyPlanDao.addStudyMinutesToPlanTopic(dailyPlanTopicId, durationMinutes)
                }
            }

            notifyWidget(appContext)
        }
    }

    // ================= ROUTINE =================

    suspend fun ensureTodayRoutineExists(date: String = getTodayDate()) {
        withContext(Dispatchers.IO) {
            val count = routineDao.countTasksForDate(date)
            if (count == 0) {
                val defaults = NeetDefaultData.getDefaultRoutineTasks(date)
                routineDao.insertTasks(defaults)
            }
        }
    }

    fun getTasksForDate(date: String): Flow<List<RoutineTask>> {
        return routineDao.getTasksForDate(date)
    }

    suspend fun getTasksForDateSync(date: String): List<RoutineTask> {
        return withContext(Dispatchers.IO) {
            routineDao.getTasksForDateSync(date)
        }
    }

    suspend fun updateTask(task: RoutineTask) {
        withContext(Dispatchers.IO) {
            routineDao.updateTask(task)
            notifyWidget(appContext)
        }
    }

    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            routineDao.updateTaskCompletion(taskId, isCompleted)
            val allTasks = routineDao.getAllTasksSync()
            val task = allTasks.find { it.id == taskId }
            if (task != null) {
                val planTopics = dailyPlanDao.getPlanTopicsForDateSync(task.date)
                val matchedPlanTopic = planTopics.find {
                    (task.dailyPlanTopicId != null && it.id == task.dailyPlanTopicId) ||
                    (it.routineBlockOrder == task.blockOrder)
                }
                if (matchedPlanTopic != null) {
                    val newPlanStatus = if (isCompleted) "COMPLETED" else "NOT_STARTED"
                    val timestamp = if (isCompleted) System.currentTimeMillis() else null
                    dailyPlanDao.updatePlanTopicStatus(matchedPlanTopic.id, newPlanStatus, timestamp)
                    if (isCompleted) {
                        if (matchedPlanTopic.syllabusTopicId > 0) {
                            syllabusDao.updateTopicStatus(matchedPlanTopic.syllabusTopicId, "COMPLETED", task.date)
                        }
                        addCustomRevision(
                            subject = matchedPlanTopic.subject,
                            chapter = matchedPlanTopic.chapter,
                            topic = matchedPlanTopic.topic,
                            stage = 1
                        )
                    }
                }
            }
            notifyWidget(appContext)
        }
    }

    // ================= STUDY SESSIONS =================
    fun getTodayStudyMinutes(date: String = getTodayDate()): Flow<Int> {
        return studyDao.getStudyMinutesForDate(date)
    }

    suspend fun getTodayStudyMinutesSync(date: String = getTodayDate()): Int {
        return withContext(Dispatchers.IO) {
            studyDao.getStudyMinutesForDateSync(date)
        }
    }

    fun getTodayPomodoroCount(date: String = getTodayDate()): Flow<Int> {
        return pomodoroDao.getCompletedStudyPomodoroCount(date)
    }

    fun getSessionsForDate(date: String): Flow<List<StudySession>> {
        return studyDao.getSessionsForDate(date)
    }

    fun getRecentSessions(): Flow<List<StudySession>> {
        return studyDao.getRecentSessions()
    }

    suspend fun saveStudySession(session: StudySession): Long {
        return withContext(Dispatchers.IO) {
            val id = studyDao.insertSession(session)
            notifyWidget(appContext)
            id
        }
    }

    suspend fun deleteStudySession(session: StudySession) {
        withContext(Dispatchers.IO) {
            studyDao.deleteSession(session)
            notifyWidget(appContext)
        }
    }

    // ================= QUESTIONS & ACCURACY =================
    fun getTodayQuestionsAttempted(date: String = getTodayDate()): Flow<Int> {
        return questionDao.getTotalAttemptedForDate(date)
    }

    fun getTodayQuestionsCorrect(date: String = getTodayDate()): Flow<Int> {
        return questionDao.getTotalCorrectForDate(date)
    }

    fun getQuestionsForDate(date: String): Flow<List<QuestionRecord>> {
        return questionDao.getQuestionsForDate(date)
    }

    suspend fun saveQuestionRecord(record: QuestionRecord): Long {
        return withContext(Dispatchers.IO) {
            val id = questionDao.insertQuestionRecord(record)
            notifyWidget(appContext)
            id
        }
    }

    suspend fun deleteQuestionRecord(record: QuestionRecord) {
        withContext(Dispatchers.IO) {
            questionDao.deleteQuestionRecord(record)
            notifyWidget(appContext)
        }
    }

    // ================= SYLLABUS =================
    fun getAllSyllabusTopics(): Flow<List<SyllabusTopic>> {
        return syllabusDao.getAllTopics()
    }

    fun getTopicsBySubjectAndClass(subject: String, classLevel: Int): Flow<List<SyllabusTopic>> {
        return syllabusDao.getTopicsBySubjectAndClass(subject, classLevel)
    }

    fun getSyllabusProgress(): Flow<Pair<Int, Int>> {
        return combine(
            syllabusDao.getCompletedTopicCount(),
            syllabusDao.getTotalTopicCount()
        ) { completed, total ->
            Pair(completed, total)
        }
    }

    fun getUnfinishedTopics(): Flow<List<SyllabusTopic>> {
        return syllabusDao.getUnfinishedTopics()
    }

    suspend fun updateTopicStatus(id: Long, status: String) {
        withContext(Dispatchers.IO) {
            val date = if (status == "COMPLETED") getTodayDate() else null
            syllabusDao.updateTopicStatus(id, status, date)

            // Also sync any daily_plan_topics for today with matching syllabusTopicId
            val today = getTodayDate()
            val planTopics = dailyPlanDao.getPlanTopicsForDateSync(today)
            val matched = planTopics.filter { it.syllabusTopicId == id }
            for (pt in matched) {
                val ts = if (status == "COMPLETED") System.currentTimeMillis() else null
                dailyPlanDao.updatePlanTopicStatus(pt.id, status, ts)
                if (pt.routineBlockOrder != null) {
                    val tasks = routineDao.getTasksForDateSync(today)
                    val rt = tasks.find { it.blockOrder == pt.routineBlockOrder }
                    if (rt != null) {
                        routineDao.updateTaskCompletion(rt.id, status == "COMPLETED")
                    }
                }
            }

            if (status == "COMPLETED") {
                val allTopics = syllabusDao.getAllTopicsSync()
                val topic = allTopics.find { it.id == id }
                if (topic != null) {
                    addTopicToRevision(topic, startingStage = 1)
                }
            }

            notifyWidget(appContext)
        }
    }

    suspend fun updateTopic(topic: SyllabusTopic) {
        withContext(Dispatchers.IO) {
            syllabusDao.updateTopic(topic)
        }
    }

    // ================= REVISION R1 - R7 =================
    fun getDueRevisions(today: String = getTodayDate()): Flow<List<RevisionItem>> {
        return revisionDao.getDueRevisions(today)
    }

    fun getUpcomingRevisions(today: String = getTodayDate()): Flow<List<RevisionItem>> {
        return revisionDao.getUpcomingRevisions(today)
    }

    fun getCompletedRevisions(): Flow<List<RevisionItem>> {
        return revisionDao.getCompletedRevisions()
    }

    fun getDueRevisionCount(today: String = getTodayDate()): Flow<Int> {
        return revisionDao.getDueCount(today)
    }

    suspend fun addTopicToRevision(topic: SyllabusTopic, startingStage: Int = 1) {
        withContext(Dispatchers.IO) {
            val nextDue = calculateNextRevisionDate(startingStage)
            val item = RevisionItem(
                topicId = topic.id,
                subject = topic.subject,
                chapter = topic.chapter,
                topic = topic.topic,
                currentStage = startingStage,
                dueDate = nextDue,
                isCompleted = false
            )
            revisionDao.insertRevision(item)
        }
    }

    suspend fun addCustomRevision(subject: String, chapter: String, topic: String, stage: Int = 1) {
        withContext(Dispatchers.IO) {
            val nextDue = calculateNextRevisionDate(stage)
            val item = RevisionItem(
                subject = subject,
                chapter = chapter,
                topic = topic,
                currentStage = stage,
                dueDate = nextDue,
                isCompleted = false
            )
            revisionDao.insertRevision(item)
        }
    }

    suspend fun advanceRevisionStage(item: RevisionItem) {
        withContext(Dispatchers.IO) {
            val today = getTodayDate()
            if (item.currentStage >= 7) {
                val completedItem = item.copy(
                    isCompleted = true,
                    lastReviewedDate = today
                )
                revisionDao.updateRevision(completedItem)
            } else {
                val nextStage = item.currentStage + 1
                val nextDueDate = calculateNextRevisionDate(nextStage)
                val updatedItem = item.copy(
                    currentStage = nextStage,
                    dueDate = nextDueDate,
                    lastReviewedDate = today
                )
                revisionDao.updateRevision(updatedItem)
            }
        }
    }

    suspend fun deleteRevision(item: RevisionItem) {
        withContext(Dispatchers.IO) {
            revisionDao.deleteRevision(item)
        }
    }

    private fun calculateNextRevisionDate(stage: Int): String {
        val daysToAdd = when (stage) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            5 -> 21
            6 -> 30
            7 -> 60
            else -> 1
        }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    // ================= MISTAKE BOOK =================
    fun getAllMistakes(): Flow<List<MistakeItem>> {
        return mistakeDao.getAllMistakes()
    }

    fun getMistakesByStatus(status: String): Flow<List<MistakeItem>> {
        return mistakeDao.getMistakesByStatus(status)
    }

    fun getTotalMistakesCount(): Flow<Int> {
        return mistakeDao.getTotalMistakesCount()
    }

    fun getUnresolvedMistakesCount(): Flow<Int> {
        return mistakeDao.getUnresolvedMistakesCount()
    }

    suspend fun addMistake(item: MistakeItem): Long {
        return withContext(Dispatchers.IO) {
            mistakeDao.insertMistake(item)
        }
    }

    suspend fun updateMistake(item: MistakeItem) {
        withContext(Dispatchers.IO) {
            mistakeDao.updateMistake(item)
        }
    }

    suspend fun deleteMistake(item: MistakeItem) {
        withContext(Dispatchers.IO) {
            mistakeDao.deleteMistake(item)
        }
    }

    // ================= 7-DAY ANALYTICS =================
    fun getLast7DaysDates(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            list.add(sdf.format(c.time))
        }
        return list
    }

    fun getSessionsFromDate(startDate: String): Flow<List<StudySession>> {
        return studyDao.getSessionsFromDate(startDate)
    }

    fun getQuestionsFromDate(startDate: String): Flow<List<QuestionRecord>> {
        return questionDao.getQuestionsFromDate(startDate)
    }

    fun getDistinctRoutineDates(): Flow<List<String>> {
        return routineDao.getDistinctRoutineDates()
    }

    // ================= SETTINGS & BACKUP =================
    fun getSetting(key: String): Flow<String?> {
        return settingDao.getSetting(key)
    }

    suspend fun setSetting(key: String, value: String) {
        withContext(Dispatchers.IO) {
            settingDao.setSetting(AppSetting(key, value))
        }
    }

    suspend fun resetTodayProgress(date: String = getTodayDate()) {
        withContext(Dispatchers.IO) {
            routineDao.deleteTasksForDate(date)
            dailyPlanDao.deletePlanForDate(date)
            dailyPlanDao.deletePlanTopicsForDate(date)
            studyDao.deleteSessionsForDate(date)
            questionDao.deleteQuestionsForDate(date)

            ensureDailyPlanExists(date)
            notifyWidget(appContext)
        }
    }

    suspend fun resetAllData() {
        withContext(Dispatchers.IO) {
            routineDao.clearAll()
            dailyPlanDao.clearAllPlans()
            dailyPlanDao.clearAllPlanTopics()
            pomodoroDao.clearAll()
            studyDao.clearAll()
            questionDao.clearAll()
            syllabusDao.clearAll()
            revisionDao.clearAll()
            mistakeDao.clearAll()
            settingDao.clearAll()

            database.populateInitialData()
            ensureDailyPlanExists(getTodayDate())
            notifyWidget(appContext)
        }
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        val planArray = JSONArray()
        dailyPlanDao.getAllPlansSync().forEach { p ->
            val obj = JSONObject().apply {
                put("date", p.date)
                put("isLocked", p.isLocked)
            }
            planArray.put(obj)
        }
        root.put("dailyPlans", planArray)

        val planTopicsArray = JSONArray()
        dailyPlanDao.getAllPlanTopicsSync().forEach { pt ->
            val obj = JSONObject().apply {
                put("date", pt.date)
                put("syllabusTopicId", pt.syllabusTopicId)
                put("subject", pt.subject)
                put("classLevel", pt.classLevel)
                put("branch", pt.branch)
                put("chapter", pt.chapter)
                put("topic", pt.topic)
                put("status", pt.status)
                put("questionsTarget", pt.questionsTarget)
                put("questionsAttempted", pt.questionsAttempted)
                put("questionsCorrect", pt.questionsCorrect)
                put("studyMinutes", pt.studyMinutes)
            }
            planTopicsArray.put(obj)
        }
        root.put("dailyPlanTopics", planTopicsArray)

        val routineArray = JSONArray()
        routineDao.getAllTasksSync().forEach { r ->
            val obj = JSONObject().apply {
                put("date", r.date)
                put("blockOrder", r.blockOrder)
                put("title", r.title)
                put("targetQuestions", r.targetQuestions)
                put("completedQuestions", r.completedQuestions)
                put("plannedMinutes", r.plannedMinutes)
                put("isCompleted", r.isCompleted)
                put("chapter", r.chapter)
                put("topic", r.topic)
            }
            routineArray.put(obj)
        }
        root.put("routines", routineArray)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("dailyPlans")) {
                val array = root.getJSONArray("dailyPlans")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    dailyPlanDao.insertPlan(DailyPlan(date = obj.getString("date"), isLocked = obj.getBoolean("isLocked")))
                }
            }
            notifyWidget(appContext)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        fun notifyWidget(context: Context) {
            try {
                val intent = Intent("com.example.ACTION_UPDATE_ROUTINE_WIDGET")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
