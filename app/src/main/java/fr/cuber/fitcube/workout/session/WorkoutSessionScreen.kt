package fr.cuber.fitcube.workout.session

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.data.db.loadingCollect
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.LoadingFlowContainer
import fr.cuber.fitcube.utils.PredictionField
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.parseTimer
import fr.cuber.fitcube.utils.showPrediction
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun WorkoutSessionScreen(
    workoutId: Int,
    back: () -> Unit,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loadingState by viewModel.state.loadingCollect()
    LaunchedEffect(key1 = Unit) {
        viewModel.onStart(context, workoutId, back)
    }
    val trigger by viewModel.trigger.collectAsState()
    LaunchedEffect(key1 = trigger) {
        println("Recompile trigger: $loadingState") //I guess i need this to have no bug w launching form notification ?
    }
    LoadingFlowContainer(value = loadingState) { state ->
        WorkoutSessionScaffold(
            state = state,
            onPauseAction = {
                viewModel.pauseAction()
            },
            closeSession = {
                viewModel.closeService()
            },
            onRestChange = { viewModel.setRest(it) },
            onCurrentPredictionChange = { a, b -> viewModel.setCurrentPrediction(a, b) },
            onNextWeightChange = { a, b -> viewModel.setNextPrediction(a, b) },
            orderExercises = { viewModel.orderExercises(it) },
            skipPause = { viewModel.skipPause() },
            skipExercise = { viewModel.skipExercise(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScaffold(
    modifier: Modifier = Modifier,
    state: SessionState,
    onPauseAction: () -> Unit,
    closeSession: () -> Unit,
    onRestChange: (Int) -> Unit,
    onCurrentPredictionChange: (List<Double>, Int) -> Unit,
    onNextWeightChange: (List<Double>, Int) -> Unit,
    orderExercises: (List<Int>) -> Unit,
    skipPause: () -> Unit,
    skipExercise: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        state.current().type.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight(700)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    IconButton(
                        onClick = closeSession,
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(horizontal = 10.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) {
        WorkoutSessionContent(
            modifier = modifier.padding(it),
            state = state,
            onPauseAction = onPauseAction,
            onRestChange = onRestChange,
            onCurrentPredictionChange = onCurrentPredictionChange,
            onNextWeightChange = onNextWeightChange,
            orderExercises = orderExercises,
            skipPause = skipPause,
            skipExercise = skipExercise
        )
    }
}

@Preview
@Composable
fun WorkoutSessionContentPreview() {
    FitCubeTheme {
        Surface {
            WorkoutSessionScaffold(
                state = defaultSessionState(10).copy(
                    timer = 83,
                    elapsedTime = 1000 * (3600L + 23 * 60L + 45L)
                ),
                onPauseAction = {},
                closeSession = {},
                onRestChange = {},
                onCurrentPredictionChange = { a, b -> a + b },
                onNextWeightChange = { a, b -> a + b },
                orderExercises = {},
                skipPause = {},
                skipExercise = {}
            )
        }
    }

}

enum class Expanded {
    DEFAULT,
    PREDICTION,
    EXERCISES,
    TIMER
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    state: SessionState,
    onPauseAction: () -> Unit,
    onRestChange: (Int) -> Unit,
    onCurrentPredictionChange: (List<Double>, Int) -> Unit,
    onNextWeightChange: (List<Double>, Int) -> Unit,
    orderExercises: (List<Int>) -> Unit,
    skipPause: () -> Unit,
    skipExercise: (Boolean) -> Unit
) {
    val exerciseCount = state.exerciseCount()
    val progress = if (exerciseCount > 0) {
        state.workout.exercises.map {
            val index = state.order.indexOf(it.exercise.id)
            if (index < state.currentExercise) it.exercise.prediction.size.toFloat()
            else if (index == state.currentExercise) {
                var v = 0f
                if (state.current().exercise.mode == WorkoutMode.TIMED && state.status == SessionStatus.TIMING) {
                    val u = state.current().exercise.prediction[state.currentSet()].toFloat()
                    v = (u - max(state.timer, 0)) / u
                }
                state.currentSet() + v
            } else 0f
        }.sum() / state.workout.exercises.sumOf {
            it.exercise.prediction.size
        }.toFloat()
    } else {
        0f
    }
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
                img = state.current().type.imagePreview(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
        }
        WorkoutSessionPrediction(
            exercises = state.ordered(),
            currentExercise = state.currentExercise,
            currentSet = state.currentSet(),
            expanded = expanded,
            setExpanded = {
                expanded = expanded(expanded, Expanded.PREDICTION)
            },
            onCurrentPredictionChange = { a, b -> onCurrentPredictionChange(a, b) },
            onNextPredictionChange = { a, b -> onNextWeightChange(a, b) },
            nextPredictions = state.predictions
        )
        WorkoutSessionActions(
            onPauseAction = onPauseAction,
            expanded = expanded,
            paused = state.paused,
            timer = state.timer,
            currentSet = state.currentSet(),
            totalSets = state.current().exercise.prediction.size,
            elapsedTime = state.elapsedTime,
            status = state.status,
            rest = state.rest,
            setRest = onRestChange,
            setExpanded = {
                expanded = expanded(expanded, Expanded.TIMER)
            },
            skipPause = skipPause,
            skipExercise = skipExercise
        )
        val progressAnimation by animateFloatAsState(
            targetValue = if (state.status == SessionStatus.DONE) 1f else progress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing), label = "",
        )
        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            strokeCap = StrokeCap.Round,
        )
        if (state.currentExercise < exerciseCount - 1) {
            WorkoutSessionNextExercise(
                exercises = state.ordered(),
                exerciseIndex = state.currentExercise + 1,
                exerciseCount = exerciseCount,
                expanded = expanded,
                setExpanded = {
                    expanded = expanded(expanded, Expanded.EXERCISES)
                },
                orderExercises = orderExercises
            )
        }
    }
}


@Composable
fun WorkoutSessionPrediction(
    exercises: List<FullExercise>,
    nextPredictions: Map<Int, List<Double>>,
    currentExercise: Int,
    modifier: Modifier = Modifier,
    currentSet: Int,
    expanded: Expanded,
    setExpanded: () -> Unit,
    onCurrentPredictionChange: (List<Double>, Int) -> Unit,
    onNextPredictionChange: (List<Double>, Int) -> Unit
) {
    var index by remember {
        mutableIntStateOf(currentExercise)
    }
    LaunchedEffect(key1 = currentExercise, key2 = expanded) {
        if (expanded != Expanded.PREDICTION) {
            index = currentExercise
        }
    }
    val exercise = exercises[index]
    val isExpanded = expanded == Expanded.PREDICTION
    val suffix = if (exercise.exercise.mode == WorkoutMode.TIMED) "s" else "kgs"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(10.dp)
            .clickable { setExpanded() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isExpanded) Arrangement.Center else Arrangement.Start
            ) {
                Text(
                    "Predictions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight(700)
                )
                if (!isExpanded) {
                    Spacer(modifier = Modifier.weight(1f))
                    val prediction = exercise.exercise.prediction
                    if (currentSet > 0) Text(
                        "${prediction[currentSet - 1]}$suffix",
                        fontWeight = FontWeight(200)
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text("${prediction[currentSet]}$suffix", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(5.dp))
                    if (currentSet + 1 < prediction.size) Text(
                        "${prediction[currentSet + 1]}$suffix",
                        fontWeight = FontWeight(200)
                    )
                }
            }
            var checked by remember { mutableStateOf(true) }//If we show the field for next prediction only
            var dialog by remember { mutableStateOf(false) }//If the dialog is shown when not the same nb of sets
            var predi by remember { mutableStateOf(listOf<Double>()) }
            var top by remember { mutableStateOf(false) }
            if (expanded == Expanded.PREDICTION) {

                if (dialog) {
                    AlertDialog(
                        text = {
                            Text("Do you want to change the number of sets ?")
                        },
                        icon = {
                            Icon(Icons.Filled.Warning, contentDescription = null)
                        },
                        onDismissRequest = { dialog = false },
                        dismissButton = {
                            Button(
                                onClick = {
                                    dialog = false
                                }
                            ) {
                                Text("Dismiss")
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (top) {
                                        onCurrentPredictionChange(predi, index)
                                    }
                                    if (checked || !top) {
                                        onNextPredictionChange(predi, exercise.exercise.id)
                                    }
                                    dialog = false
                                }
                            ) {
                                Text("Confirm")
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        index -= 1
                    }, enabled = index > 0) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                    }
                    Text(exercise.type.name)
                    IconButton(onClick = {
                        index += 1
                    }, enabled = index < exercises.size - 1) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = showPrediction(exercise.exercise.prediction))
                }
                PredictionField(
                    validPrediction = {
                        if (it.size != exercise.exercise.prediction.size) {
                            top = true
                            predi = it
                            dialog = true
                        } else {
                            onCurrentPredictionChange(it, index)
                            if (checked) {
                                onNextPredictionChange(it, exercise.exercise.id)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = checked, onCheckedChange = {
                        checked = it
                    })
                    Text("Use same prediction for next session")
                }
                if (!checked) {
                    Text("Next session prediction")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = showPrediction(nextPredictions[exercise.exercise.id]!!))
                    }
                    PredictionField(
                        validPrediction = {
                            if (it.size != nextPredictions[exercise.exercise.id]!!.size) {
                                top = false
                                predi = it
                                dialog = true
                            } else {
                                onNextPredictionChange(it, exercise.exercise.id)
                            }

                        }
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun WorkoutSessionPredictionPreview() {
    var exercises by remember {
        mutableStateOf(List(10) { defaultFullExercise(it) })
    }
    var expanded by remember {
        mutableStateOf(Expanded.PREDICTION)
    }
    var nextPredictions by remember {
        mutableStateOf(List(10) { it }.associateWith { List(4) { _ -> it * 10.0 } })
    }
    FitCubeTheme(false) {
        Surface {
            WorkoutSessionPrediction(
                exercises = exercises,
                currentExercise = 0,
                currentSet = 0,
                expanded = expanded,
                setExpanded = {
                    expanded = expanded(expanded, Expanded.PREDICTION)
                },
                onCurrentPredictionChange = { predictions, index ->
                    exercises = exercises.mapIndexed { a, b ->
                        if (a == index) {
                            b.copy(exercise = b.exercise.copy(prediction = predictions))
                        } else {
                            b
                        }
                    }
                },
                onNextPredictionChange = { a, b ->
                    nextPredictions = nextPredictions.toMutableMap().apply {
                        set(b, a)
                    }
                },
                nextPredictions = nextPredictions
            )
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
    expanded: Expanded,
    status: SessionStatus,
    currentSet: Int,
    rest: Int,
    setRest: (Int) -> Unit,
    totalSets: Int,
    elapsedTime: Long,
    modifier: Modifier = Modifier,
    setExpanded: () -> Unit,
    skipPause: () -> Unit,
    skipExercise: (Boolean) -> Unit
) {
    var openedDialog by remember {
        mutableStateOf(false)
    }
    if (openedDialog) {
        Dialog(onDismissRequest = { openedDialog = false }) {
            Card(
                modifier = Modifier
                    .height(150.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Skipping exercise:",
                        modifier = Modifier.padding(16.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                skipExercise(false)
                                openedDialog = false
                            },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Only one series")
                        }
                        TextButton(
                            onClick = {
                                skipExercise(true)
                                openedDialog = false
                            },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("All remaining")
                        }
                        TextButton(
                            onClick = { openedDialog = false },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
    val started = status != SessionStatus.START
    Column(modifier = modifier
        .clickable { setExpanded() }
        .padding(horizontal = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = when (status) {
                        SessionStatus.START, SessionStatus.REST, SessionStatus.TIMING -> {
                            parseTimer(timer)
                        }

                        SessionStatus.EXERCISE -> {
                            "Set ${currentSet + 1}/$totalSets"
                        }

                        else -> {
                            "Finish"
                        }
                    },
                    fontSize = 50.sp,
                    fontWeight = FontWeight(700),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Text(
                    text = if (started) {
                        "Elapsed time: ${parseDuration(elapsedTime)}"
                    } else {
                        "Starting in..."
                    },
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            FilledIconButton(
                onClick = {
                    if (status == SessionStatus.REST) {
                        skipPause()
                    } else if (currentSet == totalSets - 1) {
                        skipExercise(false)
                    } else {
                        openedDialog = true
                    }
                },
                modifier = Modifier.size(55.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.LightGray)
            ) {
                Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "")
            }
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            FilledIconButton(
                onClick = onPauseAction,
                modifier = Modifier.size(55.dp)
            ) {
                if (timer > 0) {
                    if (paused || status == SessionStatus.EXERCISE || status == SessionStatus.DONE) {
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
        }
        Spacer(modifier = Modifier.padding(horizontal = 10.dp))
        if (expanded == Expanded.TIMER) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                value = rest.toString(),
                maxLines = 1,
                onValueChange = {
                    setRest(it.toIntOrNull() ?: 0)
                },
                suffix = { Text("s") },
                label = { Text("Rest time") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }

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
                timer = 83,
                modifier = Modifier.padding(10.dp),
                currentSet = 1,
                totalSets = 4,
                elapsedTime = 3600L + 23 * 60L + 45L,
                status = SessionStatus.DONE,
                expanded = Expanded.TIMER,
                rest = 30,
                setRest = {},
                setExpanded = {},
                skipPause = {},
                skipExercise = {}
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutSessionNextExercise(
    modifier: Modifier = Modifier,
    expanded: Expanded,
    setExpanded: () -> Unit,
    exerciseIndex: Int,
    exerciseCount: Int,
    exercises: List<FullExercise>,
    preview: Boolean = false,
    orderExercises: (List<Int>) -> Unit
) {
    val localDensity = LocalDensity.current
    var mod = modifier
        .fillMaxWidth()
        .animateContentSize()
        .clickable { setExpanded() }
    mod = if (expanded == Expanded.EXERCISES) {
        if (preview) mod else mod.fillMaxHeight(0.55f)
    } else {
        mod.padding(10.dp)
    }
    Column(
        modifier = mod,
    ) {
        if (expanded == Expanded.EXERCISES) {
            var editable by remember {
                mutableStateOf(false)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            ) {
                Text(
                    "Coming up",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = {
                    editable = !editable
                }) {
                    Icon(Icons.Filled.ModeEdit, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider()
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
                mutableStateOf(exercises.associate { it.exercise.id to 0f})
            }
            var order by remember {
                mutableStateOf(exercises.map { it.exercise.id })
            }
            LazyColumn(
                modifier = Modifier.background(Color.LightGray)
            ) {
                items(exercises.subList(exerciseIndex, exerciseCount)) { exercise ->
                    val id = exercise.exercise.id
                    val index = order.indexOf(id)
                    val animatedOffset by animateFloatAsState(targetValue = dragOffsets[id]!!.first,
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
                                IntOffset(0, (if(dragId == -1 && previousDragId != id) dragOffsets[id]!!.first else animatedOffset).roundToInt())
                            }
                            .combinedClickable(
                                enabled = !editable,
                                onClick = {},
                                onDoubleClick = {
                                    order = order
                                        .toMutableList()
                                        .apply {
                                            this[index] = this[exerciseIndex - 1]
                                            this[exerciseIndex - 1] = id
                                        }
                                    orderExercises(order)
                                }
                            )
                            .draggable(
                                enabled = editable,
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    dragOffsets = dragOffsets
                                        .toMutableMap()
                                        .apply {
                                            val v = this[id]!!
                                            this[id] = Pair(v.first + delta, v.second)
                                        }
                                    //We need to check
                                    if (index > exerciseIndex) {
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
                            .height(IntrinsicSize.Min)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "${exercise.type.name} (${order.indexOf(id) + 1}/$exerciseCount)",
                                    fontSize = 14.sp
                                )
                                Text(
                                    showPrediction(exercise.exercise.prediction),
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp
                                )
                            }
                            Row(
                                modifier = Modifier.weight(0.2f)
                            ) {
                                ExerciseIcon(
                                    img = exercise.type.imagePreview(), modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            }

                        }
                    }
                    //TODO Fix this color
                    HorizontalDivider()
                }
            }
        } else {
            val ex = exercises[exerciseIndex]
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "Coming up",
                        modifier = Modifier.padding(bottom = 5.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${ex.type.name} (${exerciseIndex + 1}/$exerciseCount)",
                        fontStyle = FontStyle.Italic
                    )
                }
                ExerciseIcon(
                    img = ex.type.imagePreview(), modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .aspectRatio(1f)
                )
            }
        }
    }
}


@Preview
@Composable
fun WorkoutSessionNextExercisePreview() {
    /*var exercises by remember {
        mutableStateOf(
            listOf(
                FullExercise(
                    exercise = WorkoutExercise(
                        id = -314,
                        typeId = -314,
                        workoutId = 0,
                        mode = WorkoutMode.TIMED,
                        prediction = listOf(5.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = -314,
                        name = "Warmup",
                        description = "",
                        image = listOf("png/warmup.png")
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 10,
                        typeId = 294,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(5.0, 8.0, 8.0, 8.0, 8.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 294,
                        name = "Lat pulldowns",
                        description = "",
                        image = listOf()
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 11,
                        typeId = 45,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(30.0, 30.0, 30.0, 30.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 45,
                        name = "Dumbbell Bent Arm Pullover",
                        description = "",
                        image = listOf("png/0046-relaxation.png", "png/0046-tension.png")
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 12,
                        typeId = 24,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(40.0, 40.0, 40.0, 40.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 24,
                        name = "Rear Deltoid Row Dumbbell",
                        description = "",
                        image = listOf("png/0024-relaxation.png", "png/0024-tension.png")
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 13,
                        typeId = 203,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(25.0, 25.0, 25.0, 25.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 203,
                        name = "Alternating Hammer Curl with Dumbbell",
                        description = "",
                        image = listOf("png/0213-relaxation.png", "png/0213-tension.png")
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 14,
                        typeId = 213,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(30.0, 30.0, 25.0, 25.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 213,
                        name = "Alternating Bicep Curl with Dumbbell",
                        description = "",
                        image = listOf("png/0223-relaxation.png", "png/0223-tension.png")
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = 22,
                        typeId = 299,
                        workoutId = 3,
                        mode = WorkoutMode.REPETITION,
                        prediction = listOf(5.0, 5.0, 5.0, 5.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = 299,
                        name = "Tirage sur tête",
                        description = "",
                        image = listOf()
                    )
                ),
                FullExercise(
                    exercise = WorkoutExercise(
                        id = -315,
                        typeId = -315,
                        workoutId = 0,
                        mode = WorkoutMode.TIMED,
                        prediction = listOf(5.0),
                        enabled = true
                    ),
                    type = ExerciseType(
                        id = -315,
                        name = "Stretching",
                        description = "",
                        image = listOf("png/warmup.png")
                    )
                )
            )
        )
    }*/
    var exercises by remember {
        mutableStateOf(List(100) { defaultFullExercise(it) })
    }
    FitCubeTheme {
        Surface {
            WorkoutSessionNextExercise(
                exerciseCount = exercises.size,
                exerciseIndex = 1,
                expanded = Expanded.EXERCISES,
                setExpanded = {},
                exercises = exercises,
                preview = true,
                orderExercises = {
                    exercises = exercises.toMutableList().sortedBy { a -> it.indexOf(a.exercise.id) }
                }
            )
        }
    }
}

private fun expanded(current: Expanded, value: Expanded): Expanded =
    if (current == value) Expanded.DEFAULT else value
