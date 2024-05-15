package fr.cuber.fitcube.workout.info

import android.annotation.SuppressLint
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.defaultSession
import fr.cuber.fitcube.data.db.entity.defaultWorkout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.DefaultIconParser
import fr.cuber.fitcube.utils.IconParser
import fr.cuber.fitcube.utils.IconParserFun
import fr.cuber.fitcube.utils.parseDuration
import fr.cuber.fitcube.utils.showPrediction
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun WorkoutInfoScreen(
    modifier: Modifier = Modifier,
    back: () -> Unit,
    workoutId: Int,
    openExercise: (WorkoutExercise) -> Unit,
    addExercise: () -> Unit,
    startWorkout: () -> Unit,
    viewModel: WorkoutInfoViewModel = hiltViewModel()
) {
    val workoutExercises by viewModel.getWorkout(workoutId).collectAsState(
        initial = WorkoutWithExercises(
            Workout(0, ""), emptyList()
        )
    )
    val sessions by viewModel.getSessions(workoutId).collectAsState(initial = emptyList())
    WorkoutInfoScaffold(
        workout = workoutExercises.workout,
        startWorkout = startWorkout,
        exercises = workoutExercises.exercises,
        sessions = sessions,
        back = back,
        modifier = modifier,
        openExercise = openExercise,
        addExercise = addExercise,
        icon = { ex, mod ->
            IconParser(
                exercise = ex,
                modifier = mod
            )
        })
}

@Composable
fun WorkoutInfoScaffold(
    workout: Workout,
    exercises: List<FullExercise>,
    sessions: List<Session>,
    openExercise: (WorkoutExercise) -> Unit,
    back: () -> Unit,
    addExercise: () -> Unit,
    startWorkout: () -> Unit,
    icon: IconParserFun,
    modifier: Modifier = Modifier

) {
    Scaffold(
        topBar = {
            WorkoutInfoAppBar(title = workout.name, onBack = back, onAdd = addExercise, onRemove = {})
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
            icon = icon,
            sessions = sessions
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInfoAppBar(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    title: String
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
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Save modifications"
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Save modifications"
                )
            }

        },
        modifier = Modifier.fillMaxWidth()
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun WorkoutInfoStatistics(
    sessions: List<Session>,
    modifier: Modifier = Modifier
) {
    val total = sessions.size
    val last = if(sessions.isEmpty()) { "None" } else {SimpleDateFormat("d MMMM yyyy").format(Date(sessions.last().date)) }
    val estimate = if(sessions.isEmpty()) { 0 } else { sessions.sumOf { it.duration } / sessions.size }
    Column(modifier = modifier.border(1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20)).padding(10.dp)) {
        Text("Total sessions: $total")
        Text("Estimate time: ${parseDuration(estimate)}")
        Text("Last session: $last")
    }
}

@Composable
fun WorkoutInfoContent(
    openExercise: (WorkoutExercise) -> Unit,
    icon: IconParserFun,
    exercises: List<FullExercise>,
    modifier: Modifier = Modifier,
    sessions: List<Session>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkoutInfoStatistics(sessions = sessions, modifier = Modifier.padding(10.dp).fillMaxWidth())
        Divider()
        LazyColumn{
            items(exercises){
                WorkoutInfoExerciseItem(exerciseIcon = icon, exercise = it, modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openExercise(it.exercise) })
            }
        }

    }
}

@Composable
fun WorkoutInfoExerciseItem(
    exerciseIcon: IconParserFun,
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
                    Color.White
                }
            )
        }
        exerciseIcon(exercise.type,
            Modifier
                .fillMaxWidth(0.3f)
                .aspectRatio(1f))
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
                back = {},
                addExercise = {},
                startWorkout = {},
                sessions = List(10) {
                    defaultSession()
                },
                icon = { ex, mod ->
                    DefaultIconParser(exercise = ex.copy(id = 50), modifier = mod)
                }
            )
        }
    }
}