package fr.cuber.fitcube.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.cuber.fitcube.db.entity.ExerciseType
import fr.cuber.fitcube.db.entity.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Insert
    suspend fun createWorkout(workout: Workout)

}