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

    fun timerTick()

    /**
     * Calls the rest and update it
     */
    fun restUpdate()

    fun onEnd(): MutableStateFlow<Boolean>
    fun setNextPrediction(prediction: List<Double>, index: Int)

    fun setCurrentPrediction(prediction: List<Double>, index: Int)
    fun pauseAction()
    fun setRest(it: Int)

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

    override fun timerTick() {
        _uiState.value = _uiState.value.copy(elapsedTime = _uiState.value.elapsedTime + 1000)
        if (_uiState.value.paused || _uiState.value.status == SessionStatus.EXERCISE) return
        if(_uiState.value.bufferTimer) {
            _uiState.value = _uiState.value.copy(bufferTimer = false)
            return
        }
        _uiState.value = _uiState.value.copy(timer = _uiState.value.timer - 1)
        if (_uiState.value.timer == 0) {
            if (_uiState.value.status == SessionStatus.START) {
                _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE, started = System.currentTimeMillis(), elapsedTime = 0)
            } else {
                _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
            }
        }
    }


    override fun restUpdate() {
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
        _uiState.value = _uiState.value.copy(
            currentExercise = ex,
            currentSet = set
        )
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

    override fun pauseAction() {
        if (_uiState.value.status == SessionStatus.EXERCISE) {
            //This can be a rest
            _uiState.value = _uiState.value.copy(timer = _uiState.value.rest, status = SessionStatus.REST, bufferTimer = true)
            restUpdate()
        } else {
            _uiState.value = _uiState.value.copy(paused = _uiState.value.paused.not())
        }
    }

    override fun setRest(it: Int) {
        _uiState.value = _uiState.value.copy(rest = it)
    }

}