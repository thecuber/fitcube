package fr.cuber.fitcube.workout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.HomeWorkout
import fr.cuber.fitcube.data.db.dao.defaultHomeWorkout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.ui.theme.customColors
import fr.cuber.fitcube.utils.parseDuration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

@Composable
fun WorkoutList(
    workouts: List<HomeWorkout>,
    modifier: Modifier = Modifier,
    startWorkout: (Int) -> Unit,
    openWorkout: (Int) -> Unit,
    addWorkout: (String) -> Unit
) {
    var dialog by remember {
        mutableStateOf(false)
    }
    if (dialog) {
        WorkoutListAddDialog(
            close = { dialog = false },
            addWorkout = { name -> addWorkout(name) }
        )
    }
    var selected by remember {
        mutableIntStateOf(
            -1
        )
    }
    LaunchedEffect(workouts) {
        if(workouts.isNotEmpty() && selected == -1) {
            selected = workouts.sortedBy { it.date }[0].workout.id
        }
    }
    val workout = workouts.find { it.workout.id == selected }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        WorkoutListContent(
            homeWorkouts = workouts.sortedBy { it.date },
            selected = selected,
            onSessionSelected = {
                selected = it
            },
            openPopup = {
                dialog = true
            },
            modifier = Modifier.fillMaxHeight(0.7f)
        )
        HorizontalDivider()
        WorkoutListDetails(
            workout = workout,
            modifier = Modifier.weight(1f),
            startWorkout = { startWorkout(selected) },
            openWorkout = { openWorkout(selected) }
        )
    }
}

/**
 * A card for a certain workout
 */
@Composable
fun WorkoutListItem(
    selected: Boolean,
    name: String,
    date: Long?,
    selectSession: () -> Unit
) {
    val bgColor = animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.customColors.unselectedContainer
        }, label = "bgColor",
        animationSpec = tween(500, 0, LinearEasing)
    )
    Card(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clickable { selectSession() },
        colors = CardDefaults.cardColors(
            containerColor = bgColor.value,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Column {
                Text(name, fontWeight = FontWeight(700), fontSize = 20.sp)
                Text(
                    text = if (date != null) {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val last = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val days = last.daysUntil(today)
                        if(days > 1) {
                            pluralStringResource(id = R.plurals.last_workout_session, days, days)
                        } else if (days == 0){
                            "Last session: Today"
                        } else {
                            "Last session: Yesterday"
                        }
                    } else stringResource(id = R.string.no_workout_session),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

/**
 * The composable that contains the list of workouts
 */
@Composable
fun WorkoutListContent(
    homeWorkouts: List<HomeWorkout>,
    selected: Int,
    onSessionSelected: (Int) -> Unit,
    openPopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            homeWorkouts.sortedBy { it.date }.forEach {
                val index = it.workout.id
                WorkoutListItem(
                    selected = index == selected,
                    name = it.workout.name,
                    date = it.date,
                    selectSession = { onSessionSelected(index) }
                )
            }
        }
        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth()
                .clickable { openPopup() },
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Create workout", fontWeight = FontWeight(700), fontSize = 20.sp)
                Icon(imageVector = Icons.Outlined.AddCircle, contentDescription = "")
            }
        }
    }
}

@Composable
@Preview
fun HomeWorkoutListPreview() {
    FitCubeTheme {
        Surface {
            WorkoutListContent(homeWorkouts = List(5) {
                defaultHomeWorkout(it)
            }, selected = 2, onSessionSelected = {}, openPopup = {})
        }
    }
}

/**
 * Bottom part of screen, details for the selected workout, and links to action
 */
@Composable
fun WorkoutListDetails(
    modifier: Modifier = Modifier,
    startWorkout: () -> Unit,
    openWorkout: () -> Unit,
    workout: HomeWorkout?
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(workout != null) {
                Text(
                    text = workout.workout.name,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exercises: ${workout.exerciseCount}",
                    fontSize = 16.sp
                )
                Text(
                    text = "Estimated time: ${parseDuration(workout.estimated)}",
                    fontSize = 16.sp
                )
            } else {
                Text(
                    text = "No workout created for now",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = startWorkout, enabled = workout != null && workout.workout.status) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Start")
            }
            Button(onClick = openWorkout, enabled = workout != null) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Edit")
            }
        }
    }
}

@Composable
@Preview
fun WorkoutListDetailsPreview() {
    FitCubeTheme {
        Surface {
            WorkoutListDetails(
                startWorkout = {},
                openWorkout = {},
                workout = defaultHomeWorkout(0)
            )
        }
    }
}


/**
 * Dialog to create a new workout
 */
@Composable
fun WorkoutListAddDialog(
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