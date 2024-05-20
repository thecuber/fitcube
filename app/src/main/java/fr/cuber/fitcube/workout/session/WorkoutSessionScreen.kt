package fr.cuber.fitcube.workout.session

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBarWithBack
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
    WorkoutSessionScaffold(back = back, start = {
        viewModel.start()
    }, state = state, modifier = modifier)
}

@Composable
fun WorkoutSessionScaffold(
    back: () -> Unit,
    start: () -> Unit,
    modifier: Modifier = Modifier,
    state: SessionUiState
) {
    Scaffold(
        topBar = {
            FitCubeAppBarWithBack(title = "Workout session", onBack = back)
        },
        modifier = modifier
    ) {
        WorkoutSessionContent(
            modifier = Modifier.padding(it),
            start = start,
            state = state
        )
    }
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    start: () -> Unit,
    state: SessionUiState
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
        WorkoutSessionNextExercise(state = state)
        Divider()
        if (state.status == SessionStatus.WAITING) {
            Spacer(Modifier.weight(1f))
            Button(onClick = start, modifier = Modifier.padding(vertical = 10.dp)) {
                Text(text = "Start session")
            }
        }
    }
}

@Composable
fun SessionStatistics(state: SessionUiState) {
    var timer by remember { mutableLongStateOf(0) }
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
        Text("Exercise ${state.currentExercise + 1}/${state.workout.exercises.size} : ${state.workout.exercises[state.currentExercise].type.name}")
        Text("Elapsed time ${parseDuration(timer)}", fontStyle = FontStyle.Italic)
    }
}

@Composable
fun WorkoutSessionNextExercise(state: SessionUiState) {
    if (state.workout.exercises.size > state.currentExercise + 1) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val exercise = state.workout.exercises[state.currentExercise + 1]
            Column {
                Text("Next exercise : ${exercise.type.name}")
                Text(exercise.type.description, maxLines = 1, modifier = Modifier.fillMaxWidth(0.8f), fontStyle = FontStyle.Italic)
                Text(showPrediction(exercise.exercise.prediction), fontSize = 12.sp)
            }
            ExerciseIcon(exercise = exercise.type, modifier = Modifier
                .aspectRatio(1f))
        }
    }

}

@Preview
@Composable
fun WorkoutSessionNextExercisePreview() {
    FitCubeTheme {
        Surface {
            WorkoutSessionNextExercise(
                state = SessionUiState(
                    status = SessionStatus.REST, 0, 0, 0, defaultWorkoutWithExercises(10), 0L
                )
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
                state = SessionUiState(
                    status = SessionStatus.REST, 0, 0, 0, defaultWorkoutWithExercises(10), 0L
                )
            )
        }
    }
}
