package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        RoutineTask::class,
        DailyPlan::class,
        DailyPlanTopic::class,
        StudySession::class,
        PomodoroSession::class,
        QuestionRecord::class,
        SyllabusTopic::class,
        RevisionItem::class,
        MistakeItem::class,
        AppSetting::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun studyDao(): StudyDao
    abstract fun questionDao(): QuestionDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun revisionDao(): RevisionDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neet_prep_os.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.populateInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Ensure default syllabus topics are inserted if none exist
        if (syllabusDao().getTopicCountSync() == 0) {
            val topics = NeetDefaultData.getInitialSyllabusTopics()
            syllabusDao().insertTopics(topics)
        }

        // Ensure today's routine blocks exist
        if (routineDao().countTasksForDate(today) == 0) {
            val defaultTasks = NeetDefaultData.getDefaultRoutineTasks(today)
            routineDao().insertTasks(defaultTasks)
        }
    }
}
