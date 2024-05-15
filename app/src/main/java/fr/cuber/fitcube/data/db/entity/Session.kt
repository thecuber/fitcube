package fr.cuber.fitcube.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val workoutId: Int,
    val date: Long,
    val duration: Long,
    val exercises: Map<Int, List<Double>>
)

fun defaultSession() = Session(0, 0, System.currentTimeMillis(), 1000 * 60 * Random.nextLong(45, 90), emptyMap())