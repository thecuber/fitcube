package fr.cuber.fitcube.workout.info

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import fr.cuber.fitcube.utils.LoadingFlowContainer
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.showPrediction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun WorkoutInfoScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    deleteWorkout: () -> Unit,
    workoutId: Int,
    openExercise: (WorkoutExercise) -> Unit,
    addExercise: () -> Unit,
    startWorkout: () -> Unit,
    viewModel: WorkoutInfoViewModel = hiltViewModel()
) {
    val workoutExercisesLoading by viewModel.getWorkout(workoutId).loadingCollect()
    val sessionsLoading by viewModel.getSessions(workoutId).loadingCollect()
    LoadingFlowContainer(
        workoutExercisesLoading, sessionsLoading
    ) { workoutExercises, sessions ->
        val workout =
            workoutExercises.workout.copy(order = workoutExercises.workout.order.ifEmpty { workoutExercises.exercises.map { it.exercise.id } })
        WorkoutInfoScaffold(
            workout = workout,
            startWorkout = startWorkout,
            exercises = workoutExercises.exercises.sortedBy {
                if (workoutExercises.workout.order.isNotEmpty()) workoutExercises.workout.order.indexOf(
                    it.exercise.id
                ) else 0
            },
            sessions = sessions,
            onClose = onClose,
            modifier = modifier,
            openExercise = openExercise,
            addExercise = addExercise,
            deleteWorkout = deleteWorkout,
            orderExercises = {
                viewModel.moveOrder(it, workoutId)
            },
            archiveExercise = { a, b ->
                viewModel.archiveExercise(a, b)
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
    deleteWorkout: () -> Unit,
    orderExercises: (List<Int>) -> Unit,
    archiveExercise: (Int, Boolean) -> Unit
) {
    val sbHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var delete by remember { mutableStateOf(false) }
    if (delete) {
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
    var visibility by remember {
        mutableStateOf(false)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = sbHostState)
        },
        topBar = {
            FitCubeAppBar(
                title = workout.name,
                onClose = onClose,
                actions = mapOf((if (visibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff) to {
                    visibility = !visibility
                }, Icons.Filled.Add to addExercise, Icons.Filled.Delete to { delete = true })
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                startWorkout()
            }) {
                Icon(Icons.Filled.PlayArrow, "Start workout.")
            }
        }
    ) { it ->
        WorkoutInfoContent(
            openExercise = openExercise,
            modifier = modifier.padding(it),
            exercises = exercises.filter { ex -> visibility || ex.exercise.enabled },
            sessions = sessions,
            orderExercises = orderExercises,
            archiveExercise = { id ->
                val ex = exercises.find { it.exercise.id == id }!!
                if (!ex.exercise.enabled) {
                    archiveExercise(id, true)
                } else {
                    archiveExercise(id, false)
                    scope.launch {
                        val result = sbHostState
                            .showSnackbar(
                                message = "Exercise archived",
                                actionLabel = "Undo",
                                // Defaults to SnackbarDuration.Short
                                duration = SnackbarDuration.Indefinite
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                archiveExercise(id, true)
                            }

                            SnackbarResult.Dismissed -> {

                            }
                        }
                    }
                }
            }
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
    orderExercises: (List<Int>) -> Unit,
    archiveExercise: (Int) -> Unit
) {
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
        val localDensity = LocalDensity.current
        var previousDragId by remember {
            mutableIntStateOf(-1)
        }
        var dragId by remember {
            mutableIntStateOf(-1)
        }
        //First value is the total offset, second is how much we remove from previous drag
        var dragOffsets by remember {
            mutableStateOf(exercises.associate { it.exercise.id to Pair(0f, 0f) })
        }
        var dragHeights by remember {
            mutableStateOf(exercises.associate { it.exercise.id to 0f })
        }
        var order by remember {
            mutableStateOf(exercises.map { it.exercise.id })
        }
        LaunchedEffect(exercises) {
            order = exercises.map { it.exercise.id }
            dragOffsets = exercises.associate { it.exercise.id to Pair(0f, 0f) }
            dragHeights = exercises.associate { it.exercise.id to 0f }.toMutableMap()
        }
        LazyColumn(
            modifier = Modifier.background(Color.LightGray)
        ) {
            items(exercises) { exercise ->
                val id = exercise.exercise.id
                val index = order.indexOf(id)
                if(dragOffsets[id] != null) {

                    val animatedOffset by animateFloatAsState(
                        targetValue = dragOffsets[id]!!.first,
                        label = ""
                    )
                    Column(
                        modifier = Modifier
                            .zIndex(if (dragId == id) 1f else 0f)
                            .fillMaxWidth()
                            .onGloballyPositioned { c ->
                                dragHeights = dragHeights
                                    .toMutableMap()
                                    .apply {
                                        this[id] = with(localDensity) {
                                            c.size.height
                                                .toDp()
                                                .toPx()
                                        }
                                    }
                            }
                            .offset {
                                IntOffset(
                                    0,
                                    (if (dragId == -1 && previousDragId != id) dragOffsets[id]!!.first else animatedOffset).roundToInt()
                                )
                            }
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    dragOffsets = dragOffsets
                                        .toMutableMap()
                                        .apply {
                                            val v = this[id]!!
                                            this[id] = Pair(v.first + delta, v.second)
                                        }
                                    if (index > 0) {
                                        val prevId = order[index - 1]
                                        val offset = dragOffsets[id]!!
                                        val offsetDiff = offset.first - offset.second
                                        if (offsetDiff < 0 && dragHeights[prevId]!! / 2 < -offsetDiff) {
                                            order = order
                                                .toMutableList()
                                                .apply {
                                                    this[index] = prevId
                                                    this[index - 1] = id
                                                }
                                            dragOffsets = dragOffsets
                                                .toMutableMap()
                                                .apply {
                                                    val v =
                                                        dragHeights[prevId]!! / 2 + dragHeights[id]!! / 2
                                                    this[prevId] =
                                                        Pair(this[prevId]!!.first + v, 0f)
                                                    this[id] = Pair(offset.first, offset.second - v)
                                                }
                                        }
                                    }
                                    if (index < order.size - 1) {
                                        val nextId = order[index + 1]
                                        val offset = dragOffsets[id]!!
                                        val offsetDiff = offset.first - offset.second
                                        if (offsetDiff > 0 && dragHeights[nextId]!! / 2 < offsetDiff) {
                                            order = order
                                                .toMutableList()
                                                .apply {
                                                    this[index] = nextId
                                                    this[index + 1] = id
                                                }
                                            dragOffsets = dragOffsets
                                                .toMutableMap()
                                                .apply {
                                                    val v =
                                                        dragHeights[nextId]!! / 2 + dragHeights[id]!! / 2
                                                    this[nextId] =
                                                        Pair(this[nextId]!!.first - v, 0f)
                                                    this[id] = Pair(offset.first, offset.second + v)
                                                }
                                        }
                                    }
                                },
                                onDragStarted = {
                                    dragId = id
                                    previousDragId = -1
                                },
                                onDragStopped = {
                                    dragOffsets = dragOffsets
                                        .toMutableMap()
                                        .mapValues { Pair(0f, 0f) }
                                    orderExercises(order)
                                    dragId = -1
                                    previousDragId = id
                                }
                            )
                            //.height(IntrinsicSize.Min)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        WorkoutInfoExerciseItem(
                            exercise,
                            openExercise = openExercise,
                            archiveExercise = archiveExercise
                        )
                    }
                    HorizontalDivider()
                }
                }
        }
    }
}

