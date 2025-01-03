package fr.cuber.fitcube.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import fr.cuber.fitcube.ui.theme.FitCubeTheme


@Composable
fun PredictionField(
    modifier: Modifier = Modifier,
    validPrediction: (List<Double>) -> Unit,
    top: Boolean = true
) {
    var text by remember { mutableStateOf("") }
    val regexPattern = "((\\d+x)?\\d+(\\.\\d+)?\\s?)+"
    var validParsing by remember {
        mutableStateOf(
            false
        )
    }
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                colors = if(top) OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedLabelColor = Color.White,
                ) else OutlinedTextFieldDefaults.colors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                label = { Text("Change to...") },
                maxLines = 1, onValueChange = {
                    validParsing = it.matches(regexPattern.toRegex())
                    text = it
                },
                modifier = Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.padding(5.dp))
            FilledIconButton(
                onClick = {
                    validPrediction(parsePrediction(text))
                    text = ""},
                enabled = validParsing,
                modifier = Modifier
                    .size(55.dp)
                    .zIndex(10F),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.LightGray)
            ) {
                Icon(imageVector = Icons.Filled.Done, contentDescription = "")
            }
        }
    }
}

@Preview
@Composable
fun PredictionFieldPreview() {
    FitCubeTheme {
        Surface {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                    MaterialTheme.colorScheme.secondary
                )
            ) {
                PredictionField(
                    validPrediction = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
        }
    }
}