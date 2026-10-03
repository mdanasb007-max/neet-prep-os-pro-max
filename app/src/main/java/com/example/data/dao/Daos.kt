package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    fun getPlanByDate(date: String): Flow<DailyPlan?>

    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    suspend fun getPlanByDateSync(date: String): DailyPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlan)

    @Query("SELECT * FROM daily_plan_topics WHERE date = :date ORDER BY routineBlockOrder ASC, id ASC")
    fun getPlanTopicsForDate(date: String): Flow<List<DailyPlanTopic>>

    @Query("SELECT * FROM daily_plan_topics WHERE date = :date ORDER BY routineBlockOrder ASC, id ASC")
    suspend fun getPlanTopicsForDateSync(date: String): List<DailyPlanTopic>

    @Query("SELECT * FROM daily_plan_topics WHERE date = :date AND status != 'COMPLETED'")
    suspend fun getUnfinishedPlanTopicsForDateSync(date: String): List<DailyPlanTopic>

    @Query("SELECT * FROM daily_plans ORDER BY date DESC LIMIT 1 OFFSET 1")
    suspend fun getPreviousDayPlanSync(): DailyPlan?

    @Query("SELECT date FROM daily_plans WHERE date < :date ORDER BY date DESC LIMIT 1")
    suspend fun getLatestDateBeforeSync(date: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanTopics(topics: List<DailyPlanTopic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanTopic(topic: DailyPlanTopic): Long

    @Update
    suspend fun updatePlanTopic(topic: DailyPlanTopic)

    @Query("UPDATE daily_plan_topics SET status = :status, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun updatePlanTopicStatus(id: Long, status: String, timestamp: Long?)

    @Query("UPDATE daily_plan_topics SET studyMinutes = studyMinutes + :additionalMinutes WHERE id = :id")
    suspend fun addStudyMinutesToPlanTopic(id: Long, additionalMinutes: Int)

    @Query("UPDATE daily_plan_topics SET questionsAttempted = :attempted, questionsCorrect = :correct WHERE id = :id")
    suspend fun updatePlanTopicQuestions(id: Long, attempted: Int, correct: Int)

    @Query("DELETE FROM daily_plans WHERE date = :date")
    suspend fun deletePlanForDate(date: String)

    @Query("DELETE FROM daily_plan_topics WHERE date = :date")
    suspend fun deletePlanTopicsForDate(date: String)

    @Query("DELETE FROM daily_plans")
    suspend fun clearAllPlans()

    @Query("DELETE FROM daily_plan_topics")
    suspend fun clearAllPlanTopics()

    @Query("SELECT DISTINCT date FROM daily_plans ORDER BY date DESC")
    fun getAllPlanDates(): Flow<List<String>>

    @Query("SELECT * FROM daily_plans")
    suspend fun getAllPlansSync(): List<DailyPlan>

    @Query("SELECT * FROM daily_plan_topics")
    suspend fun getAllPlanTopicsSync(): List<DailyPlanTopic>
}

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getPomodoroSessionsForDate(date: String): Flow<List<PomodoroSession>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE date = :date AND mode = 'STUDY' AND isCompleted = 1")
    fun getCompletedStudyPomodoroCount(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoro(session: PomodoroSession): Long

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearAll()

    @Query("SELECT * FROM pomodoro_sessions")
    suspend fun getAllSync(): List<PomodoroSession>
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_tasks WHERE date = :date ORDER BY blockOrder ASC")
    fun getTasksForDate(date: String): Flow<List<RoutineTask>>

    @Query("SELECT * FROM routine_tasks WHERE date = :date ORDER BY blockOrder ASC")
    suspend fun getTasksForDateSync(date: String): List<RoutineTask>

    @Query("SELECT COUNT(*) FROM routine_tasks WHERE date = :date")
    suspend fun countTasksForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<RoutineTask>)

    @Update
    suspend fun updateTask(task: RoutineTask)

    @Query("UPDATE routine_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean)

    @Query("UPDATE routine_tasks SET chapter = :chapter, topic = :topic, dailyPlanTopicId = :planTopicId WHERE date = :date AND blockOrder = :blockOrder")
    suspend fun updateTaskPlanTopic(date: String, blockOrder: Int, chapter: String, topic: String, planTopicId: Long)

    @Query("DELETE FROM routine_tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    @Query("DELETE FROM routine_tasks")
    suspend fun clearAll()

    @Query("SELECT DISTINCT date FROM routine_tasks ORDER BY date DESC")
    fun getDistinctRoutineDates(): Flow<List<String>>

    @Query("SELECT * FROM routine_tasks")
    suspend fun getAllTasksSync(): List<RoutineTask>
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsForDate(date: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<StudySession>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE date = :date AND sessionType = 'STUDY'")
    fun getStudyMinutesForDate(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE date = :date AND sessionType = 'STUDY'")
    suspend fun getStudyMinutesForDateSync(date: String): Int

    @Query("SELECT COUNT(*) FROM study_sessions WHERE date = :date AND (sessionType = 'POMODORO' OR sessionType = 'STUDY')")
    fun getCompletedPomodoroCountForDate(date: String): Flow<Int>

    @Query("SELECT * FROM study_sessions WHERE date >= :startDate ORDER BY date ASC, timestamp ASC")
    fun getSessionsFromDate(startDate: String): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<StudySession>)

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE date = :date")
    suspend fun deleteSessionsForDate(date: String)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()

    @Query("SELECT * FROM study_sessions")
    suspend fun getAllSessionsSync(): List<StudySession>
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question_records WHERE date = :date ORDER BY timestamp DESC")
    fun getQuestionsForDate(date: String): Flow<List<QuestionRecord>>

    @Query("SELECT COALESCE(SUM(attempted), 0) FROM question_records WHERE date = :date")
    fun getTotalAttemptedForDate(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(correct), 0) FROM question_records WHERE date = :date")
    fun getTotalCorrectForDate(date: String): Flow<Int>

    @Query("SELECT * FROM question_records WHERE date >= :startDate ORDER BY date ASC, timestamp ASC")
    fun getQuestionsFromDate(startDate: String): Flow<List<QuestionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionRecord(record: QuestionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<QuestionRecord>)

    @Delete
    suspend fun deleteQuestionRecord(record: QuestionRecord)

    @Query("DELETE FROM question_records WHERE date = :date")
    suspend fun deleteQuestionsForDate(date: String)

    @Query("DELETE FROM question_records")
    suspend fun clearAll()

    @Query("SELECT * FROM question_records")
    suspend fun getAllQuestionsSync(): List<QuestionRecord>
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_topics ORDER BY orderIndex ASC, id ASC")
    fun getAllTopics(): Flow<List<SyllabusTopic>>

    @Query("SELECT * FROM syllabus_topics WHERE subject = :subject AND classLevel = :classLevel ORDER BY orderIndex ASC, id ASC")
    fun getTopicsBySubjectAndClass(subject: String, classLevel: Int): Flow<List<SyllabusTopic>>

    @Query("SELECT * FROM syllabus_topics WHERE status != 'COMPLETED' ORDER BY orderIndex ASC, id ASC")
    fun getUnfinishedTopics(): Flow<List<SyllabusTopic>>

    @Query("SELECT * FROM syllabus_topics WHERE subject = :subject AND classLevel = :classLevel AND branch = :branch AND status != 'COMPLETED' ORDER BY orderIndex ASC LIMIT :limit")
    suspend fun getNextUnfinishedByBranch(subject: String, classLevel: Int, branch: String, limit: Int): List<SyllabusTopic>

    @Query("SELECT * FROM syllabus_topics WHERE subject = :subject AND classLevel = :classLevel AND status != 'COMPLETED' ORDER BY orderIndex ASC LIMIT :limit")
    suspend fun getNextUnfinishedBySubjectAndClass(subject: String, classLevel: Int, limit: Int): List<SyllabusTopic>

    @Query("SELECT COUNT(*) FROM syllabus_topics")
    fun getTotalTopicCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM syllabus_topics WHERE status = 'COMPLETED'")
    fun getCompletedTopicCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM syllabus_topics")
    suspend fun getTopicCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<SyllabusTopic>)

    @Update
    suspend fun updateTopic(topic: SyllabusTopic)

    @Query("UPDATE syllabus_topics SET status = :status, completedDate = :date WHERE id = :id")
    suspend fun updateTopicStatus(id: Long, status: String, date: String?)

    @Query("DELETE FROM syllabus_topics")
    suspend fun clearAll()

    @Query("SELECT * FROM syllabus_topics")
    suspend fun getAllTopicsSync(): List<SyllabusTopic>
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_items WHERE dueDate <= :today AND isCompleted = 0 ORDER BY dueDate ASC, currentStage ASC")
    fun getDueRevisions(today: String): Flow<List<RevisionItem>>

    @Query("SELECT * FROM revision_items WHERE dueDate <= :today AND isCompleted = 0 ORDER BY dueDate ASC, currentStage ASC")
    suspend fun getDueRevisionsSync(today: String): List<RevisionItem>

    @Query("SELECT * FROM revision_items WHERE dueDate > :today AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getUpcomingRevisions(today: String): Flow<List<RevisionItem>>

    @Query("SELECT * FROM revision_items WHERE isCompleted = 1 ORDER BY lastReviewedDate DESC")
    fun getCompletedRevisions(): Flow<List<RevisionItem>>

    @Query("SELECT * FROM revision_items ORDER BY dueDate ASC")
    fun getAllRevisions(): Flow<List<RevisionItem>>

    @Query("SELECT COUNT(*) FROM revision_items WHERE dueDate <= :today AND isCompleted = 0")
    fun getDueCount(today: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(item: RevisionItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RevisionItem>)

    @Update
    suspend fun updateRevision(item: RevisionItem)

    @Delete
    suspend fun deleteRevision(item: RevisionItem)

    @Query("DELETE FROM revision_items")
    suspend fun clearAll()

    @Query("SELECT * FROM revision_items")
    suspend fun getAllRevisionsSync(): List<RevisionItem>
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistake_items ORDER BY timestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeItem>>

    @Query("SELECT * FROM mistake_items WHERE status = :status ORDER BY timestamp DESC")
    fun getMistakesByStatus(status: String): Flow<List<MistakeItem>>

    @Query("SELECT COUNT(*) FROM mistake_items")
    fun getTotalMistakesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mistake_items WHERE status = 'UNRESOLVED'")
    fun getUnresolvedMistakesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(item: MistakeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MistakeItem>)

    @Update
    suspend fun updateMistake(item: MistakeItem)

    @Delete
    suspend fun deleteMistake(item: MistakeItem)

    @Query("DELETE FROM mistake_items")
    suspend fun clearAll()

    @Query("SELECT * FROM mistake_items")
    suspend fun getAllMistakesSync(): List<MistakeItem>
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSettingSync(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)

    @Query("DELETE FROM app_settings")
    suspend fun clearAll()

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettingsSync(): List<AppSetting>
}
