package fr.cuber.fitcube.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.entity.ExerciseType


typealias IconParserFun = @Composable (ex: ExerciseType, modifier: Modifier) -> Unit

@Composable
fun IconParser(exercise: ExerciseType, modifier: Modifier = Modifier) {
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            bitmapState =
                BitmapFactory.decodeStream(context.assets.open("images/${exercise.id}.jpg"))
        } catch (_: Exception) {
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
        DefaultIconParser(exercise, modifier)
    }
}

@Composable
fun DefaultIconParser(exercise: ExerciseType, modifier: Modifier = Modifier) {
    Image(
        painterResource(id = R.drawable.test), contentDescription = stringResource(
            id = R.string.no_image
        ),
        modifier = modifier
    )
}