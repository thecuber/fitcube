package fr.cuber.fitcube.old.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ExerciseDAO {

    @Query("SELECT * FROM exercises")
    fun getAll(): List<BaseExercise>

    @Query("SELECT * FROM workouts_exercise WHERE uid = :id")
    fun getExerciseById(id: Int): WorkoutExercise

    @Query("INSERT INTO workouts_exercise (workoutUid, exerciseUid, style, sets) VALUES (:workoutUid, :exerciseUid, :style, '[]')")
    fun createWorkoutExercise(workoutUid: Int, exerciseUid: Int, style: ExerciseStyle = ExerciseStyle.REPETITION): Long

    @Query("UPDATE workouts_exercise SET sets = :sets, style = :style WHERE uid = :uid")
    fun updateWorkoutExerciseSets(uid: Int, sets: List<Long>, style: ExerciseStyle)
}