package fr.cuber.fitcube.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import fr.cuber.fitcube.workout.session.WorkoutSessionContentPreview

val md_theme_light_primary = Color(0xFFF57F17)
val md_theme_light_secondary = Color(0xFF33691E)
val surfaceVariant = Color(0xFFDDDDDD)
val md_theme_light_tertiary = Color(0xFFFF0000)
// ..
// ..

val md_theme_dark_primary = Color(0xFFACD370)
val md_theme_dark_onPrimary = Color(0xFF213600)
val md_theme_dark_primaryContainer = Color(0xFF324F00)
// ..
// ..
@Preview
@Composable
fun PreviewColor() {
    WorkoutSessionContentPreview()
}