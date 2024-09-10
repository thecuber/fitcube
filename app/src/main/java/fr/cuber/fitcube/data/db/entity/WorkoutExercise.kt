package fr.cuber.fitcube.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeId: Int,
    val workoutId: Int,
    val mode: WorkoutMode,
    val prediction: List<Double>,
    @ColumnInfo(defaultValue = "true") val enabled: Boolean = true
)

fun defaultWorkoutExercise(id: Int) = WorkoutExercise(
    id = id,
    typeId = id,
    workoutId = 0,
    mode = WorkoutMode.REPETITION,
    prediction = List(4) { 25.0 },
    enabled = true
)

enum class WorkoutMode {
    REPETITION,
    TIMED
}