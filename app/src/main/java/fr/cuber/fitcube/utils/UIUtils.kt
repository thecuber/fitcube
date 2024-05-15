package fr.cuber.fitcube.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun DividerSpaced(pad: Dp) {
    Spacer(modifier = Modifier.padding(pad))
    Divider()
    Spacer(modifier = Modifier.padding(pad))
}