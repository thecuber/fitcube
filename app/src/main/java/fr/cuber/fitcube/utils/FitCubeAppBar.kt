package fr.cuber.fitcube.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import fr.cuber.fitcube.ui.theme.FitCubeTheme

private val default: () -> Unit = {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitCubeAppBar(
    title: String,
    onClose: () -> Unit = default,
    actions: Map<ImageVector, () -> Unit> = emptyMap()
) {
    val close = (onClose !== default)
    val color = MaterialTheme.colorScheme.primary
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = color,
            navigationIconContentColor = color
        ),
        title = {
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp))
        },
        navigationIcon = {
            if (close) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.padding(horizontal = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(
                        borderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 2.dp
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                }
            }
        },
        actions = {
            actions.forEach { (icon, action) ->
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.clickable { action() }, tint = color)
            }
            Spacer(modifier = Modifier.padding(5.dp))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun FitCubeAppBarPreview() {
    FitCubeTheme {
        Surface {
            FitCubeAppBar(title = "FitCube", onClose = {},
                actions = mapOf(Icons.Rounded.Close to {})
            )
        }
    }
}