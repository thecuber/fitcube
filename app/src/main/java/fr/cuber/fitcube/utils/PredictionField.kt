package fr.cuber.fitcube.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.cuber.fitcube.ui.theme.FitCubeTheme

@Composable
fun PredictionField(
    label: String,
    placeholder: String,
    validPrediction: (List<Double>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    LaunchedEffect(key1 = label) {
        text = ""
    }
    val regexPattern = "(\\d+x\\d+\\s?)+"
    var validParsing by remember {
        mutableStateOf(
            false
        )
    }
    Column {
        Text(text = label)
        TextField(
            value = text,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            placeholder = { Text(placeholder) },
            maxLines = 1, onValueChange = {
                validParsing = it.matches(regexPattern.toRegex())
                text = it
                if(validParsing) {
                    validPrediction(parsePrediction(it))
                }
            }, isError = !validParsing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
fun PredictionFieldPreview() {
    FitCubeTheme {
        Surface {
            PredictionField(
                label = "Prediction",
                validPrediction = {},
                placeholder = "3x3 4x4 5x5"
            )
        }
    }
}