package fr.cuber.fitcube.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(defaultValue = "0") val status: Boolean,//If the workout can be started
    @ColumnInfo(defaultValue = "105") val rest: Int = 105,//The rest timer for this workout
    @ColumnInfo(defaultValue = "1") val sound: Boolean = true,//If the sound is enabled for this workout
    @ColumnInfo(defaultValue = "3") val warmup: Int = 3,
    @ColumnInfo(defaultValue = "'[]'") val order: List<Int> = emptyList()
)

fun defaultWorkout(id: Int = 1, status: Boolean = true) = Workout(id, "Default Workout $id", status, 105, true)

fun Workout.startWarmup(): Boolean {
    return (this.warmup and 1) > 0
}

fun Workout.endWarmup(): Boolean {
    return (this.warmup and 2) > 0
}

