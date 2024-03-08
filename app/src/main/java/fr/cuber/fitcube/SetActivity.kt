package fr.cuber.fitcube

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import fr.cuber.fitcube.db.AppDatabase
import fr.cuber.fitcube.db.WorkoutDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetActivity : AppCompatActivity() {

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

            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "FitCube",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            Text(modifier = Modifier.padding(innerPadding),text = trainingSet?.name ?: "No training set found")
        }
    }

}