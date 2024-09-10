package fr.cuber.fitcube.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import fr.cuber.fitcube.utils.FitCubeAppBar

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        result.value = it
    }
    LaunchedEffect(key1 = result.value) {
        if (result.value != null) {
            viewModel.uploadDatabase(result.value!!, context)
        }
    }
    Scaffold(
        topBar = {
            FitCubeAppBar(title = "Settings", onClose = onClose)
        }
    ) { pd ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { viewModel.downloadDatabase(context)}) {
                Text(text = "Export database")
            }
            Button(onClick = {
                launcher.launch("application/x-sqlite3")
            }) {
                Text(text = "Load database")
            }
        }
    }
}