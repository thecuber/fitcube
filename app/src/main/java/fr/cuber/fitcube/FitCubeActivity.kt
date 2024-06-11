package fr.cuber.fitcube

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.cuber.fitcube.ui.theme.FitCubeTheme

@AndroidEntryPoint
class FitCubeActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FitCubeTheme {
                FitCubeNavGraph()
            }
        }
    }


}