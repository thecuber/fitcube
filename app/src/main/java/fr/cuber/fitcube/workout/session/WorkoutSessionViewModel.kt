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
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val dbRepository: RoomRepository,
    private val sessionState: SessionState
): ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private lateinit var notificationService: WorkoutSessionNotificationService
    private var bounded: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WorkoutSessionNotificationService.LocalBinder
            notificationService = binder.getService()
            bounded = true
            notificationService.start()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bounded = false
        }
    }

    fun getState() = sessionState.getState()
    fun bindService(intent: Intent, context: Context) {
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun getWorkout(workoutId: Int) = dbRepository.getWorkout(workoutId)
    fun bindWorkout(workout: WorkoutWithExercises) = sessionState.bindWorkout(workout)

}