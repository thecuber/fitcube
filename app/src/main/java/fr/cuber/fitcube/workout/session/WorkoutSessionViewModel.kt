package fr.cuber.fitcube.workout.session

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.success
import fr.cuber.fitcube.utils.LoadingFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val dbRepository: RoomRepository
) : ViewModel() {

    fun pauseAction() = binder.pauseAction()

    var state: Flow<LoadingFlow<SessionState>> = MutableStateFlow(LoadingFlow.Loading)

    fun setNextPrediction(prediction: List<Double>, index: Int) =
        binder.setNextPrediction(prediction, index)

    fun setCurrentPrediction(prediction: List<Double>, index: Int) =
        binder.setCurrentPrediction(prediction, index)

    fun finishSession() {
        val state = binder.getValue()
        val m = HashMap<Int, List<Double>>()
        for ((i, e) in state.workout.exercises.withIndex()) {
            m[e.exercise.id] = e.exercise.prediction
            viewModelScope.launch {
                dbRepository.saveWorkoutExercise(e.exercise.copy(prediction = state.predictions[i]))
            }
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
        closeService()
    }

    fun setRest(it: Int) {
        binder.setRest(it)
    }

    fun closeService() {
        state = MutableStateFlow(LoadingFlow.Loading)
        context.unbindService(connection)
        binder.finishService()
        closeScreen()//To change the screen
    }

    lateinit var closeScreen: () -> Unit

    var trigger = MutableStateFlow(false)


    private lateinit var binder: WorkoutSessionService.ServiceBinder
    private var workoutId = 0

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            binder = service as WorkoutSessionService.ServiceBinder
            viewModelScope.launch {
                if(workoutId > 0) {
                    binder.bindWorkout(dbRepository.getWorkoutSuspend(workoutId))
                } else {
                    trigger.value = true
                }
                state = binder.listenState().success()
            }
            viewModelScope.launch {
                binder.getEndTrigger().collect {
                    if(it) {
                        finishSession()
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }

    }

    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    fun onStart(context: Context, id: Int, close: () -> Unit) {
        workoutId = id
        this.context = context
        Intent(context, WorkoutSessionService::class.java).also { intent ->
            context.startForegroundService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        this.closeScreen = close
    }

    fun pushTop(index: Int) {
        binder.pushTop(index)
    }

    fun skipPause() {
        binder.skipPause()
    }

    fun skipExercise(full: Boolean) {
        binder.skipExercise(full)
    }


}