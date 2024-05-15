package fr.cuber.fitcube.workout.session

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.DefaultIconParser
import fr.cuber.fitcube.utils.FitCubeAppBarWithBack
import fr.cuber.fitcube.utils.IconParser
import fr.cuber.fitcube.utils.IconParserFun


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
    WorkoutSessionScaffold(back = back, start = {
        viewModel.bindWorkout(workout)
        Intent(context, WorkoutSessionNotificationService::class.java).also { intent ->
            viewModel.bindService(intent, context)
        }
    }, state = state, modifier = modifier, exerciseIcon = { ex, mod ->
        IconParser(
            ex, mod
        )
    })
}

@Composable
fun WorkoutSessionScaffold(
    back: () -> Unit,
    start: () -> Unit,
    modifier: Modifier = Modifier,
    exerciseIcon: IconParserFun,
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
            state = state,
            exerciseIcon = exerciseIcon
        )
    }
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    start: () -> Unit,
    state: SessionUiState,
    exerciseIcon: IconParserFun
) {
    val workout = state.workout
    if(workout.exercises.isEmpty()) {
        return
    }
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        exerciseIcon(state.workout.exercises[state.currentExercise].type, Modifier.fillMaxWidth(0.8f).padding(vertical = 10.dp))
        Text(text = workout.exercises[state.currentExercise].type.name)
        if (state.status == SessionStatus.REST) {
            Spacer(Modifier.weight(1f))
            Button(onClick = start, modifier = Modifier.padding(vertical = 10.dp)) {
                Text(text = "Start session")
            }
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
                state = SessionUiState(status = SessionStatus.REST, 0, 0, 0, WorkoutWithExercises(
                    Workout(0, ""), listOf(
                        FullExercise(
                            WorkoutExercise(
                                0,
                                0,
                                0,
                                WorkoutMode.REPETITION,
                                listOf(11.0, 12.0, 13.0, 14.0)
                            ),
                            defaultExerciseType(1)
                        )
                    )
                ), 0L),
                exerciseIcon = { ex, mod -> DefaultIconParser(ex, mod) }
            )
        }
    }
}
