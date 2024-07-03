package fr.cuber.fitcube.workout.session

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.SessionState
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Session
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val dbRepository: RoomRepository,
    private val sessionState: SessionState
): ViewModel() {

    fun pauseAction() = sessionState.pauseAction()

    fun getState() = sessionState.listenState()

    fun setNextPrediction(prediction: List<Double>, index: Int) = sessionState.setNextPrediction(prediction, index)

    fun setCurrentPrediction(prediction: List<Double>, index: Int) = sessionState.setCurrentPrediction(prediction, index)


    fun getWorkout(workoutId: Int) = dbRepository.getWorkout(workoutId)
    fun bindWorkout(workout: WorkoutWithExercises) = sessionState.bindWorkout(workout)

    fun finishSession(context: Context) {
        val state = sessionState.getValue()
        val m = HashMap<Int, List<Double>>()
        for (e in state.workout.exercises) {
            m[e.exercise.id] = e.exercise.prediction
        }
        val session = Session(
            0,
            state.workout.workout.id,
            state.started,
            System.currentTimeMillis() - state.started,
            m
        )
        viewModelScope.launch {
            dbRepository.createSession(session)
        }
        Intent(context, WorkoutSessionService::class.java).also { intent ->
            context.startForegroundService(intent.setAction(WorkoutSessionService.Actions.STOP.toString()))
        }
    }

    fun setRest(it: Int) {
        sessionState.setRest(it)
    }


}