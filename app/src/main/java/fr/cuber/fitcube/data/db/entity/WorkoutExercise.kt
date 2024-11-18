package fr.cuber.fitcube.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeId: Int,//The id of the type to link to the other database
    val workoutId: Int,//The id of the workout linked to
    val mode: WorkoutMode,//The mode: repetition with or without weights, or a timed series
    val prediction: List<Double>,//The prediction for each series
    @ColumnInfo(defaultValue = "true") val enabled: Boolean = true//If the exercise is being used or archived
)

fun defaultWorkoutExercise(id: Int) = WorkoutExercise(
    id = id,
    typeId = id,
    workoutId = 0,
    mode = WorkoutMode.LOADED_REPETITION,
    prediction = List(4) { id * 10.0 },
    enabled = true
)

enum class WorkoutMode {
    LOADED_REPETITION,//The weights for each series
    TIMED,//The duration of each series
    UNLOADED_REPETITION//The number of repetitions for each series
}