package fr.cuber.fitcube.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val typeId: Int,
    val workoutId: Int,
    val mode: WorkoutMode,
    val prediction: List<Double>
)

fun defaultWorkoutExercise() = WorkoutExercise(
    id = 0,
    typeId = 0,
    workoutId = 0,
    mode = WorkoutMode.REPETITION,
    prediction = List(4) { 25.0 }
)

enum class WorkoutMode {
    REPETITION,
    TIMED
}
