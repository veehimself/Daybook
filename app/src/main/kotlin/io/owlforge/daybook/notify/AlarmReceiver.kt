package io.owlforge.daybook.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.owlforge.daybook.DaybookApplication
import io.owlforge.daybook.data.TaskStatus
import io.owlforge.daybook.util.dayStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as DaybookApplication
        val pending = goAsync()
        val action = intent.action
        val taskId = intent.getLongExtra(Notifier.EXTRA_TASK_ID, -1)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val dao = app.db.tasks()
                when (action) {
                    Scheduler.PLAN -> {
                        val p = app.profile.current()
                        app.notifier.plan(p.name)
                        app.scheduler.schedulePlan(p.planHour, p.planMinute) // tomorrow's nudge
                    }
                    Scheduler.REPORT -> {
                        val list = dao.range(dayStart(0), dayStart(1))
                        app.notifier.report(list.size, list.count { it.status == TaskStatus.COMPLETED })
                        app.scheduler.scheduleReport()
                    }
                    Scheduler.REMIND -> dao.get(taskId)?.let { app.notifier.reminder(it) }
                    Scheduler.START -> dao.get(taskId)?.let { app.notifier.showLive(it) }
                    Scheduler.END -> {
                        app.notifier.cancelLive(taskId)
                        dao.get(taskId)?.let { if (it.status == TaskStatus.PENDING) app.notifier.done(it) }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
