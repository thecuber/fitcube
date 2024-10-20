package fr.cuber.fitcube.exercise.type.edit

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.defaultExerciseType
import fr.cuber.fitcube.data.db.entity.imagePagerPreview
import fr.cuber.fitcube.data.db.entity.voidExerciseType
import fr.cuber.fitcube.ui.theme.FitCubeTheme
import fr.cuber.fitcube.utils.ExerciseIcon
import fr.cuber.fitcube.utils.FitCubeAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExerciseTypeEditScreen(
    viewModel: ExerciseTypeEditViewModel = hiltViewModel(),
    id: Int,
    onClose: () -> Unit
) {
    var updatedId by remember {
        mutableIntStateOf(id)
    }
    val exercise by viewModel.getExerciseType(id).collectAsState(initial = voidExerciseType())
    var modifiedEx by remember {
        mutableStateOf(voidExerciseType())
    }
    LaunchedEffect(key1 = exercise) {
        modifiedEx = exercise
    }
    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        result.value = it
    }
    val context = LocalContext.current
    LaunchedEffect(key1 = result.value) {
        if (result.value != null) {
            val filename = "${System.currentTimeMillis()}.png"
            val dir = context.getDir("images", Context.MODE_PRIVATE)
            if (!dir.exists()) {
                dir.mkdir()
            }
            FileOutputStream(File(dir, filename)).use { output ->
                val c = context.contentResolver.openInputStream(result.value!!)
                c?.use { input ->
                    input.copyTo(output)
                }
            }
            viewModel.updateImages(exercise.image + "fld/$filename", exercise.id)
        }
    }
    val focus = LocalFocusManager.current
    ExerciseTypeEditScaffold(
        onClose = onClose,
        exercise = modifiedEx,
        setExercise = { ex ->
            modifiedEx = ex
        },
        deleteImage = { im ->
            viewModel.updateImages(
                exercise.image.filterIndexed { index, _ -> index != im },
                exercise.id
            )
        },
        addImage = { launcher.launch("image/*") },
        save = {
            if (updatedId == 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    updatedId = viewModel.createExerciseType(modifiedEx).toInt()
                }
            } else {
                viewModel.updateExercise(modifiedEx)
            }
            focus.clearFocus()
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }
    )

}

@Composable
private fun ExerciseTypeEditScaffold(
    onClose: () -> Unit,
    deleteImage: (Int) -> Unit,
    exercise: ExerciseType,
    setExercise: (ExerciseType) -> Unit,
    addImage: () -> Unit,
    save: () -> Unit
) {
    var deleteDialog by remember { mutableIntStateOf(-1) }
    if (deleteDialog >= 0) {
        AlertDialog(
            icon = {
                Icon(Icons.Filled.Info, contentDescription = "Example Icon")
            },
            title = {
                Text(text = "Are you sure you want to delete the selected image ?")
            },
            onDismissRequest = {
                deleteDialog = -1
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteImage(deleteDialog)
                        deleteDialog = -1
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteDialog = -1
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            FitCubeAppBar(
                title = if (exercise.id == 0) {
                    "Create exercise"
                } else {
                    "Edit exercise"
                }, onClose = onClose, actions = mapOf(Icons.Filled.Done to save)
            )
        },

        ) {
        ExerciseTypeEditContent(
            modifier = Modifier.padding(it),
            exercise = exercise,
            setExercise = setExercise,
            deleteImage = { im ->
                deleteDialog = im
            },
            addImage = addImage
        )
    }
}

@Preview
@Composable
private fun ExerciseTypeEditScaffoldPreview() {
    FitCubeTheme {
        Surface {
            ExerciseTypeEditScaffold(
                onClose = {},
                exercise = defaultExerciseType(5),
                setExercise = {},
                deleteImage = {},
                addImage = {},
                save = {})
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseTypeEditContent(
    modifier: Modifier = Modifier,
    exercise: ExerciseType,
    setExercise: (ExerciseType) -> Unit,
    deleteImage: (Int) -> Unit,
    addImage: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState(pageCount = {
            exercise.image.size
        })

        if (pagerState.pageCount == 0) {
            ExerciseIcon(
                img = "", modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                // Our page content
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExerciseIcon(
                        img = exercise.imagePagerPreview(page), modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                    )
                }
            }
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .size(16.dp)
                            .background(color)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(25.dp))
        OutlinedTextField(
            value = exercise.name,
            onValueChange = {
                setExercise(exercise.copy(name = it))
            },
            label = {
                Text("Name")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(value = exercise.description, onValueChange = {
            setExercise(exercise.copy(description = it))
        }, label = {
            Text("Description")
        },
            minLines = 5, maxLines = 10, modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = addImage) {
                Icon(imageVector = Icons.Outlined.ImageSearch, contentDescription = "")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add image")
            }
            Button(onClick = {
                deleteImage(pagerState.currentPage)
            }, enabled = pagerState.pageCount > 0) {
                Icon(imageVector = Icons.Outlined.HideImage, contentDescription = "")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Delete image")
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
    }
}

