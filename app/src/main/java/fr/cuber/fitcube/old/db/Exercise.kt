package fr.cuber.fitcube.old.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class BaseExercise (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val description: String
)

enum class ExerciseStyle {
    REPETITION,
    TIME
}

@Entity(tableName = "workouts_exercise")
data class WorkoutExercise (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val workoutUid: Int,
    @ColumnInfo val style: ExerciseStyle,
    @ColumnInfo val exerciseUid: Int,
    @ColumnInfo val sets: List<Long>
)
