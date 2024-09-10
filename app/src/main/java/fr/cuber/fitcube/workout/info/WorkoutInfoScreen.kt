package fr.cuber.fitcube.workout.info

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.defaultSession
import fr.cuber.fitcube.data.db.entity.defaultWorkout
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.data.db.loadingCollect
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBar
import fr.cuber.fitcube.utils.LoadingFlow
import fr.cuber.fitcube.utils.LoadingFlowContainer
import fr.cuber.fitcube.utils.OutlinedTIButton
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.showPrediction
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
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
    //for time between delete and close
    val workoutExercisesLoading by viewModel.getWorkout(workoutId).loadingCollect()
    val sessionsLoading by viewModel.getSessions(workoutId).loadingCollect()
    var loader: LoadingFlow<*> = LoadingFlow.Success(null)
    LoadingFlowContainer(
        workoutExercisesLoading, sessionsLoading, loader
    ) { workoutExercises, sessions, _ -> WorkoutInfoScaffold(
        workout = workoutExercises.workout,
        startWorkout = startWorkout,
        exercises = workoutExercises.exercises.sortedBy { if(workoutExercises.workout.order.isNotEmpty()) workoutExercises.workout.order.indexOf(it.exercise.id) else 0 },
        sessions = sessions,
        onClose = onClose,
        modifier = modifier,
        openExercise = openExercise,
        addExercise = addExercise,
        onRemove = {
            println("Deleting $it")
            viewModel.deleteExercises(it) },
        deleteWorkout = {
            loader = LoadingFlow.Loading
            viewModel.deleteWorkout(workoutId)
            onClose()
        },
        onMove = {
            from, to -> viewModel.moveOrder(workoutExercises.workout.order.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }, workoutId)
        },
        onUpdateWarmup = {
            viewModel.updateWarmup(workoutId, it)
        }
    )
    }
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
    deleteWorkout: () -> Unit,
    onMove: (ItemPosition, ItemPosition) -> Unit,
    onUpdateWarmup: (Int) -> Unit
) {
    var delete by remember { mutableStateOf(false) }
    if(delete) {
        AlertDialog(
            icon = {
                Icon(Icons.Filled.Warning, contentDescription = "Example Icon")
            },
            title = {
                Text(text = "Are you sure you want to delete the following workout ?")
            },
            onDismissRequest = {
                delete = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteWorkout()
                        delete = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        delete = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
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
            onRemove = onRemove,
            onMove = onMove,
            updateWarmup = onUpdateWarmup,
            warmup = workout.warmup
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

@Composable
fun WorkoutInfoContent(
    openExercise: (WorkoutExercise) -> Unit,
    exercises: List<FullExercise>,
    modifier: Modifier = Modifier,
    sessions: List<Session>,
    onAdd: () -> Unit,
    onRemove: (List<Int>) -> Unit,
    onMove: (ItemPosition, ItemPosition) -> Unit,
    updateWarmup: (Int) -> Unit,
    warmup: Int
) {
    var delete by remember { mutableStateOf(false) }
    val selection = remember { mutableStateOf(listOf<Int>()) }
    val state = rememberReorderableLazyListState(onMove = onMove)
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Warmup")
                Checkbox(checked = (warmup and 1) > 0, onCheckedChange = {updateWarmup(warmup xor 1)})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stretching")
                Checkbox(checked = (warmup and 2) > 0, onCheckedChange = {updateWarmup(warmup xor 2)})
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTIButton(text = if(delete) "Cancel" else "Add exercise", onClick = {
                if(delete) {
                    selection.value = listOf()
                    delete = false
                }else {
                    onAdd()
                }
            }, icon = if(delete) Icons.Filled.Cancel else Icons.Filled.Add)
            OutlinedTIButton(
                text = "Delete " + (if(delete) selection.value.size.toString() + " " else "") + "exercises",
                onClick = {
                    if(delete) {
                        onRemove(selection.value)
                        selection.value = listOf()
                        delete = false
                    } else {
                        delete = true
                    }
                },
                icon = Icons.Filled.Delete
            )
        }

        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(exercises, { it.exercise.id }) { item ->
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(if(selection.value.contains(item.exercise.id)) Color.Gray else MaterialTheme.colorScheme.surface)
                    ) {
                        WorkoutInfoExerciseItem(exercise = item, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if(delete) {
                                    if(selection.value.contains(item.exercise.id)) {
                                        selection.value = selection.value.filter { it != item.exercise.id }
                                    } else {
                                        selection.value += item.exercise.id
                                    }
                                } else {
                                    openExercise(item.exercise)
                                }
                            }
                            .padding(horizontal = 20.dp))
                    }
                }
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
        Column(
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
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
        ExerciseIcon(
            exercise.type.imagePreview(),
            Modifier
                .fillMaxWidth(1f)
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
    var order by remember {
        mutableStateOf(List(10) { it })
    }
    FitCubeTheme {
        Surface {
            WorkoutInfoScaffold(
                workout = defaultWorkout(),
                exercises = List(10) {
                    defaultFullExercise(it)
                }.sortedBy { order.indexOf(it.type.id) },
                openExercise = {},
                onClose = {},
                addExercise = {},
                startWorkout = {},
                sessions = List(10) {
                    defaultSession()
                },
                onRemove = {},
                deleteWorkout = {},
                onMove = { from, to ->
                    order = order.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                },
                onUpdateWarmup = {}
            )
        }
    }
}