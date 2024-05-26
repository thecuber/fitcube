package fr.cuber.fitcube.workout.session

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.PredictionField
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.utils.showPrediction


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
    WorkoutSessionScaffold(
        back = back,
        start = {
            viewModel.start()
        },
        state = state,
        modifier = modifier,
        setCurrentPrediction = { a, b -> viewModel.setCurrentPrediction(a, b) },
        setNextPrediction = { a, b -> viewModel.setNextPrediction(a, b) },
        nextSet = {
            viewModel.nextState()
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionAppBar(
    title: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    waiting: SessionStatus
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.menu_back))
            }
        },
        actions = {
            if (waiting == SessionStatus.WAITING) {
                IconButton(onClick = onStart) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Save modifications"
                    )
                }
            }

        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun WorkoutSessionScaffold(
    back: () -> Unit,
    start: () -> Unit,
    modifier: Modifier = Modifier,
    state: SessionUiState,
    setCurrentPrediction: (List<Double>, Int) -> Unit,
    setNextPrediction: (List<Double>, Int) -> Unit,
    nextSet: () -> Unit
) {
    Scaffold(
        topBar = {
            WorkoutSessionAppBar(
                title = "Workout session",
                onBack = back,
                onStart = start,
                waiting = state.status
            )
        },
        modifier = modifier
    ) {
        WorkoutSessionContent(
            modifier = Modifier.padding(it),
            state = state,
            setNextPrediction = setNextPrediction,
            setCurrentPrediction = setCurrentPrediction,
            nextSet = nextSet
        )
    }
}

@Composable
fun WorkoutSessionActions(state: SessionUiState, next: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.status == SessionStatus.REST) {
            Text(parseTimer(state.restTimer))
        }
        if (state.status == SessionStatus.WAITING) {
            Text("Waiting for session start...")
        }
        if(state.status == SessionStatus.EXERCISE) {
            Button(onClick = next) {
                Text("Rest !")
            }
        }

    }
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    state: SessionUiState,
    setCurrentPrediction: (List<Double>, Int) -> Unit,
    setNextPrediction: (List<Double>, Int) -> Unit,
    nextSet: () -> Unit
) {
    val workout = state.workout
    if (workout.exercises.isEmpty()) {
        return
    }
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        SessionStatistics(state = state)
        ExerciseIcon(
            state.workout.exercises[state.currentExercise].type,
            Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 10.dp)
        )
        Divider()
        WorkoutSessionActions(state = state, next = nextSet)
        Divider()
        WorkoutSessionNextExercise(state = state)
        Divider()
        WorkoutSessionPredictions(
            state = state,
            setCurrentPrediction = { prediction, index ->
                setCurrentPrediction(prediction, index)
            },
            setNextPrediction = { prediction, index ->
                setNextPrediction(prediction, index)
            }
        )
    }
}

@Composable
fun SessionStatistics(state: SessionUiState) {
    var timer by remember { mutableLongStateOf(0) }
    LaunchedEffect(key1 = state.started) {
        timer = 0
    }
    LaunchedEffect(key1 = Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                timer += 1000L
                handler.postDelayed(this, 1000)
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (state.status == SessionStatus.REST) {
                "${parseTimer(state.restTimer)} until next set (${state.currentSet + 1}/${state.workout.exercises[state.currentExercise].exercise.prediction.size})"
            } else {
                "Current set : ${state.currentSet + 1}/${state.workout.exercises[state.currentExercise].exercise.prediction.size}"
            }
        )
        Text(state.workout.exercises[state.currentExercise].type.name)
        if (state.status != SessionStatus.WAITING) {
            Text(
                "Elapsed time ${parseDuration(timer)}", fontStyle = FontStyle.Italic
            )
        }

    }
}

@Composable
fun WorkoutSessionNextExercise(state: SessionUiState) {
    if (state.workout.exercises.size > state.currentExercise + 1) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val exercise = state.workout.exercises[state.currentExercise + 1]
            Column {
                Text("Next exercise : ${exercise.type.name} (${state.currentExercise + 2} / ${state.workout.exercises.size})")
                Text(
                    exercise.type.description,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    fontStyle = FontStyle.Italic
                )
                Text(showPrediction(exercise.exercise.prediction), fontSize = 12.sp)
            }
            ExerciseIcon(
                exercise = exercise.type, modifier = Modifier
                    .aspectRatio(1f)
            )
        }
    }

}

@Composable
fun WorkoutSessionPredictions(
    state: SessionUiState,
    setCurrentPrediction: (List<Double>, Int) -> Unit,
    setNextPrediction: (List<Double>, Int) -> Unit
) {
    var index by remember {
        mutableIntStateOf(state.currentExercise)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                index--
            }, enabled = index > 0) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous exercise"
                )
            }
            Text(state.workout.exercises[index].type.name)
            IconButton(onClick = {
                index++
            }, enabled = index < state.workout.exercises.size - 1) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Previous exercise"
                )
            }
        }
        PredictionField(
            label = "Current sets",
            placeholder = showPrediction(state.workout.exercises[index].exercise.prediction),
            validPrediction = {
                setCurrentPrediction(it, index)
            })
        PredictionField(
            label = "Next sets",
            placeholder = showPrediction(state.predictions[index]),
            validPrediction = {
                setNextPrediction(it, index)
            })
    }
}

/*
@Preview
@Composable
fun WorkoutSessionPredictionsPreview() {
    FitCubeTheme {
        Surface {
            WorkoutSessionPredictions(
                state = defaultSessionUiState(10),
                setCurrentPrediction = { _, _ -> },
                setNextPrediction = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
fun WorkoutSessionNextExercisePreview() {
    FitCubeTheme {
        Surface {
            WorkoutSessionNextExercise(
                state = defaultSessionUiState(10)
            )
        }
    }
}

@Preview
@Composable
fun WorkoutSessionScreenPreview() {
    FitCubeTheme {
        Surface {
            WorkoutSessionScaffold(
                back = {},
                start = {},
                state = defaultSessionUiState(10),
                setNextPrediction = { _, _ -> },
                setCurrentPrediction = { _, _ -> },
                nextSet = {}
            )
        }
    }
}*/



