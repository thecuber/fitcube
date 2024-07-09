package fr.cuber.fitcube.data.db

import fr.cuber.fitcube.data.db.dao.ExerciseDao
import fr.cuber.fitcube.data.db.dao.SessionDao
import fr.cuber.fitcube.data.db.dao.WorkoutDao
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val sessionDao: SessionDao

) {
    fun getExerciseTypes() = exerciseDao.getAllExerciseTypes()

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

    suspend fun createSession(session: Session) = sessionDao.createSession(session)

    suspend fun updateWorkoutStatus(id: Int, status: Boolean) = workoutDao.updateStatus(id, status)
    suspend fun updateImages(image: List<String>, id: Int) = exerciseDao.updateImages(image, id)

    suspend fun createExerciseType(exercise: ExerciseType) = exerciseDao.createExerciseType(exercise)

    suspend fun updateExerciseType(exercise: ExerciseType) = exerciseDao.updateExerciseType(exercise)

    fun getExerciseType(id: Int) = exerciseDao.getExerciseType(id).filterNotNull()

    suspend fun deleteExercises(ids: List<Int>) = exerciseDao.deleteExercises(ids)
    suspend fun deleteWorkout(workoutId: Int) = workoutDao.deleteWorkout(workoutId)

    suspend fun getWorkoutSuspend(workoutId: Int) = workoutDao.getWorkoutSuspend(workoutId)

}