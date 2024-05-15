package fr.cuber.fitcube.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_types")
data class ExerciseType(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val description: String,
    val base: Boolean,
    val image: String
)

fun defaultExerciseType(id: Int) = ExerciseType(
    id = id,
    name = "Exercise $id",
    description = "Description for exercise $id",
    base = true,
    image = ""
)