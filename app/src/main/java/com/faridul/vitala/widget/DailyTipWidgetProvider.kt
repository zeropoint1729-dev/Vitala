package com.faridul.vitala.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.faridul.vitala.MainActivity
import com.faridul.vitala.R
import com.faridul.vitala.VitalaApplication
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DailyTipWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val app = context.applicationContext as VitalaApplication
        val pendingResult = goAsync()

        app.applicationScope.launch {
            try {
                val tipText = app.tipRepository.observeTodayTip().firstOrNull()?.text
                    ?: context.getString(R.string.widget_fallback)

                val views = RemoteViews(context.packageName, R.layout.widget_daily_tip)
                views.setTextViewText(R.id.widget_tip_text, tipText)

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(MainActivity.EXTRA_DEEP_LINK_ROUTE, "tips")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
