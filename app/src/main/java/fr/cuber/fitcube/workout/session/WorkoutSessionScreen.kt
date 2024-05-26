package fr.cuber.fitcube.workout.session

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.parseTimer


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
    LaunchedEffect(Unit) {
        Intent(context, WorkoutSessionNotificationService::class.java).also { intent ->
            viewModel.bindService(intent, context)
        }
    }
    LaunchedEffect(workout) {
        viewModel.bindWorkout(workout)
    }

}

/**
 * Displays the content which consists of the current timer, the actions to pause / continue / skip / previous, embedded into a card. Also contains the total progress of the session
 */
@Composable
fun WorkoutSessionActions(
    onPauseAction: () -> Unit,
    paused: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    timer: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = parseTimer(timer), fontSize = 50.sp, fontWeight = FontWeight(700), modifier = Modifier.padding(horizontal = 10.dp))
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onPrevious, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.White)){
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = "")
            }
            Button(onClick = onPauseAction, modifier = Modifier.padding(horizontal = 10.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)){
                if(paused) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "")
                } else {
                    Icon(painter = painterResource(id = R.drawable.baseline_pause_24), contentDescription = "")
                }
            }
            Button(onClick = onNext, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.White)){
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "")
            }
            Spacer(modifier = Modifier.padding(end = 10.dp))
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            strokeCap = StrokeCap.Round
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
                onNext = {},
                onPrevious = {},
                progress = 0.5f,
                timer = 83,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}
