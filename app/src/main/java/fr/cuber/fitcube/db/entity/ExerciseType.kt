package fr.cuber.fitcube.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_types")
data class ExerciseType(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val description: String
)