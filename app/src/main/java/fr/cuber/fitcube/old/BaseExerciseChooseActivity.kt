package fr.cuber.fitcube.old

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.old.db.AppDatabase
import fr.cuber.fitcube.old.db.BaseExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseExerciseChooseActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var workout = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("HELLO")
        db = AppDatabase.getInstance(applicationContext)
        workout = intent.getIntExtra("uid", 0)
        println("ZUUUU")
        CoroutineScope(Dispatchers.IO).launch {
            println("UU")
            val ex = db.exerciseDAO().getAll()
            println("AASAHSA")
            CoroutineScope(Dispatchers.Main).launch {
                setContent {
                    ActivityScaffold(ex)
                }
            }
        }

    }

    @Preview
    @Composable
    fun PreviewActivityScaffold() {
        val ex = BaseExercise(
            0,
            "Biceps",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        )
        ActivityScaffold(
            List(100) { ex }
        )
    }

    @Composable
    fun ActivityScaffold(exercises: List<BaseExercise>) {
        println("ZEUFFEZ")
        Scaffold(
            topBar = {
                ActivityTopAppBar()
            }
        ) {
            Content(it, exercises)
        }
    }

    @Composable
    fun Content(it: PaddingValues, exercises: List<BaseExercise>) {
        println("A")
        LazyColumn(modifier = Modifier.padding(it)) {
            items(exercises) {
                BaseExerciseCard(it)
            }
        }
    }

    @Composable
    fun BaseExerciseCard(ex: BaseExercise) {
        println("BB")
        //val res = applicationContext.resources.getIdentifier("images/${ex.uid}.png", "raw", applicationContext.packageName)
        Row(Modifier.clickable {
            val intent = Intent(this, WorkoutExerciseEditActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                val uid = db.exerciseDAO().createWorkoutExercise(workout, ex.uid)
                intent.putExtra("uid", uid)
                startActivity(intent)
            }

        }) {
            Column {
                Text(text = ex.name, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                Text(text = ex.description)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ActivityTopAppBar() {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("Add exercise to workout")
            }
        )
    }
}