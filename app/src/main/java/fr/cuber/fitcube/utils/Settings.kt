package fr.cuber.fitcube.utils

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.R
import fr.cuber.fitcube.ui.theme.FitCubeTheme

const val SOUND_DELAY = 10
const val WARMUP_DELAY_PROD = 60.0
const val WARMUP_DELAY_DEV = 5.0
const val WARMUP_ID = -314

fun Context.isProd(): Boolean {
    return this.getString(R.string.version) == "prod"
}

fun Context.getWarmupTime(): Double {
    return if(this.isProd()) WARMUP_DELAY_PROD else WARMUP_DELAY_DEV
}

fun Context.getStartingTime(): Int {
    return if(this.isProd()) 10 else 2
}

fun Context.getSoundDelay(): Int {
    return if(this.isProd()) SOUND_DELAY else 1
}

@Composable
fun Test() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().background(Color.Red)
        ) {
            Text("Test")
        }
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().background(Color.Blue)
        ) {
            Text("TGest2")
        }
    }
}

@Preview
@Composable
fun Preview() {
    FitCubeTheme {
        Surface {
            Test()
        }
    }
}