package io.owlforge.daybook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.owlforge.daybook.DaybookApplication
import io.owlforge.daybook.data.AddResult
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.data.Profile
import io.owlforge.daybook.data.TaskStatus
import io.owlforge.daybook.util.dayStart
import io.owlforge.daybook.util.zone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class DayReport(val date: LocalDate, val tasks: List<PlanTask>) {
    val total get() = tasks.size
    val done get() = tasks.count { it.status == TaskStatus.COMPLETED }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as DaybookApplication
    private val dao = a.db.tasks()
    private val eager = SharingStarted.Eagerly
    private val lazy = SharingStarted.WhileSubscribed(5_000)

    /** null while DataStore is loading, so we never flash onboarding at a returning user. */
    val profile: StateFlow<Profile?> =
        a.profile.flow.map<Profile, Profile?> { it }.stateIn(viewModelScope, eager, null)

    val tab = MutableStateFlow(0)           // 0 plan · 1 journal · 2 profile
    val dayOffset = MutableStateFlow(0)     // 0 today · 1 tomorrow
    val showAdd = MutableStateFlow(false)
    val openTaskId = MutableStateFlow<Long?>(null)

    val tasks: StateFlow<List<PlanTask>> = dayOffset
        .flatMapLatest { o -> dao.observeRange(dayStart(o), dayStart(o + 1)) }
        .stateIn(viewModelScope, lazy, emptyList())

    val openTask: StateFlow<PlanTask?> = openTaskId
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.observe(id) }
        .stateIn(viewModelScope, lazy, null)

    val reports: StateFlow<List<DayReport>> = dao.observeAll()
        .map { all ->
            all.groupBy { Instant.ofEpochMilli(it.startMillis).atZone(zone).toLocalDate() }
                .map { (d, l) -> DayReport(d, l.sortedBy { it.startMillis }) }
                .sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, lazy, emptyList())

    /** Deep-links coming from notifications. */
    fun handle(open: String?, taskId: Long) {
        when (open) {
            "plan" -> { tab.value = 0; dayOffset.value = 1; showAdd.value = true }
            "journal" -> tab.value = 1
            "task" -> if (taskId > 0) { tab.value = 0; openTaskId.value = taskId }
        }
    }

    fun saveProfile(name: String, hour: Int, minute: Int) = viewModelScope.launch {
        a.profile.save(name.trim(), hour, minute)
        a.scheduler.schedulePlan(hour, minute)
        a.scheduler.scheduleReport()
    }

    fun addTask(title: String, startMin: Int, endMin: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val r = a.repo.add(title, dayOffset.value, startMin, endMin)) {
                is AddResult.Ok -> { showAdd.value = false; onResult(null) }
                is AddResult.Error -> onResult(r.msg)
            }
        }
    }

    fun mark(id: Long, status: Int) = viewModelScope.launch { a.repo.mark(id, status) }
    fun delete(t: PlanTask) = viewModelScope.launch { a.repo.delete(t) }
}
