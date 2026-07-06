package com.clairjour.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.clairjour.app.R

object NotificationChannels {
    const val PLEDGE = "daily_pledge"
    const val JOURNAL = "journal_reminder"
    const val MILESTONE = "milestone"

    fun register(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return
        listOf(
            NotificationChannel(
                PLEDGE,
                context.getString(R.string.notif_channel_pledge_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_pledge_desc)
            },
            NotificationChannel(
                JOURNAL,
                context.getString(R.string.notif_channel_journal_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_journal_desc)
            },
            NotificationChannel(
                MILESTONE,
                context.getString(R.string.notif_channel_milestone_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_milestone_desc)
            }
        ).forEach { nm.createNotificationChannel(it) }
    }
}
