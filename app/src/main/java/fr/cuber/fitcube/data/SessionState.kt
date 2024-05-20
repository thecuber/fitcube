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
    fun listenState(): StateFlow<SessionUiState>

    fun getValue(): SessionUiState
    fun bindWorkout(workout: WorkoutWithExercises)

    fun startRest(timer: Int)

    fun timerTick(): Boolean

    fun postRest()

    fun onEnd(): MutableStateFlow<Boolean>

}

class SessionStateImpl @Inject constructor() : SessionState {

    private val _uiState = MutableStateFlow(SessionUiState(SessionStatus.WAITING, 0, 0, 0, defaultWorkoutWithExercises(0), 0L))
    private val state: StateFlow<SessionUiState> = _uiState

    private val endTrigger = MutableStateFlow(false)

    override fun start() {
        _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE, started = System.currentTimeMillis())
    }

    override fun listenState() = state

    override fun getValue() = state.value

    override fun bindWorkout(workout: WorkoutWithExercises) {
        _uiState.value = _uiState.value.copy(workout = workout)
    }

    override fun startRest(timer: Int) {
        _uiState.value = _uiState.value.copy(status = SessionStatus.REST, restTimer = timer)
    }

    override fun timerTick(): Boolean {
        _uiState.value = _uiState.value.copy(restTimer = _uiState.value.restTimer - 1)
        if(_uiState.value.restTimer == 0) {
            postRest()
            return false
        }
        return true
    }

    override fun postRest() {
        var set = getValue().currentSet + 1
        var ex = getValue().currentExercise
        if (set == getValue().workout.exercises[getValue().currentExercise].exercise.prediction.size) {
            set = 0
            ex += 1
            if (ex == getValue().workout.exercises.size) {
                _uiState.value = _uiState.value.copy(status = SessionStatus.DONE)
                endTrigger.value = true
                return
            }
        }
        _uiState.value = _uiState.value.copy(currentExercise = ex, currentSet = set)
    }

    override fun onEnd() = endTrigger

}