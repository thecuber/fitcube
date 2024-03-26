package fr.cuber.fitcube.old

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.old.db.AppDatabase
import fr.cuber.fitcube.old.db.ExerciseStyle
import fr.cuber.fitcube.old.db.WorkoutExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutExerciseEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        val uid = intent.getIntExtra("uid", 0)
        CoroutineScope(Dispatchers.IO).launch {
            setContent {
                ActivityScaffold(db.exerciseDAO().getExerciseById(uid), db)
            }
        }
    }

    @Preview
    @Composable
    fun PreviewActivityScaffold() {
        val ex = WorkoutExercise(
            0,
            0, ExerciseStyle.REPETITION, 0, listOf()
        )
        ActivityScaffold(
            ex, null
        )
    }

    @Composable
    fun ActivityScaffold(exercise: WorkoutExercise, db: AppDatabase?) {
        var style by remember {
            mutableStateOf(exercise.style == ExerciseStyle.REPETITION)
        }
        var sets by remember {
            mutableStateOf(exercise.sets)
        }
        Scaffold(
            topBar = {
                ActivityTopAppBar(exercise.uid, style, sets, db)
            }
        ) {
            Content(it, style, { style = it }, sets, { sets = it })
        }
    }

    @Composable
    fun Content(it: PaddingValues, style: Boolean, setStyle: (Boolean) -> Unit, sets: List<Long>, setSets: (List<Long>) -> Unit) {
        Column (Modifier.padding(it)) {
            Row {
                Text(if(style) "Repetitions" else "Time")
                Switch(style, onCheckedChange = {
                    setStyle(it)
                })
            }
            Button(onClick = { setSets(sets.plus(if (sets.isNotEmpty()) sets[sets.size - 1] else 0)) }) {
                Text("Add set")
            }
            Button(onClick = { setSets(sets.dropLast(1)) }, enabled = sets.isNotEmpty()) {
                Text("Remove set")
            }
            for (i in (sets.indices)) {
                TextField(value = sets[i].toString(), onValueChange = { v ->
                    setSets(sets.toMutableList().also { it[i] = v.toLong()})
                })
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ActivityTopAppBar(uid: Int, style: Boolean, sets: List<Long>, db: AppDatabase?) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("Add exercise to workout")
            },
            actions = {
                IconButton(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        db?.exerciseDAO()?.updateWorkoutExerciseSets(uid, sets, if(style) ExerciseStyle.REPETITION else ExerciseStyle.TIME)
                        finish()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Localized description"
                    )
                }
            }
        )
    }
}