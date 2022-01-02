package com.udacity.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.Constants
import com.udacity.DetailActivity
import com.udacity.R

fun NotificationManager.sendNotification(
    id: Long?,
    messageBody: String,
    applicationContext: Context
) {
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.putExtra(Constants.EXTRA_DOWNLOAD_ID, id)
    val notificationId = id?.toInt() ?: 0

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        notificationId,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(
            0,
            applicationContext.getString(R.string.detail),
            contentPendingIntent
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    notify(notificationId, builder.build())

}

fun NotificationManager.cancelNotification(id: Long?) {
    val notificationId = id?.toInt() ?: 0
    cancel(notificationId)
}