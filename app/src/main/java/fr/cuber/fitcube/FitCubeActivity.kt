package fr.cuber.fitcube

import android.app.ActivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.workout.session.WorkoutSessionService


@AndroidEntryPoint
class FitCubeActivity: AppCompatActivity() {

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.d( "t", "Service is already running")
                return true
            }
        }
        Log.d("t", "tagueueleezfknz gfkzjebfzejfzek")
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        isMyServiceRunning(WorkoutSessionService::class.java)
        super.onCreate(savedInstanceState)
        setContent {
            FitCubeTheme {
                FitCubeNavGraph()
            }
        }
    }


}