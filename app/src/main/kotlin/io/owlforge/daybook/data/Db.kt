package io.owlforge.daybook.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

object TaskStatus {
    const val PENDING = 0
    const val COMPLETED = 1
    const val INCOMPLETE = 2
}

@Entity(tableName = "tasks", indices = [Index("startMillis")])
data class PlanTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val status: Int = TaskStatus.PENDING,
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE startMillis >= :from AND startMillis < :to ORDER BY startMillis")
    fun observeRange(from: Long, to: Long): Flow<List<PlanTask>>

    @Query("SELECT * FROM tasks WHERE startMillis >= :from AND startMillis < :to ORDER BY startMillis")
    suspend fun range(from: Long, to: Long): List<PlanTask>

    @Query("SELECT * FROM tasks ORDER BY startMillis")
    fun observeAll(): Flow<List<PlanTask>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: Long): Flow<PlanTask?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): PlanTask?

    /** Any task whose [start, end) window intersects the given window. Touching edges are allowed. */
    @Query("SELECT * FROM tasks WHERE startMillis < :end AND endMillis > :start LIMIT 1")
    suspend fun findOverlap(start: Long, end: Long): PlanTask?

    @Query("SELECT * FROM tasks WHERE endMillis > :now")
    suspend fun upcoming(now: Long): List<PlanTask>

    @Insert
    suspend fun insert(task: PlanTask): Long

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: Int)

    @Delete
    suspend fun delete(task: PlanTask)
}

@Database(entities = [PlanTask::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun tasks(): TaskDao
}
