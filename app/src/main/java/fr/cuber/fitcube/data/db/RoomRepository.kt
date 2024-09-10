package fr.cuber.fitcube.data.db

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import fr.cuber.fitcube.data.db.dao.ExerciseDao
import fr.cuber.fitcube.data.db.dao.SessionDao
import fr.cuber.fitcube.data.db.dao.WorkoutDao
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.utils.LoadingFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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

    fun getWorkout(workoutId: Int) = workoutDao.getWorkout(workoutId).map { t -> t.copy(workout = t.workout.copy(order = t.workout.order.ifEmpty { t.exercises.map { it.exercise.id } })) }

    suspend fun getWorkoutExercise(id: Int) = exerciseDao.getWorkoutExercise(id)


    suspend fun saveWorkoutExercise(ex: WorkoutExercise) = exerciseDao.saveWorkoutExercise(ex)
    suspend fun deleteWorkoutExercise(ex: WorkoutExercise) = exerciseDao.deleteWorkoutExercise(ex)
    suspend fun createWorkoutExercise(ex: WorkoutExercise) = exerciseDao.createWorkoutExercise(ex)

    fun getSessions(id: Int) = sessionDao.getSessionsByWorkoutId(id)

    fun getSessions() = sessionDao.getSessions()

    suspend fun createSession(session: Session) = sessionDao.createSession(session)

    suspend fun updateWorkoutStatus(id: Int, status: Boolean) = workoutDao.updateStatus(id, status)
    suspend fun updateImages(image: List<String>, id: Int) = exerciseDao.updateImages(image, id)

    suspend fun createExerciseType(exercise: ExerciseType) = exerciseDao.createExerciseType(exercise)

    suspend fun updateExerciseType(exercise: ExerciseType) = exerciseDao.updateExerciseType(exercise)

    fun getExerciseType(id: Int) = exerciseDao.getExerciseType(id).filterNotNull()

    suspend fun deleteExercises(ids: List<Int>) = exerciseDao.deleteExercises(ids)
    suspend fun deleteWorkout(workoutId: Int) {
        workoutDao.deleteWorkout(workoutId)
        exerciseDao.deleteWorkout(workoutId)
        sessionDao.deleteSession(workoutId)
    }

    suspend fun getWorkoutSuspend(workoutId: Int) = workoutDao.getWorkoutSuspend(workoutId)
    suspend fun updateWorkoutWarmup(workoutId: Int, it: Int) = workoutDao.updateWarmup(workoutId, it)
    suspend fun moveOrder(order: List<Int>, workoutId: Int) = workoutDao.moveOrder(order, workoutId)

}

fun <T> Flow<T>.success() = this.map { LoadingFlow.Success(it) }

@Composable fun <T> Flow<LoadingFlow<T>>?.loadingCollect() = this?.collectAsState(initial = LoadingFlow.Loading) ?: MutableStateFlow(LoadingFlow.Loading).collectAsState()
