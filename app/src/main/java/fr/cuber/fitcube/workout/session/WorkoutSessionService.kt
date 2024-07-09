package fr.cuber.fitcube.workout.session

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.text.Html
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.cuber.fitcube.FitCubeActivity
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.data.db.entity.imageStream
import fr.cuber.fitcube.ui.theme.surfaceLight
import fr.cuber.fitcube.utils.boldPrediction
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.workout.session.WorkoutSessionService.NotificationConstants.CHANNEL_ID
import fr.cuber.fitcube.workout.session.WorkoutSessionService.NotificationConstants.NOTIFICATION_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream
import java.util.Timer

/**
 * Service that manages the workout session. Runs the timer, keeps the data, and updates the notification.
 */
class WorkoutSessionService : Service() {

    object NotificationConstants {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "workout_session"
    }


    //Binder used to receive communications from the activity and the BroadcastReceiver
    inner class ServiceBinder : Binder() {

        private val _uiState = MutableStateFlow(defaultSessionState(0))
        private val state: StateFlow<SessionState> = _uiState

        fun listenState() = state

        fun getValue() = state.value

        private val trigger = MutableStateFlow(false)

        private lateinit var player: MediaPlayer

        fun create() {
            player = MediaPlayer.create(applicationContext, R.raw.beep)
        }

        fun getEndTrigger() = trigger

        /**
         * Called once at startup, stores the workout
         */
        fun bindWorkout(workout: WorkoutWithExercises) {
            _uiState.value = _uiState.value.copy(
                workout = workout,
                predictions = workout.exercises.map { it.exercise.prediction })
            updateNotification()
        }

        /**
         * Function called by the timer. Updates the values, and switch to the new set / exercise if needed
         */
        fun timerTick() {
            _uiState.value = _uiState.value.copy(elapsedTime = _uiState.value.elapsedTime + 1000)
            //if (_uiState.value.paused || _uiState.value.status == SessionStatus.EXERCISE) return This line should not be needed now
            _uiState.value = _uiState.value.copy(timer = _uiState.value.timer - 1)
            if (_uiState.value.timer == 0) {
                if (_uiState.value.status == SessionStatus.START) {
                    //If start, then we reset the starting time because of the time elapsed from the activity, loses 10 seconds on total time
                    _uiState.value = _uiState.value.copy(
                        status = SessionStatus.EXERCISE,
                        started = System.currentTimeMillis(),
                        elapsedTime = 0
                    )
                } else {
                    _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
                    //We play the sound
                    player.start()
                }
                //In any case, we cancel the timer, because end of a "rest" (rest or first start)
                cancelTimer()
            }
        }

        /**
         * Function called after an exercise is done, updates the count / set / status
         */
        private fun postExerciseUpdate() {
            var set = getValue().currentSet + 1
            var ex = getValue().currentExercise
            if (set == getValue().workout.exercises[getValue().currentExercise].exercise.prediction.size) {
                set = 0
                ex += 1
                if (ex == getValue().workout.exercises.size) {
                    _uiState.value = _uiState.value.copy(status = SessionStatus.DONE)
                    return
                }
            }
            _uiState.value = _uiState.value.copy(
                currentExercise = ex,
                currentSet = set
            )
        }

        /**
         * Updates the prediction for next time
         */
        fun setNextPrediction(prediction: List<Double>, index: Int) {
            val predictions = _uiState.value.predictions.toMutableList()
            predictions[index] = prediction
            _uiState.value = _uiState.value.copy(predictions = predictions)
        }

        /**
         * Updates the prediction for this session only
         */
        fun setCurrentPrediction(prediction: List<Double>, index: Int) {
            _uiState.value =
                _uiState.value.copy(workout = _uiState.value.workout.copy(exercises = _uiState.value.workout.exercises.mapIndexed { i, fullExercise ->
                    if (index == i) {
                        fullExercise.copy(exercise = fullExercise.exercise.copy(prediction = prediction))
                    } else {
                        fullExercise
                    }
                }))
        }

        /**
         * When any pause button is clicked
         */
        fun pauseAction() {
            if(_uiState.value.status == SessionStatus.DONE) {
                trigger.value = true
                return
            }
            if (_uiState.value.status == SessionStatus.EXERCISE) {
                //This can be a rest
                _uiState.value =
                    _uiState.value.copy(timer = _uiState.value.rest, status = SessionStatus.REST)
                postExerciseUpdate()
            } else {
                _uiState.value = _uiState.value.copy(paused = _uiState.value.paused.not())
            }
            if(_uiState.value.status != SessionStatus.DONE) {
                if (running) {
                    cancelTimer()
                } else {
                    startTimer()
                }
            }
            updateNotification()
        }

        /**
         * Updates the value of the rest time for this session
         */
        fun setRest(it: Int) {
            _uiState.value = _uiState.value.copy(rest = it)
        }

        /**
         * Called by the activity to clear everything
         */
        fun finishService() {
            cancelTimer()
            binder._uiState.value = defaultSessionState(0)
            player.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

    }

    //The binder object to communicate with
    private val binder = ServiceBinder()

    //For the BroadcastReceiver
    object ServiceBinderSingleton {
        lateinit var binder: WorkoutSessionService.ServiceBinder
    }

    override fun onBind(intent: Intent?): IBinder {
        ServiceBinderSingleton.binder = binder
        return binder
    }

    private var timer = Timer()
    private var running = false//If the timer is running

    private fun cancelTimer() {
        running = false
        timer.cancel()
        timer = Timer()
    }

    private fun startTimer() {
        running = true
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                binder.timerTick()
                updateNotification()
            }
        }, 1000, 1000)
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        binder.create()
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_ID).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Updates the body of the notification and builds it
     */
    private fun updateNotification() {
        val state = binder.getValue()
        fun parsing(vals: List<String>): String {
            return vals.joinToString(" - ")
        }

        val intent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(
                applicationContext,
                NotificationBroadcastReceiver::class.java
            ).apply {
                putExtra("MESSAGE", "PAUSE")
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        var inputStream: InputStream? = null
        if(state.status != SessionStatus.DONE) {
            inputStream = imageStream(state.workout.exercises[state.currentExercise].type.imagePreview(), applicationContext)
        }
        var bitmapTint: Bitmap? = null
        if (inputStream != null) {
            val bitmapState =
                BitmapFactory.decodeStream(inputStream)
            val paint = Paint()
            paint.setColorFilter(
                PorterDuffColorFilter(
                    surfaceLight.toArgb(),
                    PorterDuff.Mode.DARKEN
                )
            )
            bitmapTint =
                Bitmap.createBitmap(bitmapState.width, bitmapState.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapTint)
            canvas.drawBitmap(bitmapState, 0f, 0f, paint)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setUsesChronometer(state.status != SessionStatus.START)
            .setWhen(state.started)
            .setOnlyAlertOnce(true)
            .setColor(surfaceLight.toArgb())
            .setColorized(true)
            .setContentText(
                if(state.status == SessionStatus.DONE) {
                    "Click to modify the weights used"
                } else {
                    Html.fromHtml(
                        boldPrediction(
                            state.workout.exercises[state.currentExercise].exercise.prediction,
                            state.currentSet
                        ), Html.FROM_HTML_MODE_COMPACT
                    )
                }
            )
            .setContentTitle(
                if (state.status == SessionStatus.START && state.paused) {
                    "Waiting for workout to start..."
                } else if (state.status == SessionStatus.EXERCISE) {
                    parsing(
                        listOf(
                            "Set ${state.currentSet + 1}/${state.workout.exercises[state.currentExercise].exercise.prediction.size}",
                            "${state.workout.exercises[state.currentExercise].type.name} (${state.currentExercise + 1}/${state.workout.exercises.size})"
                        )
                    )
                } else if(state.status == SessionStatus.DONE) {
                    "Workout done!"
                } else {
                    parsing(
                        listOf(
                            parseTimer(state.timer),
                            "${state.workout.exercises[state.currentExercise].type.name} (${state.currentExercise + 1}/${state.workout.exercises.size})"
                        )
                    )
                }
            )
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, FitCubeActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setLargeIcon(bitmapTint)
            .addAction(
                0, if (state.status == SessionStatus.START && state.paused) {
                    "Start"
                } else if (state.status == SessionStatus.DONE) {
                    "Finish"
                } else if (state.status == SessionStatus.EXERCISE) {
                    "Rest"
                } else if (state.paused) {
                    "Unpause"
                } else {
                    "Pause"
                }, intent
            )
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
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    /**
     * Builds the notification channel, called once at the creation
     */
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

/**
 * Broadcast receiver that listens to the notification actions
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("MESSAGE")
        if (message != null) {
            if (message == "PAUSE") {
                WorkoutSessionService.ServiceBinderSingleton.binder.pauseAction()
            }
        }
    }
}




