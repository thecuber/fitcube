package fr.cuber.fitcube.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrainingSetDAO {
    @Query("SELECT * FROM training_sets")
    fun getAll(): List<TrainingSet>
    @Query("INSERT INTO training_sets (name, color) VALUES (:name, \"#000000\")")
    fun createTrainingSet(name: String): Long

    @Query("SELECT * FROM training_sets WHERE uid = :uid")
    fun getTrainingSet(uid: Int): TrainingSet
}