package fr.cuber.fitcube.utils

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.R
import fr.cuber.fitcube.ui.theme.FitCubeTheme

sealed class LoadingFlow<out T> {

    data object Loading : LoadingFlow<Nothing>()

    data class Success<T>(val data: T) : LoadingFlow<T>()

    data class Error(
        val t: Throwable,
        var consumed: Boolean = false
    ) : LoadingFlow<Nothing>()
}

@Composable
fun PlaceholderLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 0.8f))
        ), label = ""
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //TODO modify when go the icon
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), modifier = Modifier.rotate(angle), contentDescription = null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary))
    }
}

@Preview
@Composable
fun PlaceholderLoaderPreview() {
    FitCubeTheme {
        Surface {
            PlaceholderLoader()
        }
    }
}