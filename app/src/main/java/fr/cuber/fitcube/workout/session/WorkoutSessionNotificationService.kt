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
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.Html
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.SessionState
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises
import fr.cuber.fitcube.utils.boldPrediction
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.workout.session.WorkoutSessionNotificationService.NotificationConstants.CHANNEL_ID
import fr.cuber.fitcube.workout.session.WorkoutSessionNotificationService.NotificationConstants.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutSessionNotificationService : Service() {

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
            session.listenState().collect { newState ->
                if (newState.status != SessionStatus.WAITING && newState.workout.workout.id != 0)
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
            val intent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent(
                    applicationContext,
                    NotificationBroadcastReceiver::class.java
                ).also { it.action = "REST" },
                PendingIntent.FLAG_IMMUTABLE
            )
            bigLayout.setOnClickPendingIntent(R.id.notification_button_next, intent)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateNotification(state: SessionUiState) {
        fun parsing(vals: List<String>): String {
            return vals.joinToString(" - ")
        }
        bigLayout.setTextViewText(
            R.id.notification_title,
            when (state.status) {
                SessionStatus.WAITING -> {
                    "Waiting for workout to start..."
                }

                SessionStatus.REST -> {
                    parsing(listOf(state.workout.workout.name, parseTimer(state.timer)))
                }

                else -> {
                    state.workout.workout.name
                }
            }
        )
        bigLayout.setTextViewText(
            R.id.notification_body_exercise,
            Html.fromHtml(
                boldPrediction(
                    state.workout.exercises[state.currentExercise].exercise.prediction,
                    state.currentSet
                ), Html.FROM_HTML_MODE_COMPACT
            )
        )
        try {
            val bitmapState =
                BitmapFactory.decodeStream(applicationContext.assets.open("images/${state.workout.exercises[state.currentExercise].type.id}.jpg"))
            bigLayout.setImageViewBitmap(R.id.notification_image, bitmapState)
        } catch (e: Exception) {
            print(e.stackTrace)
        }
        bigLayout.setTextViewText(
            R.id.notification_body_set,
            "${state.workout.exercises[state.currentExercise].type.name} (${state.currentExercise + 1}/${state.workout.exercises.size})"
        )
        bigLayout.setBoolean(
            R.id.notification_button_next,
            "setEnabled",
            state.status != SessionStatus.REST
        )
        //Collapsed notification
        smallLayout.setTextViewText(
            R.id.notification_title_small,
            "Workout session - " + state.workout.workout.name
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(bigLayout)
            .setUsesChronometer(state.status != SessionStatus.WAITING)
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

    override fun onBind(intent: Intent): IBinder {
        createNotificationChannel()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        println("IM UNBINDING")
        Intent().also {
            it.action = "STOP"
            sendBroadcast(it)
        }
        stopSelf()
        return super.onUnbind(intent)
    }

    fun startRest() {
        Intent(
            applicationContext,
            NotificationBroadcastReceiver::class.java
        ).also {
            it.action = "REST"
            sendBroadcast(it)
        }
    }

}

enum class SessionStatus {
    WAITING,
    REST,
    EXERCISE,
    TIMING,
    DONE
}

data class SessionUiState(
    val status: SessionStatus,
    val currentSet: Int,
    val currentExercise: Int,
    val timer: Int,
    val workout: WorkoutWithExercises,
    val started: Long,
    val predictions: List<List<Double>>,
    val elapsedTime: Long
)

fun defaultSessionUiState(size: Int) = SessionUiState(
    status = SessionStatus.WAITING,
    0,
    0,
    0,
    defaultWorkoutWithExercises(size),
    0L,
    List(size) { List(4) { 10.0 } },
    0L
)


@AndroidEntryPoint
class NotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var session: SessionState

    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "REST" -> startRest()
            "STOP" -> stopTimer()
        }
    }

    private fun stopTimer() {
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun startRest() {
        val timer = 10
        session.startRest(timer)
        mainHandler.post(object : Runnable {
            override fun run() {
                if (session.timerTick())
                    mainHandler.postDelayed(this, 1000)
            }
        })
    }
}