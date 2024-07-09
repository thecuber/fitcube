package fr.cuber.fitcube.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.defaultWorkoutExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise_types")
    fun getAllExerciseTypes(): Flow<List<ExerciseType>>

    @Insert
    suspend fun insertExerciseType(exerciseType: ExerciseType)

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getWorkoutExercise(id: Int): FullExercise

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
    defaultWorkoutExercise(),
    defaultExerciseType(id)
)