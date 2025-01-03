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
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.text.Html
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.cuber.fitcube.FitCubeActivity
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.isWarmup
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.data.db.entity.imageStream
import fr.cuber.fitcube.utils.boldPrediction
import fr.cuber.fitcube.utils.getSoundDelay
import fr.cuber.fitcube.utils.getStartingTime
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

        private val _uiState = MutableStateFlow(defaultSessionState(0, -1))
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
            val updatedWorkout = workout.copy(
                exercises = workout.exercises.filter { it.exercise.enabled }
            )
            val order = if(workout.workout.order.isNotEmpty()) {
                workout.workout.order.filter { or -> updatedWorkout.exercises.find { it.exercise.id == or } != null }
            } else {
                updatedWorkout.exercises.map { it.exercise.id }
            }
            _uiState.value = _uiState.value.copy(
                workout = updatedWorkout,
                predictions = updatedWorkout.exercises.associate { it.exercise.id to it.exercise.prediction },
                rest = workout.workout.rest,
                order = order,
                sets = order.associateWith { 0 }.toMap(),
                timer = applicationContext.getStartingTime())
            updateNotification()
        }

        fun elapsedTick() {
            _uiState.value = _uiState.value.copy(elapsedTime = _uiState.value.elapsedTime + 1000)
        }

        /**
         * Function called by the timer. Updates the values, and switch to the new set / exercise if needed
         */
        fun timerTick() {
            _uiState.value = _uiState.value.copy(timer = _uiState.value.timer - 1)
            if(_uiState.value.status == SessionStatus.TIMING) {
                val bfWarmup = _uiState.value.current().isWarmup()
                if(_uiState.value.timer == 0 && !postExerciseUpdate()) {
                    _uiState.value = _uiState.value.copy(timer = if(_uiState.value.current().isWarmup() || bfWarmup) 30 else _uiState.value.rest, status = SessionStatus.REST)
                    player.start()//Play sound
                }
                return
            }
            if (_uiState.value.timer == 0) {
                if (_uiState.value.status == SessionStatus.START) {
                    //If start, then we reset the starting time because of the time elapsed from the activity, loses 10 seconds on total time
                    _uiState.value = _uiState.value.copy(
                        started = System.currentTimeMillis(),
                        elapsedTime = 0
                    )
                    startTotalTimer()
                }
                //In any case, we cancel the timer, because end of a "rest" (rest or first start)
                cancelTimer()
                _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
                if(_uiState.value.current().exercise.mode == WorkoutMode.TIMED) {
                    _uiState.value = _uiState.value.copy(timer = _uiState.value.current().exercise.prediction[_uiState.value.currentSet()].toInt(), status = SessionStatus.TIMING)
                    startTimer()
                }

            }
            if(_uiState.value.timer == applicationContext.getSoundDelay() && _uiState.value.status != SessionStatus.START && _uiState.value.workout.workout.sound) {
                //We play the sound
                player.start()
            }
        }

        /**
         * Function called after an exercise is done, updates the count / set / status
         * Returns true if the workout is finished
         */
        private fun postExerciseUpdate(): Boolean {
            val value = getValue()
            val set = value.currentSet() + 1
            var ex = value.currentExercise
            if (set == value.current().exercise.prediction.size) {
                ex += 1
                if (ex == value.exerciseCount()) {
                    _uiState.value = _uiState.value.copy(status = SessionStatus.DONE)
                    return true
                }
            }
            _uiState.value = _uiState.value.copy(
                currentExercise = ex,
                sets = value.sets.toMutableMap().apply {
                    set(value.order[value.currentExercise], set)
                }
            )
            return false
        }

        /**
         * Updates the prediction for next time
         */
        fun setNextPrediction(prediction: List<Double>, index: Int) {
            val predictions = _uiState.value.predictions.toMutableMap()
            predictions[index] = prediction
            _uiState.value = _uiState.value.copy(predictions = predictions)
        }

        /**
         * Updates the prediction for this session only
         */
        fun setCurrentPrediction(prediction: List<Double>, index: Int) {
            _uiState.value =
                _uiState.value.copy(workout = _uiState.value.workout.copy(exercises = _uiState.value.ordered().mapIndexed { i, fullExercise ->
                    if (index == i) {
                        fullExercise.copy(exercise = fullExercise.exercise.copy(prediction = prediction))
                    } else {
                        fullExercise
                    }
                }))

            updateNotification()
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
                val bfWarmup = _uiState.value.current().isWarmup()
                if(!postExerciseUpdate()) {
                    //We check if the ex before is warmup or the upcoming is warmup
                    _uiState.value =
                        _uiState.value.copy(timer = if(_uiState.value.current().isWarmup() || bfWarmup) 15 else _uiState.value.rest, status = SessionStatus.REST)
                }
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
            elapsedTimer.cancel()
            binder._uiState.value = defaultSessionState(0, -1)
            player.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        fun addRest() {
            _uiState.value = _uiState.value.copy(timer = _uiState.value.timer + 15)
        }

        fun orderExercises(ids: List<Int>) {
            _uiState.value = _uiState.value.copy(order = ids)
            updateNotification()
        }

        /**
         * Skip the current pause
         */
        fun skipPause() {
            cancelTimer()
            val current = _uiState.value.current()
            if(current.exercise.mode == WorkoutMode.TIMED) {
                _uiState.value = _uiState.value.copy(timer = _uiState.value.current().exercise.prediction[_uiState.value.currentSet()].toInt(), status = SessionStatus.TIMING)
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
            }
            updateNotification()
        }

        /**
         * Skips the current set, full indicates if we skip the whole exercise or only this set
         */
        fun skipExercise(full: Boolean) {
            if(_uiState.value.status == SessionStatus.TIMING) {
                _uiState.value = _uiState.value.copy(status = SessionStatus.EXERCISE)
                cancelTimer()
            }
            if(full) {
                _uiState.value = _uiState.value.copy(sets = _uiState.value.sets.toMutableMap().apply {
                    set(_uiState.value.current().exercise.id, _uiState.value.current().exercise.prediction.size - 1)
                })
            }
            pauseAction()
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
    private val elapsedTimer = Timer()
    private var running = false//If the timer is running

    private fun cancelTimer() {
        running = false
        timer.cancel()
        timer = Timer()
    }

    fun startTotalTimer() {
        elapsedTimer.schedule(object : java.util.TimerTask() {
            override fun run() {
                binder.elapsedTick()
            }
        }, 1000, 1000)
    }

    private fun startTimer() {
        //TODO Try to see why this does not work sometimes
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

        val pauseIntent = PendingIntent.getBroadcast(
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
        val addIntent = PendingIntent.getBroadcast(
            applicationContext,
            1,
            Intent(
                applicationContext,
                NotificationBroadcastReceiver::class.java
            ).apply {
                putExtra("MESSAGE", "REST")
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        var inputStream: InputStream? = null
        if(state.status != SessionStatus.DONE) {
            inputStream = imageStream(state.current().type.imagePreview(), applicationContext)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setUsesChronometer(state.status != SessionStatus.START)
            .setUsesChronometer(state.status != SessionStatus.START)
            .setWhen(state.started)
            .setOnlyAlertOnce(true)
            .setContentText(
                if(state.status == SessionStatus.DONE) {
                    "Click to modify the weights used"
                } else {
                    Html.fromHtml(
                        boldPrediction(
                            state.current().exercise.prediction,
                            state.currentSet()
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
                            "Set ${state.currentSet() + 1}/${state.current().exercise.prediction.size}",
                            "${state.current().type.name} (${state.currentExercise + 1}/${state.exerciseCount()})"
                        )
                    )
                } else if(state.status == SessionStatus.DONE) {
                    "Workout done!"
                } else {
                    parsing(
                        listOf(
                            parseTimer(state.timer),
                            "${state.current().type.name} (${state.currentExercise + 1}/${state.exerciseCount()})"
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
            .setLargeIcon(if(inputStream !=null) { Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 192, 192, true) } else { null })
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
                }, pauseIntent
            )
            .addAction(
                0, "+15s", addIntent
            )
            .setSmallIcon(R.drawable.baseline_fitness_center_24)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
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
            else if(message == "REST") {
                WorkoutSessionService.ServiceBinderSingleton.binder.addRest()
            }
        }
    }
}




