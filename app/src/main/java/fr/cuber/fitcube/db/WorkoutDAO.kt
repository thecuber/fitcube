package fr.cuber.fitcube.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface WorkoutDAO {
    @Query("SELECT * FROM workout_days")
    fun getAll(): List<WorkoutDay>
    @Query("INSERT INTO workout_days (name, color) VALUES (:name, \"#000000\")")
    fun createWorkoutDay(name: String): Long

    @Query("SELECT * FROM workout_days WHERE uid = :uid")
    fun getTrainingSet(uid: Int): WorkoutDay
}