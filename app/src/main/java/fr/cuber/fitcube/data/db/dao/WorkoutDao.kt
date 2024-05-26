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

    @Query("SELECT workouts.* FROM workouts LEFT JOIN sessions ON workouts.id = sessions.workoutId AND sessions.date = (SELECT MAX(DATE) FROM SESSIONS AS S WHERE S.workoutId = workouts.id)")
    fun getAllWorkouts(): Flow<List<HomeWorkout>>

    @Insert
    suspend fun createWorkout(workout: Workout)

    @Transaction
    @Query("SELECT * FROM workouts WHERE workouts.id = :workoutId")
    fun getWorkout(workoutId: Int): Flow<WorkoutWithExercises>

}


data class HomeWorkout(
    @Embedded val workout: Workout,
    val date: Long?
)

fun defaultHomeWorkout(id: Int) = HomeWorkout(
    workout = defaultWorkout(id),
    date = System.currentTimeMillis()
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

fun defaultWorkoutWithExercises(size: Int) = WorkoutWithExercises(
    workout = defaultWorkout(),
    exercises = List(size) {
        defaultFullExercise(it + 1)
    }
)