package io.owlforge.daybook.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.util.nextOccurrence

class Scheduler(private val ctx: Context) {
    private val am = ctx.getSystemService(AlarmManager::class.java)

    companion object {
        const val PLAN = "io.owlforge.daybook.PLAN"
        const val REPORT = "io.owlforge.daybook.REPORT"
        const val REMIND = "io.owlforge.daybook.REMIND"
        const val START = "io.owlforge.daybook.START"
        const val END = "io.owlforge.daybook.END"
        private const val LEAD_MS = 5 * 60_000L
    }

    // The data URI makes every (action, taskId) pair a distinct PendingIntent.
    private fun pi(action: String, taskId: Long = 0): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java)
            .setAction(action)
            .setData(Uri.parse("daybook://$action/$taskId"))
            .putExtra(Notifier.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(
            ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAt(at: Long, p: PendingIntent) {
        val exact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p)
    }

    fun schedulePlan(hour: Int, minute: Int) = setAt(nextOccurrence(hour, minute), pi(PLAN))

    fun scheduleReport() = setAt(nextOccurrence(23, 55), pi(REPORT))

    fun scheduleTask(t: PlanTask) {
        val now = System.currentTimeMillis()
        if (t.startMillis - LEAD_MS > now) setAt(t.startMillis - LEAD_MS, pi(REMIND, t.id))
        if (t.startMillis > now) setAt(t.startMillis, pi(START, t.id))
        if (t.endMillis > now) setAt(t.endMillis, pi(END, t.id))
    }

    fun cancelTask(id: Long) {
        am.cancel(pi(REMIND, id))
        am.cancel(pi(START, id))
        am.cancel(pi(END, id))
    }
}
