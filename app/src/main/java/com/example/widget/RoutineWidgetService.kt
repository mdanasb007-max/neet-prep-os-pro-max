package com.example.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.database.NeetDefaultData
import com.example.data.entity.RoutineTask
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoutineWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RoutineRemoteViewsFactory(this.applicationContext)
    }
}

class RoutineRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<RoutineTask> = emptyList()

    override fun onCreate() {
        // Initial load
    }

    override fun onDataSetChanged() {
        // Run blocking query since onDataSetChanged runs on a binder thread pool
        runBlocking {
            try {
                val db = AppDatabase.getInstance(context)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                var loaded = db.routineDao().getTasksForDateSync(today)
                if (loaded.isEmpty()) {
                    val defaults = NeetDefaultData.getDefaultRoutineTasks(today)
                    db.routineDao().insertTasks(defaults)
                    loaded = db.routineDao().getTasksForDateSync(today)
                }
                tasks = loaded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_routine_item)
        if (position >= tasks.size) return views

        val task = tasks[position]
        views.setTextViewText(R.id.widget_item_title, "${task.blockOrder}. ${task.title}")
        views.setTextViewText(
            R.id.widget_item_details,
            "${task.targetQuestions} Qs • ${task.plannedMinutes} mins" +
                    if (task.chapter.isNotEmpty()) " • ${task.chapter}" else ""
        )

        if (task.isCompleted) {
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_check_on)
            views.setTextViewText(R.id.widget_item_status, "DONE")
            views.setTextColor(R.id.widget_item_status, 0xFF10B981.toInt())
        } else {
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_widget_check_off)
            views.setTextViewText(R.id.widget_item_status, "PENDING")
            views.setTextColor(R.id.widget_item_status, 0xFFF59E0B.toInt())
        }

        // Fill-in Intent for clicking item to toggle completion status
        val fillInIntent = Intent().apply {
            putExtras(Bundle().apply {
                putLong(RoutineWidgetProvider.EXTRA_TASK_ID, task.id)
                putBoolean(RoutineWidgetProvider.EXTRA_IS_COMPLETED, task.isCompleted)
            })
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_item_checkbox, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position < tasks.size) tasks[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
