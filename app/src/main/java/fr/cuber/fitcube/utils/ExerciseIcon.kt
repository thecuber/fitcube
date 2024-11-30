package fr.cuber.fitcube.utils

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.entity.imageStream
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ExerciseIcon(img: String, modifier: Modifier = Modifier) {
    var byteArray by remember { mutableStateOf<ByteArray?>(null) }
    val context = LocalContext.current
    LaunchedEffect(img) {
        if(img.isNotEmpty()){
            try {
                val inputStream = imageStream(img, context)
                if (inputStream != null) {
                    byteArray = withContext(Dispatchers.IO) {
                        inputStream.readBytes() // Converts InputStream to ByteArray
                    }
                }
            } catch (e: Exception) {//If image is not found, or run during preview
                println("Im in error block ${e.message} ${e.stackTraceToString()}")
            }
        } else {
            byteArray = null
        }
    }
    if (byteArray != null) {
        AsyncImage(model = byteArray, modifier = modifier, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface, BlendMode.Darken), contentDescription = null)
        /*Image(
            bitmap = bitmap,
            "assetsImage",
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface, BlendMode.Darken)
        )*/
    } else {
        DefaultIconParser(modifier)
    }
}

@Composable
private fun DefaultIconParser(modifier: Modifier = Modifier) {
    Image(
        painterResource(
            id = if (LocalInspectionMode.current) {
                R.drawable.preview_placeholder
            } else {
                R.drawable.runtime_placeholder
            }
        ), contentDescription = stringResource(
            id = R.string.no_image
        ),
        modifier = modifier,
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.surface, BlendMode.Darken)
    )
}

@Preview
@Composable
private fun DefaultIconParserPreview() {
    FitCubeTheme {
        Surface {
            ExerciseIcon(img = "png/0001-relaxation.png")
        }
    }
}