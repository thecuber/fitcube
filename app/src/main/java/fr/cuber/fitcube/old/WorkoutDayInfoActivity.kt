package fr.cuber.fitcube.old

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import fr.cuber.fitcube.old.db.AppDatabase
import fr.cuber.fitcube.old.db.WorkoutDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutDayInfoActivity : AppCompatActivity() {

    private var trainingSet: WorkoutDay? = null
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getInstance(applicationContext)
        val uid = intent.getIntExtra("uid", 0)
        println("UID: $uid")
        CoroutineScope(Dispatchers.IO).launch {
            trainingSet = db.workoutDAO().getTrainingSet(uid)
        }
        setContent {
            content()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            floatingActionButton = {
               FloatingActionButton(onClick = {
                   val intent = Intent(this@WorkoutDayInfoActivity, BaseExerciseChooseActivity::class.java)
                   intent.putExtra("uid", trainingSet?.uid)
                   startActivity(intent)
               }) {
                   Icon(Icons.Filled.Add, "Add exercise.")
               }
            },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            trainingSet?.name ?: "No training set found",
                            maxLines = 1,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            Text(modifier = Modifier.padding(innerPadding),text = trainingSet?.name ?: "No training set found")
        }
    }

}