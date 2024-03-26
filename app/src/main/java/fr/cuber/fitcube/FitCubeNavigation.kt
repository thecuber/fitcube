package fr.cuber.fitcube

import androidx.navigation.NavHostController
import fr.cuber.fitcube.db.entity.Workout

private object FitCubeScreens {
    const val HOME = "home"
    const val EXERCISES = "exercises"
    const val WORKOUTS = "workouts"
    const val SETTINGS = "settings"
}

object FitCubeRoutes {
    const val HOME = "home"
    const val EXERCISES = "exercises"
    const val WORKOUTS = "workouts"
    const val SETTINGS = "settings"
}

class FitCubeNavigationActions(private val navHostController: NavHostController) {
    fun openWorkout(task: Workout) {
        TODO("Not yet implemented")
    }

}