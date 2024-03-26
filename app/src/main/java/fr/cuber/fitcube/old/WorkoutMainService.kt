package fr.cuber.fitcube.old

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.cuber.fitcube.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutMainService : Service() {
    private val binder = LocalBinder()

    private val CHANNEL_ID = "FitCubeWorkoutChannel"

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutMainService = this@WorkoutMainService
    }

    private fun createNotificationChannel() {
        val name = "WorkoutChannel"
        val descriptionText = "Channel to show permanent notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun startTimer(timingUpdate: (Int) -> Unit) {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workout")
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1, builder.build())
        CoroutineScope(Dispatchers.IO).launch {
            /*while(true) {
                Thread.sleep(1000)
                timerValue += 1
                timingUpdate(timerValue)
                builder.setContentText("textContent $timerValue")
                CoroutineScope(Dispatchers.Main).launch {
                    NotificationManagerCompat.from(this@WorkoutMainService).notify(1, builder.build())
                }
            }*/
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}