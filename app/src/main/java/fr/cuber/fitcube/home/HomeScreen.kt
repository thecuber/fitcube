package fr.cuber.fitcube.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.HomeWorkout
import fr.cuber.fitcube.data.db.dao.defaultHomeWorkout
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.defaultSession
import fr.cuber.fitcube.data.db.loadingCollect
import fr.cuber.fitcube.exercise.ExerciseList
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.FitCubeAppBar
import fr.cuber.fitcube.utils.LoadingFlowContainer
import fr.cuber.fitcube.workout.WorkoutList

@Composable
fun HomeScreen(
    startWorkout: (Int) -> Unit,
    openWorkout: (Int) -> Unit,
    openExercise: (Int) -> Unit,
    createExercise: () -> Unit,
    openSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val workoutsLoading by viewModel.getWorkouts().loadingCollect()
    val exercisesLoading by viewModel.getExercises().loadingCollect()
    val sessionsLoading by viewModel.getSessions().loadingCollect()
    val context = LocalContext.current
    val name = if(context.getString(R.string.version) == "prod") "Fitcube" else "Fitcube-dev"
    LoadingFlowContainer(
        value = workoutsLoading,
        value2 = exercisesLoading,
        value3 = sessionsLoading
    ) { workouts, exercises, sessions ->
        HomeScaffold(
            workouts = workouts,
            startWorkout = startWorkout,
            openWorkout = openWorkout,
            addWorkout = { viewModel.addWorkout(it) },
            exercises = exercises,
            openExercise = openExercise,
            createExercise = createExercise,
            sessions = sessions,
            openSettings = openSettings,
            title = name
        )
    }

}

@Composable
fun HomeScaffold(
    workouts: List<HomeWorkout>,
    addWorkout: (String) -> Unit,
    startWorkout: (Int) -> Unit,
    openWorkout: (Int) -> Unit,
    exercises: List<ExerciseType>,
    openExercise: (Int) -> Unit,
    openSettings: () -> Unit,
    createExercise: () -> Unit,
    sessions: List<Session>,
    title: String = "FitCube"
) {
    var view by remember { mutableIntStateOf(0) }
    val items = listOf(
        Pair("Home", Icons.Default.Home),
        Pair("Exercises", Icons.Default.FitnessCenter),
        Pair("History", Icons.Default.History)
    )
    Scaffold(
        topBar = {
            FitCubeAppBar(title = title, icon = true, actions = mapOf(Icons.Default.Settings to openSettings))
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, pair ->
                    NavigationBarItem(
                        selected = view == index,
                        onClick = { view = index },
                        icon = { Icon(pair.second, contentDescription = null) },
                        label = { Text(pair.first) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }
        }
    ) { pad ->
        val mod = Modifier.padding(pad)
        Crossfade(targetState = view, label = "") { v ->
            when (v) {
                0 -> WorkoutList(
                    modifier = mod,
                    workouts = workouts,
                    startWorkout = startWorkout,
                    openWorkout = openWorkout,
                    addWorkout = addWorkout
                )

                1 -> ExerciseList(
                    modifier = mod,
                    exercises = exercises,
                    openExercise = openExercise,
                    createExercise = createExercise
                )

                2 -> {
                    Column(modifier = mod.verticalScroll(rememberScrollState())) {
                        Text("History ${sessions.size}")
                        sessions.forEach { s ->
                            Text(text = s.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    FitCubeTheme {
        Surface {
            HomeScaffold(
                workouts = List(10) {
                    defaultHomeWorkout(it)
                },
                startWorkout = {},
                openWorkout = {},
                addWorkout = {},
                exercises = List(10) {
                    defaultExerciseType(it)
                },
                openExercise = {},
                createExercise = {},
                sessions = List(10) {
                    defaultSession()
                },
                openSettings = {}
            )
        }
    }
}

