package fr.cuber.fitcube.data.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.defaultWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT workouts.*, AVG(sessions.duration) as estimated, MAX(sessions.date) as date, COUNT(DISTINCT workout_exercises.id) as exerciseCount FROM workouts " +
            "LEFT JOIN sessions ON sessions.workoutId = workouts.id " +
            "LEFT JOIN workout_exercises ON workout_exercises.workoutId = workouts.id " +
            "GROUP BY workouts.id")
    fun getAllWorkouts(): Flow<List<HomeWorkout>>

    @Insert
    suspend fun createWorkout(workout: Workout)

    @Transaction
    @Query("SELECT * FROM workouts WHERE workouts.id = :workoutId")
    fun getWorkout(workoutId: Int): Flow<WorkoutWithExercises>

    @Query("UPDATE workouts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Boolean)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Int)

    @Transaction
    @Query("SELECT * FROM workouts WHERE workouts.id = :workoutId")
    suspend fun getWorkoutSuspend(workoutId: Int): WorkoutWithExercises

    @Query("UPDATE workouts SET `order` = :order WHERE id = :workoutId")
    suspend fun moveOrder(order: List<Int>, workoutId: Int)

}



data class HomeWorkout(
    @Embedded val workout: Workout,
    val date: Long?,
    val estimated: Long,
    val exerciseCount: Int,
)

fun defaultHomeWorkout(id: Int) = HomeWorkout(
    workout = defaultWorkout(id),
    date = System.currentTimeMillis() - id * (3600 * 1000 * 24),
    estimated = 1000 * (83),
    exerciseCount = 3
)

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = WorkoutExercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<FullExercise>
)

fun defaultWorkoutWithExercises(size: Int, id: Int = 0) = WorkoutWithExercises(
    workout = defaultWorkout(id),
    exercises = List(size) {
        defaultFullExercise(it)
    }
)