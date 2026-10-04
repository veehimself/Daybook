package io.owlforge.daybook.data

import io.owlforge.daybook.notify.Notifier
import io.owlforge.daybook.notify.Scheduler
import io.owlforge.daybook.util.fmtTime
import io.owlforge.daybook.util.millisOn

sealed interface AddResult {
    data class Ok(val id: Long) : AddResult
    data class Error(val msg: String) : AddResult
}

class TaskRepo(
    private val dao: TaskDao,
    private val scheduler: Scheduler,
    private val notifier: Notifier,
) {
    suspend fun add(title: String, dayOffset: Int, startMin: Int, endMin: Int): AddResult {
        val name = title.trim()
        if (name.isEmpty()) return AddResult.Error("Give your task a name")
        if (endMin <= startMin) return AddResult.Error("End time must be after start time")

        val start = millisOn(dayOffset, startMin)
        val end = millisOn(dayOffset, endMin)
        if (start <= System.currentTimeMillis()) return AddResult.Error("That start time has already passed")

        // No overlaps: new task may start exactly when another ends, never inside it.
        dao.findOverlap(start, end)?.let {
            return AddResult.Error(
                "Overlaps “${it.title}” (${fmtTime(it.startMillis)} – ${fmtTime(it.endMillis)})"
            )
        }

        val task = PlanTask(title = name, startMillis = start, endMillis = end)
        val id = dao.insert(task)
        scheduler.scheduleTask(task.copy(id = id))
        return AddResult.Ok(id)
    }

    /**
     * Records the outcome. Rejected if the task hasn't ended yet (system clock + date)
     * or if it was already marked: a decision is final.
     */
    suspend fun mark(id: Long, status: Int): Boolean {
        val task = dao.get(id) ?: return false
        if (System.currentTimeMillis() < task.endMillis) return false
        if (dao.decide(id, status) == 0) return false
        notifier.cancelDone(id)
        return true
    }

    suspend fun delete(task: PlanTask) {
        dao.delete(task)
        scheduler.cancelTask(task.id)
        notifier.cancelAll(task.id)
    }
}
