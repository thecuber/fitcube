package fr.cuber.fitcube.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.cuber.fitcube.db.entity.ExerciseType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise_types")
    fun getAllExerciseTypes(): Flow<List<ExerciseType>>

    @Insert
    suspend fun insertExerciseType(exerciseType: ExerciseType)

}