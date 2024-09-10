package fr.cuber.fitcube.workout.session

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
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
        println("Recompile trigger: ${loadingState.toString()}")
    }
    LoadingFlowContainer(value = loadingState) { state ->
        println("State is loaded")
        WorkoutSessionScaffold(
            state = state,
            onPauseAction = {
                viewModel.pauseAction()
            },
            closeSession = {
                viewModel.closeService()
            },
            onRestChange = { viewModel.setRest(it) },
            onCurrentWeightChange = { a, b -> viewModel.setCurrentPrediction(a, b) },
            onNextWeightChange = { a, b -> viewModel.setNextPrediction(a, b) }
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
    onCurrentWeightChange: (List<Double>, Int) -> Unit,
    onNextWeightChange: (List<Double>, Int) -> Unit
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
                    OutlinedButton(
                        onClick = closeSession,
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
            onPauseAction = onPauseAction,
            onRestChange = onRestChange,
            onCurrentWeightChange = onCurrentWeightChange,
            onNextWeightChange = onNextWeightChange
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
                    currentSet = 1,
                    timer = 83,
                    elapsedTime = 1000 * (3600L + 23 * 60L + 45L)
                ),
                onPauseAction = {},
                closeSession = {},
                onRestChange = {},
                onCurrentWeightChange = { a, b -> a + b },
                onNextWeightChange = { a, b -> a + b }
            )
        }
    }

}

enum class Expanded {
    DEFAULT,
    WEIGHTS,
    EXERCISES,
    TIMER
}

@Composable
fun WorkoutSessionContent(
    modifier: Modifier = Modifier,
    state: SessionState,
    onPauseAction: () -> Unit,
    onRestChange: (Int) -> Unit,
    onCurrentWeightChange: (List<Double>, Int) -> Unit,
    onNextWeightChange: (List<Double>, Int) -> Unit
) {
    val exerciseCount = state.exerciseCount()
    val progress = if (exerciseCount > 0) {
        state.workout.exercises.mapIndexed { index, it ->
            if (index < state.currentExercise) it.exercise.prediction.size.toFloat()
            else if (index == state.currentExercise) {
                var v = 0f
                if(state.current().exercise.mode == WorkoutMode.TIMED && state.status == SessionStatus.TIMING) {
                    val u = state.current().exercise.prediction[state.currentSet].toFloat()
                    v = (u - max(state.timer, 0)) / u
                }
                state.currentSet + v
            }
            else 0f
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
        var exerciseIndex by remember {
            mutableIntStateOf(state.currentExercise)
        }
        LaunchedEffect(key1 = state.currentExercise) {
            exerciseIndex = state.currentExercise
        }
        WorkoutSessionWeights(
            exercise = state.current(),
            currentSet = state.currentSet,
            expanded = expanded,
            setExpanded = {
                expanded = if (expanded == Expanded.WEIGHTS) {
                    Expanded.DEFAULT
                } else {
                    Expanded.WEIGHTS
                }
            },
            onCurrentWeightChange = { onCurrentWeightChange(it, exerciseIndex) },
            onNextWeightChange = { onNextWeightChange(it, exerciseIndex) }
        )
        WorkoutSessionActions(
            onPauseAction = onPauseAction,
            expanded = expanded,
            paused = state.paused,
            timer = state.timer,
            currentSet = state.currentSet,
            totalSets = state.current().exercise.prediction.size,
            progress = progress,
            elapsedTime = state.elapsedTime,
            status = state.status,
            rest = state.rest,
            setRest = onRestChange,
            setExpanded = {
                expanded = if (expanded == Expanded.TIMER) {
                    Expanded.DEFAULT
                } else {
                    Expanded.TIMER
                }
            },
        )
        if (state.currentExercise < exerciseCount - 1) {
            WorkoutSessionNextExercise(
                exercises = state.workout.exercises,
                exerciseIndex = state.currentExercise + 1,
                exerciseCount = exerciseCount,
                expanded = expanded,
                onSwap = { a, b -> a + b },
                setExpanded = {
                    expanded = if (expanded == Expanded.EXERCISES) {
                        Expanded.DEFAULT
                    } else {
                        Expanded.EXERCISES
                    }
                }
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
    setExpanded: () -> Unit,
    onCurrentWeightChange: (List<Double>) -> Unit,
    onNextWeightChange: (List<Double>) -> Unit
) {
    //TODO animation for text slide
    //TODO Carousel to update all exercises
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
                    "${prediction[currentSet - 1]}kgs",
                    fontWeight = FontWeight(200)
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Text("${prediction[currentSet]}kgs", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(5.dp))
                if (currentSet + 1 < prediction.size) Text(
                    "${prediction[currentSet + 1]}kgs",
                    fontWeight = FontWeight(200)
                )
            }
            var checked by remember { mutableStateOf(true) }
            if (expanded == Expanded.WEIGHTS) {
                Text("Current session weights")
                PredictionField(
                    validPrediction = {
                        onCurrentWeightChange(it)
                        if(checked) onNextWeightChange(it)
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
                    Text("Use same weights for next session")
                }
                if(!checked) {
                    Text("Next session weights")
                    PredictionField(
                        validPrediction = onNextWeightChange
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                }
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
                setExpanded = {},
                onCurrentWeightChange = {},
                onNextWeightChange = {})
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
    progress: Float,
    elapsedTime: Long,
    modifier: Modifier = Modifier,
    setExpanded: () -> Unit
) {
    val started = status != SessionStatus.START
    val progressAnimation by animateFloatAsState(
        targetValue = if(status == SessionStatus.DONE) 1f else progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing), label = "",
    )
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
        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            strokeCap = StrokeCap.Round,
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
                elapsedTime = 3600L + 23 * 60L + 45L,
                status = SessionStatus.DONE,
                expanded = Expanded.TIMER,
                rest = 30,
                setRest = {},
                setExpanded = {}
            )
        }
    }
}

@Composable
fun WorkoutSessionNextExercise(
    modifier: Modifier = Modifier,
    expanded: Expanded,
    setExpanded: () -> Unit,
    exerciseIndex: Int,
    exerciseCount: Int,
    exercises: List<FullExercise>,
    onSwap: (Int, Int) -> Unit
) {
    //TODO Add swap mechanic
    var mod = modifier
        .padding(10.dp)
        .fillMaxWidth()
        .animateContentSize()
        .clickable { setExpanded() }
    if (expanded == Expanded.EXERCISES) {
        mod = mod.fillMaxHeight(0.3f)
    }
    Column(
        modifier = mod,
    ) {
        if (expanded == Expanded.EXERCISES) {
            Text(
                "Coming up",
                modifier = Modifier.padding(bottom = 5.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            HorizontalDivider()
            LazyColumn {
                itemsIndexed(exercises.subList(exerciseIndex, exerciseCount)) { id, exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${exercise.type.name} (${exerciseIndex + 1 + id}/$exerciseCount)")
                            Text(
                                showPrediction(exercise.exercise.prediction),
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp
                            )
                        }
                        ExerciseIcon(
                            img = exercise.type.imagePreview(), modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .aspectRatio(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        } else {
            val ex = exercises[exerciseIndex]
            Row(
                modifier = Modifier
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
    FitCubeTheme {
        Surface {
            WorkoutSessionNextExercise(
                modifier = Modifier.padding(10.dp),
                exerciseCount = 10,
                exerciseIndex = 1,
                expanded = Expanded.DEFAULT,
                setExpanded = {},
                exercises = List(10) { defaultFullExercise(it) },
                onSwap = { a, b -> a + b }
            )
        }
    }
}




