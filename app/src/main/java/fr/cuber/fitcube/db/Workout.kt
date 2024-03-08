package fr.cuber.fitcube.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workout_days")
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val color: String,
    @ColumnInfo val exercises: List<Int>
)

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val name: Date,
    @ColumnInfo val duration: Int,
    @ColumnInfo val workoutUid: Int,
    @ColumnInfo val exercises: Map<Int, ExerciseHistory>
)
