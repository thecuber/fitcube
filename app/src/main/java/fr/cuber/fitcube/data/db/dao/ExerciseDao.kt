package fr.cuber.fitcube.data.db.dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.defaultWorkoutExercise
import fr.cuber.fitcube.utils.STRETCHING_ID
import fr.cuber.fitcube.utils.WARMUP_ID
import fr.cuber.fitcube.utils.getWarmupTime
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise_types")
    fun getAllExerciseTypes(): Flow<List<ExerciseType>>

    @Insert
    suspend fun insertExerciseType(exerciseType: ExerciseType)

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    fun getWorkoutExercise(id: Int): Flow<FullExercise>

    @Upsert
    suspend fun saveWorkoutExercise(ex: WorkoutExercise)

    @Delete
    suspend fun deleteWorkoutExercise(ex: WorkoutExercise)

    @Insert
    suspend fun createWorkoutExercise(ex: WorkoutExercise): Long

    @Query("SELECT * FROM exercise_types WHERE id = :id")
    fun getExerciseType(id: Int): Flow<ExerciseType?>

    @Query("UPDATE exercise_types SET image = :image WHERE id = :id")
    suspend fun updateImages(image: List<String>, id: Int)

    @Insert
    suspend fun createExerciseType(workout: ExerciseType): Long

    @Update
    suspend fun updateExerciseType(workout: ExerciseType)

    @Query("DELETE FROM workout_exercises WHERE id IN (:ids)")
    suspend fun deleteExercises(ids: List<Int>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteWorkout(workoutId: Int)

    @Query("UPDATE workout_exercises SET enabled = :archived WHERE id = :id")
    suspend fun archiveExercise(id: Int, archived: Boolean)
}

data class FullExercise(
    @Embedded val exercise: WorkoutExercise,
    @Relation(
        parentColumn = "typeId",
        entityColumn = "id"
    )
    val type: ExerciseType
)

fun defaultFullExercise(id: Int) = FullExercise(
    defaultWorkoutExercise(id),
    defaultExerciseType(id)
)

fun FullExercise.isWarmup() = (this.exercise.id == WARMUP_ID) || (this.exercise.id  == STRETCHING_ID)

fun warmupExercise(start: Boolean, context: Context) = FullExercise(
    defaultWorkoutExercise(if(start) WARMUP_ID else STRETCHING_ID).copy(prediction = listOf(context.getWarmupTime()), mode = WorkoutMode.TIMED),
    type = defaultExerciseType(if(start) WARMUP_ID else STRETCHING_ID).copy(name = if(start) { "Warmup" } else { "Stretching" }, description = "", image = listOf("png/warmup.png"))
)