@Composable
fun WorkoutInfoExerciseItem(
    exercise: FullExercise,
    openExercise: (WorkoutExercise) -> Unit,
    archiveExercise: (Int) -> Unit
) {
    val localDensity = LocalDensity.current
    var height by remember {
        mutableStateOf(0.dp)
    }
    var delta by remember {
        mutableFloatStateOf(0f)
    }
    val animatedDelta by animateFloatAsState(
        targetValue = delta,
        label = ""
    )
    Box {
        Row(
            modifier = Modifier
                .zIndex(0f)
                .fillMaxWidth(1f)
                .height(height),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .background(Color.Green)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clickable {
                        delta = 0f
                        archiveExercise(exercise.exercise.id)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .onGloballyPositioned {
                    height = with(localDensity) {
                        it.size.height.toDp()
                    }
                }
                .offset { IntOffset(animatedDelta.toInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .draggable(orientation = Orientation.Horizontal, state = rememberDraggableState {
                    delta += it
                    with(localDensity) {
                        val v = height.toPx()
                        if (-delta > v) {
                            delta = -v
                        }
                        if (delta > 0f) {
                            delta = 0f
                        }
                    }
                },
                    onDragStopped = {
                        with(localDensity) {
                            delta = if (-delta > height.toPx() * 3 / 4) {
                                -height.toPx()
                            } else {
                                0f
                            }
                        }
                    })
                .clickable {
                    openExercise(exercise.exercise)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(start = 20.dp)
            ) {
                Text(
                    exercise.type.name + (if(!exercise.exercise.enabled) " (Archived)" else ""),
                    fontWeight = if (exercise.exercise.enabled) FontWeight.Bold else FontWeight.Normal
                )
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
                    .padding(end = 20.dp)
            )
        }
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
                deleteWorkout = {},
                orderExercises = {
                    order = it
                },
                archiveExercise = { _, _ ->
                }
            )
        }
    }
}