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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.R
import fr.cuber.fitcube.ui.theme.FitCubeTheme

sealed class LoadingFlow<out T> {

    data object Loading : LoadingFlow<Nothing>()

    data class Success<T>(val data: T) : LoadingFlow<T>()

    override fun toString(): String =
        when (this) {
            Loading -> "Loading"
            is Success -> "Success"
        }

}

@Composable
private fun LoadingFlowContent() {
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
        Image(painter = painterResource(id = R.drawable.appicon), modifier = Modifier.rotate(angle), contentDescription = null)
    }
}

@Composable
fun <T> LoadingFlowContainer(
    value: LoadingFlow<T>,
    content: @Composable (T) -> Unit,
) {
    when (value) {
        is LoadingFlow.Loading -> {
            LoadingFlowContent()
        }
        is LoadingFlow.Success -> {
            content(value.data)
        }
    }
}

@Composable
fun <T, U> LoadingFlowContainer(
    value: LoadingFlow<T>,
    value2: LoadingFlow<U>,
    content: @Composable (T, U) -> Unit,
) {
    if(value is LoadingFlow.Success && value2 is LoadingFlow.Success) {
        content(value.data, value2.data)
    } else {
        LoadingFlowContent()
    }
}

@Composable
fun <T, U, V> LoadingFlowContainer(
    value: LoadingFlow<T>,
    value2: LoadingFlow<U>,
    value3: LoadingFlow<V>,
    content: @Composable (T, U, V) -> Unit,
) {
    if(value is LoadingFlow.Success && value2 is LoadingFlow.Success && value3 is LoadingFlow.Success) {
        content(value.data, value2.data, value3.data)
    } else {
        LoadingFlowContent()
    }
}


@Preview
@Composable
fun PlaceholderLoaderPreview() {
    FitCubeTheme {
        Surface {
            LoadingFlowContainer(LoadingFlow.Loading) {

            }
        }
    }
}