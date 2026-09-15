package com.faridul.vitala.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyTipWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // TODO: pick today's tip deterministically from TipRepository (date-seeded index)
        val tipText = "Add 10 minutes of morning stretching to improve circulation."
        postNotification(tipText)
        return Result.success()
    }

    private fun postNotification(text: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily health tip",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: replace with a real app icon
            .setContentTitle("Today's health tip")
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "daily_tip_channel"
        const val NOTIFICATION_ID = 1001
    }
}
