package fr.cuber.fitcube.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val status: Boolean
)

fun defaultWorkout(id: Int = 1, status: Boolean = true) = Workout(id, "Default Workout $id", status)