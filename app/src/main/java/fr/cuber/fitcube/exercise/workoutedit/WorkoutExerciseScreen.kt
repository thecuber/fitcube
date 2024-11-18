package fr.cuber.fitcube.exercise.workoutedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Switch
import fr.cuber.fitcube.R
import fr.cuber.fitcube.data.db.dao.FullExercise
import fr.cuber.fitcube.data.db.dao.defaultFullExercise
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import fr.cuber.fitcube.data.db.entity.imagePreview
import fr.cuber.fitcube.data.db.loadingCollect
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBar
import fr.cuber.fitcube.utils.LoadingFlowContainer
import fr.cuber.fitcube.utils.PredictionField
import fr.cuber.fitcube.utils.showPrediction


@Composable
fun WorkoutExerciseScreen(
    back: () -> Unit,
    id: Int,
    modifier: Modifier = Modifier,
    viewModel: WorkoutExerciseViewModel = hiltViewModel()
) {
    val exerciseLoading by viewModel.getExercise(id).loadingCollect()
    LoadingFlowContainer(value = exerciseLoading) { exercise ->
        WorkoutExerciseScaffold(
            exercise = exercise,
            setExercise = {
                viewModel.saveWorkoutExercise(it.exercise) },
            modifier = modifier,
            back = {
                back()
            }
        )
    }

}

@Composable
fun WorkoutExerciseScaffold(
    modifier: Modifier = Modifier,
    exercise: FullExercise,
    setExercise: (FullExercise) -> Unit,
    back: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitCubeAppBar(
                title = exercise.type.name,
                onClose = back,
            )
        }
    ) {
        WorkoutExerciseContent(
            exercise = exercise,
            modifier = modifier.padding(it),
            setExercise = setExercise
        )

    }
}

@Composable
fun WorkoutExerciseContent(
    modifier: Modifier = Modifier,
    exercise: FullExercise,
    setExercise: (FullExercise) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExerciseIcon(exercise.type.imagePreview(), Modifier)
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            text = exercise.type.description, fontStyle = FontStyle.Italic, modifier = Modifier
                .weight(1f)
                .verticalScroll(
                    rememberScrollState()
                )
        )
        ExerciseModeSelect(mode = exercise.exercise.mode) {
            setExercise(exercise.copy(exercise = exercise.exercise.copy(mode = it)))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
        Text(text = "Prediction for current session:")
        Text(
            showPrediction(
                exercise.exercise.prediction
            )
        )
        Spacer(modifier = Modifier.padding(5.dp))
        PredictionField(validPrediction = {
            setExercise(exercise.copy(exercise = exercise.exercise.copy(prediction = it)))
        }, top = false)
    }
}

