package fr.cuber.fitcube.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon

@Composable
fun ExerciseList(
    exercises: List<ExerciseType>,
    modifier: Modifier = Modifier,
    createExercise: () -> Unit,
    openExercise: (Int) -> Unit
) {
    var string by remember { mutableStateOf("") }
    val ex = exercises.filter { it.name.contains(string, ignoreCase = true) }
    HorizontalDivider()
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = string,
            onValueChange = { string = it },
            label = { Text("Filter exercise") },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 10.dp)
        )
        OutlinedButton(onClick = createExercise) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Create new exercise")
        }
        LazyColumn {
            items(ex) { ex ->
                ExerciseChooseItem(exercise = ex,
                    Modifier.clickable {
                        openExercise(ex.id)
                    }.padding(horizontal = 20.dp),
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ExerciseChooseItem(
    exercise: ExerciseType,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
    ) {
        ExerciseIcon(exercise.imagePreview(), modifier = Modifier.fillMaxWidth(0.2f))
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(exercise.name, fontWeight = FontWeight.Bold)
            Text(fontStyle = FontStyle.Italic, text = exercise.description)
        }
    }
}

@Preview
@Composable
fun ExerciseListPreview() {
    FitCubeTheme {
        Surface {
            ExerciseList(exercises = List(10) {
                defaultExerciseType(it)
            }, createExercise = {}, openExercise = {})
        }
    }
}