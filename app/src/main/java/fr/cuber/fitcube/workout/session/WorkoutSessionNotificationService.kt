package fr.cuber.fitcube.workout.session

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.SessionState
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.utils.showPrediction
import fr.cuber.fitcube.workout.session.WorkoutSessionNotificationService.NotificationConstants.CHANNEL_ID
import fr.cuber.fitcube.workout.session.WorkoutSessionNotificationService.NotificationConstants.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutSessionNotificationService: Service() {

    private val binder = LocalBinder()

    @Inject
    lateinit var session: SessionState
    object NotificationConstants {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "workout_session"
    }
    private lateinit var smallLayout: RemoteViews
    private lateinit var bigLayout: RemoteViews

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): WorkoutSessionNotificationService = this@WorkoutSessionNotificationService
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            session.getState().collect { newState ->
                // Call your function to rebuild the notification here
                updateNotification(newState)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        RemoteViews(packageName, R.layout.notification_layout_small).apply {
            smallLayout = this
        }
        RemoteViews(packageName, R.layout.notification_layout).apply {
            bigLayout = this
            val intent = PendingIntent.getBroadcast(applicationContext, 0, Intent(applicationContext, NotificationBroadcastReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
            bigLayout.setOnClickPendingIntent(R.id.notification_button_next, intent)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateNotification(state: SessionUiState) {
        fun parsing(vals: List<String>): String {
            return vals.joinToString(" - ")
        }
        bigLayout.setTextViewText(R.id.notification_title, parsing(listOf(state.workout.workout.name, parseTimer(state.restTimer))))
        bigLayout.setTextViewText(R.id.notification_body_exercise,  showPrediction(state.workout.exercises[state.currentExercise].exercise.prediction))
        bigLayout.setTextViewText(R.id.notification_body_set, "${state.workout.exercises[state.currentExercise].type.name} (${state.currentExercise + 1}/${state.workout.exercises.size})")

        //Collapsed notification
        smallLayout.setTextViewText(R.id.notification_title_small, "Workout session - " + state.workout.workout.name)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(bigLayout)
            .setUsesChronometer(true)
            .setWhen(state.started)
            .setContentText("Workout text")
            .setContentTitle("Workout session")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@WorkoutSessionNotificationService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("AIE")
                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    fun start() {
        session.start()
    }

    override fun onBind(intent: Intent): IBinder {
        createNotificationChannel()
        return binder
    }

}

enum class SessionStatus {
    REST,
    EXERCISE,
    TIMING,
    DONE
}

data class SessionUiState(
    val status: SessionStatus,
    val currentSet: Int,
    val currentExercise: Int,
    val restTimer: Int,
    val workout: WorkoutWithExercises,
    val started: Long
)

@AndroidEntryPoint
class NotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var session: SessionState
    override fun onReceive(context: Context?, intent: Intent?) {
        startRest()
    }

    private fun startRest() {
        val timer = 120
        session.startRest(timer)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(session.timerTick())
                    mainHandler.postDelayed(this, 1000)
            }
        })
    }
}