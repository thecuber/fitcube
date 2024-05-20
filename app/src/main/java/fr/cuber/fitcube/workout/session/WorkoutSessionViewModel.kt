package fr.cuber.fitcube.workout.session

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.SessionState
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val dbRepository: RoomRepository,
    private val sessionState: SessionState
): ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private lateinit var notificationService: WorkoutSessionNotificationService
    private var bounded: Boolean = false
    private lateinit var context: Context

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WorkoutSessionNotificationService.LocalBinder
            notificationService = binder.getService()
            bounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bounded = false
        }
    }


    init {
        CoroutineScope(Dispatchers.Main).launch {
            sessionState.onEnd().collect {
                if (!it)
                    return@collect
                finishSession(sessionState.getValue())
                context.unbindService(connection)
            }
        }
    }
    fun getState() = sessionState.listenState()
    fun bindService(intent: Intent, context: Context) {
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        this.context = context
    }

    fun getWorkout(workoutId: Int) = dbRepository.getWorkout(workoutId)
    fun bindWorkout(workout: WorkoutWithExercises) = sessionState.bindWorkout(workout)
    fun start() {
        sessionState.start()
    }

    private suspend fun finishSession(state: SessionUiState) {
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
        dbRepository.createSession(session)
    }

}