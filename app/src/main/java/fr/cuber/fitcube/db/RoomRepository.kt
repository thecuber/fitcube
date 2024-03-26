package fr.cuber.fitcube.db

import fr.cuber.fitcube.db.dao.ExerciseDao
import fr.cuber.fitcube.db.dao.WorkoutDao
import fr.cuber.fitcube.db.entity.ExerciseType
import fr.cuber.fitcube.db.entity.Workout
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
){
    fun getExerciseTypes() = exerciseDao.getAllExerciseTypes()
    suspend fun insertExerciseType(name: String, description: String) = exerciseDao.insertExerciseType(
        ExerciseType(0, name, description)
    )

    fun getWorkouts() = workoutDao.getAllWorkouts()

    suspend fun addWorkout(workout: Workout) = workoutDao.createWorkout(
        workout
    )
}