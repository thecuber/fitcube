package fr.cuber.fitcube

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.cuber.fitcube.data.AppDatabase
import fr.cuber.fitcube.data.TrainingSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

class HomeActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            db = AppDatabase.getInstance(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                trainingSets = db.trainingDAO().getAll()
            }
        setContent {
            ActivityContent()
        }
    }

    private var trainingSets: List<TrainingSet> = arrayListOf()

    @Preview
    @Composable
    fun PreviewContent() {
        ActivityContent()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ActivityContent() {
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
            HomeContent(innerPadding)
        }
    }


    @Composable
    fun HomeContent(innerPadding: PaddingValues = PaddingValues(0.dp)) {
        var isClicked by remember { mutableStateOf(false) }
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Training sets")
                Column(modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .border(1.dp, MaterialTheme.colorScheme.primary)) {
                    trainingSets.forEach {
                        TrainingSetCard(it)
                    }
                    if(trainingSets.isEmpty()) {
                        TrainingSetCard(null)
                    }
                }
                Button(onClick = { isClicked = true }){
                    Text("Add training set")
                }
            }
        }
        if(isClicked) {
            addTrainingSet { isClicked = false }
        }
    }

    @Composable
    fun TrainingSetCard(set: TrainingSet?) {
        Row(modifier = Modifier.padding(8.dp).clickable(enabled = set != null, onClick = {
            val intent = Intent(this@HomeActivity, SetActivity::class.java)
            intent.putExtra("uid", set?.uid)
            startActivity(intent)
        })) {
            Text(set?.name ?: "No training set")
        }
    }

    @Composable
    fun addTrainingSet(close: () -> Unit) {
        var setName by remember { mutableStateOf("") }
        val send = {
            close()
            val intent = Intent(this@HomeActivity, SetActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                intent.putExtra("uid", db.trainingDAO().createTrainingSet(setName))
                CoroutineScope(Dispatchers.Main).launch {
                    startActivity(intent)
                }
            }
            startActivity(intent)
        }
        Dialog(onDismissRequest = close) {
            // Draw a rectangle shape with rounded corners inside the dialog
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
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
                        value = setName,
                        placeholder = { Text("Training set name") },
                        onValueChange = { setName = it },
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
                            onClick = { send() },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }




}