package fr.cuber.fitcube.data.db

import fr.cuber.fitcube.data.db.dao.ExerciseDao
import fr.cuber.fitcube.data.db.dao.SessionDao
import fr.cuber.fitcube.data.db.dao.WorkoutDao
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val sessionDao: SessionDao

){
    fun getExerciseTypes() = exerciseDao.getAllExerciseTypes()
    suspend fun insertExerciseType(name: String, description: String) = exerciseDao.insertExerciseType(
        ExerciseType(0, name, description, true, "")
    )

    fun getWorkouts() = workoutDao.getAllWorkouts()

    suspend fun addWorkout(workout: Workout) = workoutDao.createWorkout(
        workout
    )

    fun getWorkout(workoutId: Int) = workoutDao.getWorkout(workoutId)

    suspend fun getWorkoutExercise(id: Int) = exerciseDao.getWorkoutExercise(id)


    suspend fun saveWorkoutExercise(ex: WorkoutExercise) = exerciseDao.saveWorkoutExercise(ex)
    suspend fun deleteWorkoutExercise(ex: WorkoutExercise) = exerciseDao.deleteWorkoutExercise(ex)
    suspend fun createWorkoutExercise(ex: WorkoutExercise) = exerciseDao.createWorkoutExercise(ex)

    fun getSessions(id: Int) = sessionDao.getSessionsByWorkoutId(id)

}