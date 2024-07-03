package fr.cuber.fitcube.exercise.choose

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Icon
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExerciseChooseScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    openExercise: (Int) -> Unit,
    editExercise: (Int) -> Unit,
    workout: Int,
    viewModel: ExerciseChooseViewModel = hiltViewModel()
) {
    val exercises by viewModel.getExercises().collectAsState(initial = emptyList())
    ExerciseChooseScaffold(
        exercises = exercises,
        onClose = onClose,
        modifier = modifier,
        createWorkoutExercise = {
            CoroutineScope(Dispatchers.Main).launch {
                val id = viewModel.createWorkoutExercise(it, workout)
                openExercise(id.toInt())
            }
        },
        editExercise = editExercise
    )
}


@Composable
fun ExerciseChooseScaffold(
    exercises: List<ExerciseType>,
    onClose: () -> Unit,
    createWorkoutExercise: (ExerciseType) -> Unit,
    modifier: Modifier = Modifier,
    editExercise: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            FitCubeAppBar(title = "Add exercise", onClose)
        }
    ) {
        ExerciseChooseContent(
            modifier = modifier.padding(it),
            exercises = exercises,
            createWorkoutExercise = createWorkoutExercise,
            editExercise = editExercise
        )
    }
}

@Composable
fun ExerciseChooseContent(
    exercises: List<ExerciseType>,
    modifier: Modifier = Modifier,
    createWorkoutExercise: (ExerciseType) -> Unit,
    editExercise: (Int) -> Unit
) {
    var string by remember { mutableStateOf("") }
    val ex = exercises.filter { it.name.contains(string, ignoreCase = true) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
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
        OutlinedButton(onClick = { editExercise(0)}) {
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
                        createWorkoutExercise(ex)
                    },
                    editExercise = { editExercise(ex.id) }
                )
            }
        }
    }

}

@Composable
fun ExerciseChooseItem(
    exercise: ExerciseType,
    modifier: Modifier,
    editExercise: () -> Unit = {}
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
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.Edit,
            tint = Color.Black,
            contentDescription = null,
            modifier = Modifier.clickable { editExercise() })
    }
}


@Preview
@Composable
fun ExerciseScaffoldPreview() {
    FitCubeTheme {
        Surface {
            ExerciseChooseScaffold(
                exercises = List(20) {
                    defaultExerciseType(it)
                },
                onClose = {},
                createWorkoutExercise = {},
                editExercise = {}
            )
        }
    }
}