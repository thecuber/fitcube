package fr.cuber.fitcube.workout.session

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.PredictionField
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.parseTimer


@Composable
fun WorkoutSessionScreen(
    workoutId: Int,
    back: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.getState().collectAsState()
    val workout by viewModel.getWorkout(workoutId).collectAsState(
        initial = WorkoutWithExercises(
            Workout(0, ""), emptyList()
        )
    )
    LaunchedEffect(Unit) {
        Intent(context, WorkoutSessionNotificationService::class.java).also { intent ->
            viewModel.bindService(intent, context)
        }
    }
    LaunchedEffect(workout) {
        viewModel.bindWorkout(workout)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScaffold(
    modifier: Modifier = Modifier,
    state: SessionUiState,
    paused: Boolean,
    onPauseAction: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        state.workout.exercises[state.currentExercise].type.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight(700)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    OutlinedButton(
                        onClick = onPauseAction,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 2.dp
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "")
                    }
                }
            )
        },
    ) {
        WorkoutSessionContent(
            modifier = modifier.padding(it),
            state = state,
            paused = paused,
            onPauseAction = onPauseAction
        )
    }
}

@Preview
@Composable
fun WorkoutSessionContentPreview() {
    FitCubeTheme(false) {
        Surface {
            WorkoutSessionScaffold(
                state = defaultSessionUiState(10).copy(
                    currentSet = 1,
                    timer = 83,
                    elapsedTime = 1000 * (3600L + 23 * 60L + 45L)
                ),
                paused = false,
                onPauseAction = {}
            )
        }
    }

}

enum class Expanded {
    DEFAULT,
    WEIGHTS,
    EXERCISES
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    state: SessionUiState,
    paused: Boolean,
    onPauseAction: () -> Unit
) {
    val progress = 0.5f
    val exerciseCount = state.workout.exercises.size
    var expanded by remember { mutableStateOf(Expanded.DEFAULT) }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseIcon(
                id = state.workout.exercises[state.currentExercise].type.id,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
        }
        WorkoutSessionWeights(
            exercise = state.workout.exercises[state.currentExercise],
            currentSet = state.currentSet,
            expanded = expanded,
            setExpanded = {
                expanded = if (expanded == Expanded.WEIGHTS) {
                    Expanded.DEFAULT
                } else {
                    Expanded.WEIGHTS
                }
            }
        )
        WorkoutSessionActions(
            onPauseAction = onPauseAction,
            paused = paused,
            timer = state.timer,
            currentSet = state.currentSet,
            totalSets = state.workout.exercises[state.currentExercise].exercise.prediction.size,
            progress = progress,
            elapsedTime = state.elapsedTime
        )
        if (state.currentExercise < exerciseCount - 1) {
            val next = state.workout.exercises[state.currentExercise + 1]
            WorkoutSessionNextExercise(
                image = next.type.id,
                name = next.type.name,
                exerciseIndex = state.currentExercise + 1,
                exerciseCount = exerciseCount
            )
        }
    }
}


@Composable
fun WorkoutSessionWeights(
    exercise: FullExercise,
    modifier: Modifier = Modifier,
    currentSet: Int,
    expanded: Expanded,
    setExpanded: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(10.dp)
            .clickable { setExpanded() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Weights",
                    fontSize = 20.sp,
                    fontWeight = FontWeight(700)
                )
                Spacer(modifier = Modifier.weight(1f))
                val prediction = exercise.exercise.prediction
                if (currentSet > 0) Text(
                    "${prediction[currentSet - 1]}lbs",
                    fontWeight = FontWeight(100)
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Text("${prediction[currentSet]}lbs", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(5.dp))
                if (currentSet + 1 < prediction.size) Text(
                    "${prediction[currentSet + 1]}lbs",
                    fontWeight = FontWeight(100)
                )
            }
            if (expanded == Expanded.WEIGHTS) {
                Text("Current session weights")
                PredictionField(
                    validPrediction = {})
                Spacer(modifier = Modifier.padding(5.dp))
                Text("Next session weights")
                PredictionField(
                    validPrediction = {})
                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Preview
@Composable
fun WorkoutSessionWeightsPreview() {
    FitCubeTheme(false) {
        Surface {
            WorkoutSessionWeights(
                defaultFullExercise(0),
                currentSet = 0,
                expanded = Expanded.WEIGHTS,
                setExpanded = {})
        }
    }
}

/**
 * Displays the content which consists of the current timer, the actions to pause / continue / skip / previous, embedded into a card. Also contains the total progress of the session
 */
@Composable
fun WorkoutSessionActions(
    onPauseAction: () -> Unit,
    paused: Boolean,
    timer: Int,
    currentSet: Int,
    totalSets: Int,
    progress: Float,
    elapsedTime: Long,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (timer > 0) {
                    parseTimer(timer)
                } else {
                    "Set ${currentSet + 1}/$totalSets"
                },
                fontSize = 50.sp,
                fontWeight = FontWeight(700),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onPauseAction,
                modifier = Modifier.padding(horizontal = 10.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (timer > 0) {
                    if (paused) {
                        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "")
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_pause_24),
                            contentDescription = ""
                        )
                    }
                } else {
                    Icon(imageVector = Icons.Rounded.Done, contentDescription = "")
                }
            }
            Spacer(modifier = Modifier.padding(end = 10.dp))
        }
        Text(
            text = "Ellapsed time: ${parseDuration(elapsedTime)}",
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            strokeCap = StrokeCap.Round
        )
    }
}

@Preview
@Composable
fun WorkoutSessionActionsPreview() {
    FitCubeTheme(false) {
        Surface {
            WorkoutSessionActions(
                onPauseAction = {},
                paused = false,
                progress = 0.5f,
                timer = 83,
                modifier = Modifier.padding(10.dp),
                currentSet = 1,
                totalSets = 4,
                elapsedTime = 3600L + 23 * 60L + 45L
            )
        }
    }
}

@Composable
fun WorkoutSessionNextExercise(
    modifier: Modifier = Modifier,
    image: Int,
    name: String,
    exerciseIndex: Int,
    exerciseCount: Int
) {
    Divider()
    Row(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "Coming up",
                modifier = Modifier.padding(bottom = 5.dp),
                fontWeight = FontWeight.Bold
            )
            Text("$name (${exerciseIndex + 1}/$exerciseCount)", fontStyle = FontStyle.Italic)
        }
        ExerciseIcon(
            id = image, modifier = Modifier
                .fillMaxWidth(0.3f)
                .aspectRatio(1f)
        )
    }
}

@Preview
@Composable
fun WorkoutSessionNextExercisePreview() {
    FitCubeTheme(false) {
        Surface {
            WorkoutSessionNextExercise(
                modifier = Modifier.padding(10.dp),
                image = 0,
                name = "Bench press",
                exerciseCount = 10,
                exerciseIndex = 1
            )
        }
    }
}




