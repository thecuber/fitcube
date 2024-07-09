package fr.cuber.fitcube.workout.info

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.defaultSession
import fr.cuber.fitcube.data.db.entity.defaultWorkout
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBar
import fr.cuber.fitcube.utils.OutlinedTIButton
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.showPrediction
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun WorkoutInfoScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    workoutId: Int,
    openExercise: (WorkoutExercise) -> Unit,
    addExercise: () -> Unit,
    startWorkout: () -> Unit,
    viewModel: WorkoutInfoViewModel = hiltViewModel()
) {
    val workoutExercises by viewModel.getWorkout(workoutId).collectAsState(
        initial = defaultWorkoutWithExercises(5)
    )
    val sessions by viewModel.getSessions(workoutId).collectAsState(initial = emptyList())
    WorkoutInfoScaffold(
        workout = workoutExercises.workout,
        startWorkout = startWorkout,
        exercises = workoutExercises.exercises,
        sessions = sessions,
        onClose = onClose,
        modifier = modifier,
        openExercise = openExercise,
        addExercise = addExercise,
        onRemove = {
            println("Deleting $it")
            viewModel.deleteExercises(it) },
        deleteWorkout = {
            viewModel.deleteWorkout(workoutId)
            onClose()
        }
    )
}

@Composable
fun WorkoutInfoScaffold(
    workout: Workout,
    exercises: List<FullExercise>,
    sessions: List<Session>,
    openExercise: (WorkoutExercise) -> Unit,
    onClose: () -> Unit,
    addExercise: () -> Unit,
    startWorkout: () -> Unit,
    modifier: Modifier = Modifier,
    onRemove: (List<Int>) -> Unit,
    deleteWorkout: () -> Unit
) {
    Scaffold(
        topBar = {
            FitCubeAppBar(title = workout.name, onClose = onClose, actions = mapOf(Icons.Filled.Delete to deleteWorkout))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                startWorkout()
            }) {
                Icon(Icons.Filled.PlayArrow, "Start workout.")
            }
        }
    ) {
        WorkoutInfoContent(
            openExercise = openExercise,
            modifier = modifier.padding(it),
            exercises = exercises,
            sessions = sessions,
            onAdd = addExercise,
            onRemove = onRemove
        )
    }
}


@SuppressLint("SimpleDateFormat")
@Composable
fun WorkoutInfoStatistics(
    sessions: List<Session>,
    modifier: Modifier = Modifier
) {
    val total = sessions.size
    val last = if (sessions.isEmpty()) {
        "None"
    } else {
        SimpleDateFormat("d MMMM yyyy").format(Date(sessions.last().date))
    }
    val estimate = if (sessions.isEmpty()) {
        0
    } else {
        sessions.sumOf { it.duration } / sessions.size
    }
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(10.dp)
    ) {
        Text("Total sessions: $total", color = Color.White)
        Text("Estimate time: ${parseDuration(estimate)}", color = Color.White)
        Text("Last session: $last", color = Color.White)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutInfoContent(
    openExercise: (WorkoutExercise) -> Unit,
    exercises: List<FullExercise>,
    modifier: Modifier = Modifier,
    sessions: List<Session>,
    onAdd: () -> Unit,
    onRemove: (List<Int>) -> Unit,
) {
    val selection = remember { mutableStateOf(listOf<Int>()) }
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkoutInfoStatistics(
            sessions = sessions,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 20.dp)
                .fillMaxWidth()
        )
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTIButton(text = "Add exercise", onClick = onAdd, icon = Icons.Filled.Add)
            OutlinedTIButton(
                text = "Delete exercises",
                onClick = {
                    onRemove(selection.value)
                    selection.value = listOf()
                },
                icon = Icons.Filled.Delete
            )
        }
        LazyColumn {
            items(exercises) {
                WorkoutInfoExerciseItem(exercise = it, modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selection.value.contains(it.exercise.id)) {
                            Color.Gray
                        } else {
                            Color.Transparent
                        }
                    )
                    .combinedClickable(
                        onClick = {
                            if (selection.value.isEmpty()) {
                                openExercise(it.exercise)
                            } else {
                                if (selection.value.contains(it.exercise.id)) {
                                    selection.value =
                                        selection.value.filter { id -> id != it.exercise.id }
                                } else {
                                    selection.value += it.exercise.id
                                }
                            }
                        },
                        onLongClick = {
                            if (selection.value.isEmpty() || !selection.value.contains(it.exercise.id)) {
                                selection.value += it.exercise.id
                            } else {
                                selection.value = selection.value.filter { id -> id != it.exercise.id }
                            }
                        }
                    )
                    .padding(horizontal = 20.dp))

            }
        }

    }
}

@Composable
fun WorkoutInfoExerciseItem(
    exercise: FullExercise,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(vertical = 10.dp)
    ) {
        Column {
            Text(exercise.type.name, fontWeight = FontWeight.Bold)
            Text(
                fontStyle = FontStyle.Italic,
                text = showPrediction(exercise.exercise.prediction, true),
                color = if (exercise.exercise.prediction.isEmpty()) {
                    Color.Red
                } else {
                    Color.Black
                }
            )
        }
        //FIXME NOT SAME SIZE
        ExerciseIcon(
            exercise.type.imagePreview(),
            Modifier
                .fillMaxWidth(0.3f)
                .aspectRatio(1f)
        )
    }
}

@Preview
@Composable
fun WorkoutInfoStatisticsPreview() {
    FitCubeTheme {
        Surface {
            WorkoutInfoStatistics(
                sessions = List(10) {
                    defaultSession()
                }
            )
        }
    }
}

@Preview
@Composable
fun WorkoutInfoScaffoldPreview() {
    FitCubeTheme {
        Surface {
            WorkoutInfoScaffold(
                workout = defaultWorkout(),
                exercises = List(10) {
                    defaultFullExercise(it)
                },
                openExercise = {},
                onClose = {},
                addExercise = {},
                startWorkout = {},
                sessions = List(10) {
                    defaultSession()
                },
                onRemove = {},
                deleteWorkout = {}
            )
        }
    }
}