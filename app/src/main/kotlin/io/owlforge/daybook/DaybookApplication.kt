package io.owlforge.daybook

import android.app.Application
import androidx.room.Room
import io.owlforge.daybook.data.AppDb
import io.owlforge.daybook.data.ProfileStore
import io.owlforge.daybook.data.TaskRepo
import io.owlforge.daybook.data.TaskStatus
import io.owlforge.daybook.notify.Notifier
import io.owlforge.daybook.notify.Scheduler

class DaybookApplication : Application() {
    val db by lazy { Room.databaseBuilder(this, AppDb::class.java, "daybook.db").build() }
    val profile by lazy { ProfileStore(this) }
    val notifier by lazy { Notifier(this) }
    val scheduler by lazy { Scheduler(this) }
    val repo by lazy { TaskRepo(db.tasks(), scheduler, notifier) }

    override fun onCreate() {
        super.onCreate()
        notifier.createChannels()
    }

    suspend fun rescheduleAll() {
        val p = profile.current()
        if (p.onboarded) {
            scheduler.schedulePlan(p.planHour, p.planMinute)
            scheduler.scheduleReport()
        }
        val now = System.currentTimeMillis()
        db.tasks().upcoming(now).forEach { t ->
            scheduler.scheduleTask(t)
            // A task that was already running when the phone rebooted gets its timer back.
            if (t.startMillis <= now && t.status == TaskStatus.PENDING) notifier.showLive(t)
        }
    }
}
