package fr.cuber.fitcube.data

import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.workout.session.SessionStatus
import fr.cuber.fitcube.workout.session.SessionUiState
import fr.cuber.fitcube.workout.session.defaultSessionUiState
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
    fun setNextPrediction(prediction: List<Double>, index: Int)

    fun setCurrentPrediction(prediction: List<Double>, index: Int)

}

class SessionStateImpl @Inject constructor() : SessionState {

    private val _uiState = MutableStateFlow(defaultSessionUiState(0))
    private val state: StateFlow<SessionUiState> = _uiState

    private val endTrigger = MutableStateFlow(false)

    override fun start() {
        _uiState.value = _uiState.value.copy(
            status = SessionStatus.EXERCISE,
            started = System.currentTimeMillis()
        )
    }

    override fun listenState() = state

    override fun getValue() = state.value

    override fun bindWorkout(workout: WorkoutWithExercises) {
        _uiState.value = _uiState.value.copy(
            workout = workout,
            predictions = workout.exercises.map { it.exercise.prediction })
    }

    override fun startRest(timer: Int) {
        _uiState.value = _uiState.value.copy(status = SessionStatus.REST, timer = timer)
    }

    override fun timerTick(): Boolean {
        _uiState.value = _uiState.value.copy(timer = _uiState.value.timer - 1)
        if (_uiState.value.timer == 0) {
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
        _uiState.value = _uiState.value.copy(currentExercise = ex, currentSet = set, status = SessionStatus.EXERCISE)
    }

    override fun onEnd() = endTrigger
    override fun setNextPrediction(prediction: List<Double>, index: Int) {
        val predictions = _uiState.value.predictions.toMutableList()
        predictions[index] = prediction
        _uiState.value = _uiState.value.copy(predictions = predictions)
    }

    override fun setCurrentPrediction(prediction: List<Double>, index: Int) {
        _uiState.value =
            _uiState.value.copy(workout = _uiState.value.workout.copy(exercises = _uiState.value.workout.exercises.mapIndexed { i, fullExercise ->
                if (index == i) {
                    fullExercise.copy(exercise = fullExercise.exercise.copy(prediction = prediction))
                } else {
                    fullExercise
                }
            }))
    }

}