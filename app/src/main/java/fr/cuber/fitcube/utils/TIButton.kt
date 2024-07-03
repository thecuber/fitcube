package fr.cuber.fitcube.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TIButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true
) {
    Button(onClick = onClick, enabled = enabled) {
        Icon(imageVector = icon, contentDescription = "")
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

@Composable
fun OutlinedTIButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    enabled: Boolean = true
) {
    OutlinedButton(onClick = onClick, enabled = enabled) {
        Icon(imageVector = icon, contentDescription = "")
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

@Preview
@Composable
private fun TIButtonPreview() {
    TIButton(text = "Button", onClick = {}, icon = Icons.Filled.Info)
}