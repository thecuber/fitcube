package fr.cuber.fitcube.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.R
import fr.cuber.fitcube.ui.theme.FitCubeTheme

@Composable
fun ExerciseIcon(id: Int, modifier: Modifier = Modifier, darken: Boolean = false) {
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(id) {
        try {
            bitmapState =
                BitmapFactory.decodeStream(context.assets.open("images/${id}.jpg"))
        } catch (_: Exception) {//If image is not found, or run during preview
        }
    }
    if (null != bitmapState) {
        val bitmap = bitmapState!!.asImageBitmap()
        Image(
            bitmap = bitmap,
            "assetsImage",
            modifier = modifier,
            colorFilter = null
        )
    } else {
        DefaultIconParser(modifier, darken)
    }
}

@Composable
private fun DefaultIconParser(modifier: Modifier = Modifier, darken: Boolean = false) {
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
        colorFilter = if (darken) {
            ColorFilter.tint(color = MaterialTheme.colorScheme.secondary, BlendMode.Darken)
        } else {
            null
        }
    )
}

@Preview
@Composable
private fun DefaultIconParserPreview() {
    FitCubeTheme {
        Surface {
            DefaultIconParser()
        }
    }
}