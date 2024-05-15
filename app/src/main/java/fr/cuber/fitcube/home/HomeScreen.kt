package fr.cuber.fitcube.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.HomeWorkout
import fr.cuber.fitcube.data.db.dao.defaultHomeWorkout
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.FitCubeAppBar
import java.util.Date

@Composable
fun HomeScreen(
    openWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val workouts by viewModel.getWorkouts().collectAsState(initial = emptyList())
    var dialog by remember {
        mutableStateOf(false)
    }
    HomeScaffold(openWorkout, {
        dialog = true
    }, workouts, modifier)
    if (dialog) {
        HomeDialog(
            close = { dialog = false },
            addWorkout = { name -> viewModel.addWorkout(name) }
        )
    }
}

@Composable
fun HomeScaffold(
    openWorkout: (Workout) -> Unit,
    openDialog: () -> Unit,
    workouts: List<HomeWorkout>,
    modifier: Modifier = Modifier
){
    Scaffold (
        topBar = {
            FitCubeAppBar(title = stringResource(id = R.string.app_name))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                openDialog()
            }) {
                Icon(Icons.Filled.Add, "Add workout.")
            }
        }
    ) {
        HomeContent(
            openWorkout = openWorkout,
            modifier = modifier.padding(it),
            workouts = workouts
        )
    }
}

@Composable
fun HomeContent(
    openWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier,
    workouts: List<HomeWorkout>
) {
    Column (
        modifier
            .fillMaxSize()
            .padding(16.dp)) {
        workouts.forEach { workout ->
            HomeWorkoutItem(
                workout = workout,
                onClick = { openWorkout(workout.workout) }
            )
            Divider()
        }
        if(workouts.isEmpty()) {
            Text(text = stringResource(id = R.string.no_workout), modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 50.dp))
        }
    }
}

@Composable
fun HomeWorkoutItem(
    workout: HomeWorkout,
    onClick: () -> Unit
) {

    Column(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable { onClick() }) {
        Text(text = workout.workout.name)
        Text(text = if (workout.date != null) {
            val t = ((Date().time - workout.date) / (3600 * 24)).toInt()
            pluralStringResource(id = R.plurals.last_workout_session, t, t)
        } else stringResource(id = R.string.no_workout_session), fontStyle = FontStyle.Italic)
    }
}

@Composable
fun HomeDialog(
    close: () -> Unit,
    addWorkout: (String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = close) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextField(
                    value = name,
                    placeholder = { Text("Workout name") },
                    onValueChange = { name = it },
                    modifier = Modifier.padding(16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { close() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            addWorkout(name)
                            close()
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScaffoldPreview() {
    FitCubeTheme {
        Surface {
            HomeScaffold(
                openWorkout = {},
                openDialog = {},
                workouts = List(5) {
                    defaultHomeWorkout(it)
                }
            )
        }
    }
}

@Preview
@Composable
fun HomeDialogPreview() {
    FitCubeTheme {
        Surface {
            HomeDialog(
                close = {},
                addWorkout = {}
            )
        }
    }
}