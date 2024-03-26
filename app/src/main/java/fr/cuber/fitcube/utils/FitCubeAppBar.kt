package fr.cuber.fitcube.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.cuber.fitcube.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitCubeAppBar(
    title: String
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        title = {
            Text(title)
        },
        modifier = Modifier.fillMaxWidth()
        )
}
