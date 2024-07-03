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
import android.os.IBinder
import android.text.Html
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.FitCubeActivity
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.SessionState
import fr.cuber.fitcube.utils.boldPrediction
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.workout.session.WorkoutSessionService.NotificationConstants.CHANNEL_ID
import fr.cuber.fitcube.workout.session.WorkoutSessionService.NotificationConstants.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutSessionService : Service() {

    @Inject
    lateinit var session: SessionState

    object NotificationConstants {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "workout_session"
    }

    private lateinit var smallLayout: RemoteViews
    private lateinit var bigLayout: RemoteViews

    private val timer = Timer()

    private fun timer() {
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                session.timerTick()
            }
        }, 0, 1000)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Received start command ${intent?.action}")
        when(intent?.action) {
            Actions.START.name -> {
                start()
            }
            Actions.STOP.name -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    enum class Actions {
        START, STOP
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        println("Task removed")
        super.onTaskRemoved(rootIntent)
    }

    private fun start() {
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
                ).apply {
                    println("Im clicked")
                    putExtra("MESSAGE", "PAUSE")
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            bigLayout.setOnClickPendingIntent(R.id.pauseAction, intent)
        }
        CoroutineScope(Dispatchers.Main).launch {
            session.listenState().collect { newState ->
                if (newState.workout.workout.id != 0)
                    updateNotification(newState)
            }
        }
        timer()
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_ID).build())
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateNotification(state: SessionUiState) {
        fun parsing(vals: List<String>): String {
            return vals.joinToString(" - ")
        }
        bigLayout.setTextViewText(
            R.id.notification_title,
            when (state.status) {
                SessionStatus.START -> {
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
        bigLayout.setImageViewResource(
            R.id.pauseAction,
            if (state.paused || state.status == SessionStatus.EXERCISE) {
                R.drawable.baseline_check_24
            } else {
                R.drawable.baseline_pause_24
            }
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
            .setUsesChronometer(state.status != SessionStatus.START)
            .setWhen(state.started)
            .setOnlyAlertOnce(true)
            .setContentText("Workout text")
            .setContentTitle("Workout session")
            .setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, FitCubeActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@WorkoutSessionService,
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
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

}

//Receiver for actions on notification
@AndroidEntryPoint
class NotificationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var session: SessionState

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("MESSAGE")
        println("Im clicked $message")
        if (message != null) {
            if (message == "PAUSE") {
                session.pauseAction()
            }
        }
    }
}



