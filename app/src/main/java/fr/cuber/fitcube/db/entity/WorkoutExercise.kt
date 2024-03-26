package fr.cuber.fitcube.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_exercises")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val typeId: Int,

)
