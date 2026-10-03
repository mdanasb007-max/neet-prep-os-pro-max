package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoutineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action ?: return
        if (action == ACTION_TOGGLE_TASK) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            val currentCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false)

            if (taskId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        db.routineDao().updateTaskCompletion(taskId, !currentCompleted)

                        // Refresh all widgets
                        val manager = AppWidgetManager.getInstance(context)
                        val widgetComponent = ComponentName(context, RoutineWidgetProvider::class.java)
                        val widgetIds = manager.getAppWidgetIds(widgetComponent)

                        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_routine_list)

                        for (id in widgetIds) {
                            updateAppWidget(context, manager, id)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        } else if (action == ACTION_UPDATE_ROUTINE_WIDGET || action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, RoutineWidgetProvider::class.java)
            val widgetIds = manager.getAppWidgetIds(widgetComponent)
            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_routine_list)
            for (id in widgetIds) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.example.widget.ACTION_TOGGLE_TASK"
        const val ACTION_UPDATE_ROUTINE_WIDGET = "com.example.ACTION_UPDATE_ROUTINE_WIDGET"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_IS_COMPLETED = "extra_is_completed"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_routine_layout)

            // Intent for clicking header/open button -> Opens MainActivity
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_open_app_btn, openAppPendingIntent)

            // Bind RemoteViewsService for ListView
            val serviceIntent = Intent(context, RoutineWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_routine_list, serviceIntent)
            views.setEmptyView(R.id.widget_routine_list, R.id.widget_empty_view)

            // Template PendingIntent for list items toggling tasks directly from widget
            val toggleIntent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_TASK
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_routine_list, togglePendingIntent)

            // Update stats subtitle asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val tasks = db.routineDao().getTasksForDateSync(today)
                    val completed = tasks.count { it.isCompleted }
                    val total = tasks.size.coerceAtLeast(10)
                    val completedQuestions = tasks.filter { it.isCompleted }.sumOf { it.targetQuestions }
                    val totalQuestions = tasks.sumOf { it.targetQuestions }.coerceAtLeast(390)

                    views.setTextViewText(
                        R.id.widget_subtitle,
                        "Daily Routine: $completed/$total Done • $completedQuestions/$totalQuestions Qs"
                    )
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
