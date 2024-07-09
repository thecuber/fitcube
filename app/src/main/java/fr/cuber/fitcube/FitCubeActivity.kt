package fr.cuber.fitcube

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.workout.session.WorkoutSessionService


@AndroidEntryPoint
class FitCubeActivity : AppCompatActivity() {

    private fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val isServiceRunning = isServiceRunningInForeground(this, WorkoutSessionService::class.java)
        setContent {
            FitCubeTheme {
                FitCubeNavGraph(startDestination = if (isServiceRunning) sessionRoute(0) else FitCubeRoutes.HOME)
            }
        }
    }


}