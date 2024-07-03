package fr.cuber.fitcube.utils

import android.content.Context
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
import java.io.File
import java.io.FileInputStream

@Composable
fun ExerciseIcon(img: String, modifier: Modifier = Modifier) {
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(img) {
        if(img.isNotEmpty()){
            try {
                val type = img.subSequence(0, 3)
                val fileName = img.substring(4)
                println("Image type $type")
                val file = when (type) {
                    "png" -> {
                        context.assets.open("images/$fileName")
                    }
                    "fld" -> {
                        FileInputStream(
                            File(context.getDir("images", Context.MODE_PRIVATE), fileName)
                        )
                    }
                    else -> {
                        null
                    }
                }
                bitmapState =
                    BitmapFactory.decodeStream(file)
            } catch (e: Exception) {//If image is not found, or run during preview
                println("Im in error block ${e.message} ${e.stackTraceToString()}")
            }
        }
    }
    if (bitmapState != null) {
        val bitmap = bitmapState!!.asImageBitmap()
        Image(
            bitmap = bitmap,
            "assetsImage",
            modifier = modifier,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface, BlendMode.Darken)
        )
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
            DefaultIconParser()
        }
    }
}