@Composable
fun ExerciseModeSelect(
    mode: WorkoutMode,
    onModeChange: (WorkoutMode) -> Unit
) {
    Row {
        Icon(
            painterResource(id = R.drawable.baseline_access_time_24),
            contentDescription = "",
            tint = if (mode == WorkoutMode.TIMED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Text(text = "Timed")
        Spacer(modifier = Modifier.padding(5.dp))
        Switch(
            checked = mode == WorkoutMode.LOADED_REPETITION,
            onCheckedChange = { onModeChange(if (it) WorkoutMode.LOADED_REPETITION else WorkoutMode.TIMED) })
        Spacer(modifier = Modifier.padding(5.dp))
        Text(text = "Repetition")
        Spacer(modifier = Modifier.padding(5.dp))
        Icon(
            painterResource(id = R.drawable.baseline_123_24),
            contentDescription = "",
            tint = if (mode == WorkoutMode.LOADED_REPETITION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun ExerciseModeSelectPreview() {
    var mode = WorkoutMode.TIMED
    FitCubeTheme {
        Surface {
            ExerciseModeSelect(
                mode,
                onModeChange = { mode = it }
            )
        }
    }
}

@Preview
@Composable
fun WorkoutExerciseContentPreview() {
    val exercise = defaultFullExercise(50)
    FitCubeTheme {
        Surface {
            WorkoutExerciseScaffold(exercise = exercise.copy(
                type = exercise.type.copy(
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam et lacus auctor, gravida quam sed, vulputate dolor. Curabitur aliquet dignissim eros ut lacinia. Integer dapibus nisl eget lorem sollicitudin, in hendrerit risus sagittis. Maecenas convallis imperdiet lacus, id pellentesque lacus gravida vitae. Ut rutrum est nec sapien finibus, vitae vulputate nulla convallis. Vivamus eu sapien et justo bibendum lacinia. Donec metus purus, pharetra sed orci sed, maximus tristique velit. Proin vel mi est. Cras a metus eget augue blandit faucibus. Vestibulum nec fringilla dolor. Ut tempor lectus non turpis consectetur viverra. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed sapien dolor, tempor et pharetra id, tincidunt non odio. Nulla facilisi. Nulla ut porttitor erat. Quisque eu feugiat metus.\n" +
                            "\n" +
                            "Nulla id eros et ligula rutrum egestas at quis tortor. Suspendisse in urna non lectus iaculis bibendum. Cras libero nulla, suscipit at odio in, convallis pharetra elit. Integer eget arcu nec sem viverra dignissim et sit amet leo. Duis molestie sit amet eros in aliquam. Phasellus eu nisi facilisis, tempus tortor tincidunt, mollis sapien. Phasellus non cursus ex. Nullam rhoncus vitae est ut posuere. Ut volutpat augue sed luctus pellentesque. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec nec lectus eu felis feugiat porttitor.\n" +
                            "\n" +
                            "Nam lacinia consequat urna, quis facilisis arcu tempus porta. Praesent venenatis dapibus leo ac accumsan. In mauris sapien, vehicula a ornare id, pharetra quis nisi. Morbi blandit mi vel lectus rhoncus, a molestie dui iaculis. Duis venenatis iaculis sapien at rutrum. Quisque et orci arcu. Morbi hendrerit fringilla mattis. Fusce cursus libero quis lorem convallis, semper iaculis arcu pellentesque.\n" +
                            "\n" +
                            "Donec id dolor enim. Praesent a porttitor est, vel varius odio. Mauris a mi purus. Aliquam imperdiet elementum nibh, id maximus lorem ultrices id. Quisque convallis felis ante, sed euismod leo rhoncus quis. Nulla massa ante, ultricies at varius ut, interdum id eros. Nam faucibus lobortis dolor eget consectetur. Aenean fringilla nunc vel magna convallis elementum. Phasellus gravida, eros eu suscipit ullamcorper, nibh purus efficitur ante, vel maximus mauris dui convallis eros. Nullam luctus turpis non elit auctor placerat. Morbi condimentum a ipsum eget interdum. Phasellus sit amet condimentum neque. Praesent egestas metus interdum, aliquam sem vitae, varius ligula. Sed blandit tincidunt leo vel dapibus. Etiam non lorem sagittis, consequat est at, mattis ipsum.\n" +
                            "\n" +
                            "Vivamus at massa et ex lacinia tempor eleifend a metus. Etiam efficitur, felis vel ornare tincidunt, diam nulla maximus ipsum, in consectetur urna velit nec augue. Suspendisse bibendum ante nec libero rhoncus, vel laoreet felis maximus. Praesent nisi orci, eleifend nec mollis efficitur, volutpat in justo. Ut lacinia enim vel purus ultricies aliquam. Proin ac nunc massa. Vivamus fermentum, tortor ac sollicitudin faucibus, ex eros sagittis ipsum, vel euismod odio eros vitae enim. Morbi aliquet, elit non sagittis tempus, est nibh congue ante, quis sollicitudin mauris odio at erat. Pellentesque iaculis sollicitudin tellus, vitae dictum orci. Ut eu ante vitae enim accumsan aliquam. Vestibulum maximus imperdiet lacus."
                )
            ),
                back = {},
                setExercise = {}
            )
        }
    }
}