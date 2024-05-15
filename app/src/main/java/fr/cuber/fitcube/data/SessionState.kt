package fr.cuber.fitcube.data

import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises
import fr.cuber.fitcube.workout.session.SessionStatus
import fr.cuber.fitcube.workout.session.SessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface SessionState {

    fun start()
    fun getState(): StateFlow<SessionUiState>
    fun bindWorkout(workout: WorkoutWithExercises)

    fun startRest(timer: Int)

    fun timerTick(): Boolean

}

class SessionStateImpl @Inject constructor() : SessionState {

    private val _uiState = MutableStateFlow(SessionUiState(SessionStatus.REST, 0, 0, 0, defaultWorkoutWithExercises(0), 0L))
    private val state: StateFlow<SessionUiState> = _uiState

    override fun start() {
        _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE, started = System.currentTimeMillis())
    }

    override fun getState() = state

    override fun bindWorkout(workout: WorkoutWithExercises) {
        _uiState.value = _uiState.value.copy(workout = workout)
    }

    override fun startRest(timer: Int) {
        _uiState.value = _uiState.value.copy(status = SessionStatus.REST, restTimer = timer)
    }

    override fun timerTick(): Boolean {
        _uiState.value = _uiState.value.copy(restTimer = _uiState.value.restTimer - 1)
        if(_uiState.value.restTimer == 0) {
            _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
            return false
        }
        return true
    }

}