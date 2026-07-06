package com.clairjour.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {

    private const val WORK_PLEDGE = "clairjour_pledge_reminder"
    private const val WORK_JOURNAL = "clairjour_journal_reminder"

    fun schedulePledge(context: Context, hour: Int, minute: Int) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_PLEDGE,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<PledgeReminderWorker>(Duration.ofDays(1))
                .setInitialDelay(delayUntil(hour, minute))
                .build()
        )
    }

    fun scheduleJournal(context: Context, hour: Int, minute: Int) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_JOURNAL,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<JournalReminderWorker>(Duration.ofDays(1))
                .setInitialDelay(delayUntil(hour, minute))
                .build()
        )
    }

    fun schedule(context: Context, pledgeHour: Int, pledgeMinute: Int, journalHour: Int, journalMinute: Int) {
        schedulePledge(context, pledgeHour, pledgeMinute)
        scheduleJournal(context, journalHour, journalMinute)
    }

    fun cancelPledge(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PLEDGE)
    }

    fun cancelJournal(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_JOURNAL)
    }

    private fun delayUntil(hour: Int, minute: Int): Duration {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var target = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, minute))
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target)
    }
}
