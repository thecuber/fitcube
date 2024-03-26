package fr.cuber.fitcube.old

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.old.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutMainActivity : AppCompatActivity() {

    private lateinit var service: WorkoutMainService

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, s: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = s as WorkoutMainService.LocalBinder
            service = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        val uid = intent.getIntExtra("uid", 0)
        CoroutineScope(Dispatchers.IO).launch {
            setContent {
                ActivityScaffold()
            }
        }
    }

    @Preview
    @Composable
    fun PreviewActivityScaffold() {

        ActivityScaffold(

        )
    }

    @Composable
    fun ActivityScaffold() {
        Scaffold(
            topBar = {
                ActivityTopAppBar()
            }
        ) {
            Content(it)
        }
    }

    sealed class Status {
        data object Starting : Status()
        data class Current(val first: Int, val second: Int, val running: Boolean) : Status()
        data object Done: Status()
    }

    @Composable
    fun Content(it: PaddingValues) {
        var status by remember {
            mutableStateOf<Status>(Status.Starting)
        }
        var timer by remember { mutableIntStateOf(0) }
        when (status) {
            is Status.Starting -> {
                Button({
                    status = Status.Current(0, 0, true)
                    service.startTimer {
                        timer = it
                    }
                }) {
                    Text("Start !")
                }
            }
            is Status.Current -> {
                if ((status as Status.Current).running) {

                } else {

                }
            }
            is Status.Done -> {
                Text("Done")
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
                Text("Workout")
            }
        )
    }

}