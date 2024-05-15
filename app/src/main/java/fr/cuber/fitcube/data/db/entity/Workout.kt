package fr.cuber.fitcube.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String
)

fun defaultWorkout(id: Int = 1) = Workout(id, "Default Workout $id")