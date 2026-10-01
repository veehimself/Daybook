package io.owlforge.daybook.notify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.owlforge.daybook.MainActivity
import io.owlforge.daybook.R
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.util.fmtTime

@SuppressLint("MissingPermission")
class Notifier(private val ctx: Context) {
    private val nm = NotificationManagerCompat.from(ctx)

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_OPEN = "open"

        const val CH_PLAN = "plan"
        const val CH_REMIND = "remind"
        const val CH_LIVE = "live"
        const val CH_DONE = "done"
        const val CH_REPORT = "report"

        private const val ID_PLAN = 1
        private const val ID_REPORT = 2
        private fun id(taskId: Long, kind: Int) = (taskId % 100_000).toInt() * 10 + kind + 100
        private fun remindId(t: Long) = id(t, 1)
        private fun liveId(t: Long) = id(t, 2)
        private fun doneId(t: Long) = id(t, 3)
    }

    fun createChannels() {
        val sys = ctx.getSystemService(NotificationManager::class.java)
        sys.createNotificationChannels(
            listOf(
                NotificationChannel(CH_PLAN, "Plan tomorrow", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Daily nudge to plan your next day" },
                NotificationChannel(CH_REMIND, "Task reminders", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Heads-up 5 minutes before a task starts" },
                NotificationChannel(CH_LIVE, "Live task timer", NotificationManager.IMPORTANCE_LOW)
                    .apply {
                        description = "Countdown while a task is running"
                        setShowBadge(false)
                    },
                NotificationChannel(CH_DONE, "Task check-in", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Asks whether you finished the task" },
                NotificationChannel(CH_REPORT, "Daily report", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "End-of-day summary" },
            )
        )
    }

    private fun open(reqCode: Int, what: String, taskId: Long = -1): PendingIntent {
        val i = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN, what)
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getActivity(
            ctx, reqCode, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun base(channel: String) = NotificationCompat.Builder(ctx, channel)
        .setSmallIcon(R.drawable.ic_stat_daybook)
        .setColor(0xFF7C5CFF.toInt())
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    private fun post(id: Int, n: android.app.Notification) {
        if (nm.areNotificationsEnabled()) nm.notify(id, n)
    }

    fun plan(name: String) {
        val title = if (name.isBlank()) "Plan your tomorrow" else "$name, plan your tomorrow"
        post(
            ID_PLAN,
            base(CH_PLAN)
                .setContentTitle(title)
                .setContentText("Take two minutes to line up tomorrow's tasks.")
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(open(ID_PLAN, "plan"))
                .build()
        )
    }

    fun reminder(t: PlanTask) = post(
        remindId(t.id),
        base(CH_REMIND)
            .setContentTitle("Starting in 5 minutes")
            .setContentText("${t.title} · ${fmtTime(t.startMillis)}")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setTimeoutAfter(5 * 60_000L)
            .setContentIntent(open(remindId(t.id), "task", t.id))
            .build()
    )

    /** Ongoing count-down. Chronometer runs on the system side, so it survives the app being killed. */
    fun showLive(t: PlanTask) {
        val left = (t.endMillis - System.currentTimeMillis()).coerceAtLeast(1_000)
        val extras = Bundle().apply { putBoolean("android.requestPromotedOngoing", true) } // Android 16 Live Updates
        val n = base(CH_LIVE)
            .setContentTitle(t.title)
            .setContentText("In progress · ends ${fmtTime(t.endMillis)}")
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(t.endMillis)
            .setTimeoutAfter(left + 2_000)
            .addExtras(extras)
            .setContentIntent(open(liveId(t.id), "task", t.id))
            .build()
        post(liveId(t.id), n)
    }

    fun done(t: PlanTask) = post(
        doneId(t.id),
        base(CH_DONE)
            .setContentTitle("How did it go?")
            .setContentText("Did you finish “${t.title}”?")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open(doneId(t.id), "task", t.id))
            .build()
    )

    fun report(total: Int, completed: Int) = post(
        ID_REPORT,
        base(CH_REPORT)
            .setContentTitle("Your day: $completed of $total done")
            .setContentText(if (total == 0) "No tasks today." else "Open your journal to see the full report.")
            .setContentIntent(open(ID_REPORT, "journal"))
            .build()
    )

    fun cancelLive(taskId: Long) = nm.cancel(liveId(taskId))
    fun cancelDone(taskId: Long) = nm.cancel(doneId(taskId))
    fun cancelAll(taskId: Long) {
        nm.cancel(remindId(taskId)); nm.cancel(liveId(taskId)); nm.cancel(doneId(taskId))
    }
}
