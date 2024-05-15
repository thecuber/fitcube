package fr.cuber.fitcube.exercise.workoutedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Switch
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.DefaultIconParser
import fr.cuber.fitcube.utils.DividerSpaced
import fr.cuber.fitcube.utils.WorkoutExerciseAppBar
import fr.cuber.fitcube.utils.parsePrediction
import fr.cuber.fitcube.utils.showPrediction
import kotlinx.coroutines.launch


@Composable
fun WorkoutExerciseScreen(
    back: () -> Unit,
    id: Int,
    modifier: Modifier = Modifier,
    viewModel: WorkoutExerciseViewModel = hiltViewModel()
) {
    var exercise by remember {
        mutableStateOf(
            defaultFullExercise(0)
        )
    }
    LaunchedEffect(key1 = Unit) {
        exercise = viewModel.getExercise(id)
    }
    WorkoutExerciseScaffold(
        exercise = exercise,
        setExercise = { exercise = it },
        modifier = modifier,
        back = {
            back()
        },
        onSave = {
            viewModel.saveWorkoutExercise(exercise.exercise)

        }
    )
}

@Composable
fun WorkoutExerciseScaffold(
    modifier: Modifier = Modifier,
    exercise: FullExercise,
    setExercise: (FullExercise) -> Unit,
    back: () -> Unit,
    onSave: () -> Unit,
    icon: @Composable (ExerciseType) -> Unit = { DefaultIconParser(exercise = it) },
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            WorkoutExerciseAppBar(title = "Modify exercise", onBack = back, onSave = {
                onSave()
                scope.launch {
                    snackbarHostState.showSnackbar("Exercise saved !")
                }
            })
        }
    ) {
        WorkoutExerciseContent(
            exercise = exercise,
            icon = icon,
            modifier = modifier.padding(it),
            setExercise = setExercise
        )

    }
}

@Composable
fun WorkoutExerciseContent(
    modifier: Modifier = Modifier,
    exercise: FullExercise,
    setExercise: (FullExercise) -> Unit,
    icon: @Composable (ExerciseType) -> Unit = { DefaultIconParser(exercise = it) },
) {
    val regexPattern = "(\\d+x\\d+\\s?)+"
    var prediction by remember { mutableStateOf("") }
    var validParsing by remember {
        mutableStateOf(
            false
        )
    }
    if (validParsing) {
        setExercise(exercise.copy(exercise = exercise.exercise.copy(prediction = parsePrediction(prediction))))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = exercise.type.name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.padding(5.dp))
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            icon(exercise.type)
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = exercise.type.description, fontStyle = FontStyle.Italic)
            ExerciseModeSelect(mode = exercise.exercise.mode) {
                setExercise(exercise.copy(exercise = exercise.exercise.copy(mode = it)))
            }
            DividerSpaced(pad = 5.dp)
            Text(text = "Prediction for current session:")
            Text(
                showPrediction(
                    if (validParsing) {
                        parsePrediction(prediction)
                    } else {
                        exercise.exercise.prediction
                    }
                )
            )
            Spacer(modifier = Modifier.padding(5.dp))
            TextField(value = prediction, onValueChange = {
                validParsing = it.matches(regexPattern.toRegex())
                prediction = it
            }, isError = !validParsing, label = { Text("Prediction") })
        }
    }
}

@Composable
fun ExerciseModeSelect(
    mode: WorkoutMode,
    onModeChange: (WorkoutMode) -> Unit
) {
    Row {
        Icon(
            painterResource(id = R.drawable.baseline_access_time_24),
            contentDescription = "",
            tint = if (mode == WorkoutMode.TIMED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Text(text = "Timed")
        Spacer(modifier = Modifier.padding(5.dp))
        Switch(
            checked = mode == WorkoutMode.REPETITION,
            onCheckedChange = { onModeChange(if (it) WorkoutMode.REPETITION else WorkoutMode.TIMED) })
        Spacer(modifier = Modifier.padding(5.dp))
        Text(text = "Repetition")
        Spacer(modifier = Modifier.padding(5.dp))
        Icon(
            painterResource(id = R.drawable.baseline_123_24),
            contentDescription = "",
            tint = if (mode == WorkoutMode.REPETITION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun ExerciseModeSelectPreview() {
    var mode = WorkoutMode.TIMED
    FitCubeTheme {
        Surface {
            ExerciseModeSelect(
                mode,
                onModeChange = { mode = it }
            )
        }
    }
}

@Preview
@Composable
fun WorkoutExerciseContentPreview() {
    FitCubeTheme {
        Surface {
            WorkoutExerciseScaffold(exercise = defaultFullExercise(50),
                back = {},
                onSave = {},
                setExercise = {})
        }
    }
}