package fr.cuber.fitcube

import androidx.navigation.NavHostController
import fr.cuber.fitcube.FitCubeRouteArgs.EXERCISE_ID
import fr.cuber.fitcube.FitCubeRouteArgs.WORKOUT_ID
import fr.cuber.fitcube.FitCubeScreens.EXERCISE_CHOOSE_SCREEN
import fr.cuber.fitcube.FitCubeScreens.EXERCISE_EDIT_SCREEN
import fr.cuber.fitcube.FitCubeScreens.HOME_SCREEN
import fr.cuber.fitcube.FitCubeScreens.WORKOUT_INFO_SCREEN
import fr.cuber.fitcube.FitCubeScreens.WORKOUT_SESSION_SCREEN
import fr.cuber.fitcube.data.db.entity.Workout

private object FitCubeScreens {
    const val HOME_SCREEN = "home"
    const val WORKOUT_INFO_SCREEN = "workout_info"
    const val EXERCISE_CHOOSE_SCREEN = "workout_exercise_choose"
    const val EXERCISE_EDIT_SCREEN = "workout_exercise_edit"
    const val WORKOUT_SESSION_SCREEN = "workout_session"
}

object FitCubeRouteArgs {
    const val WORKOUT_ID = "workout_id"
    const val EXERCISE_ID = "exercise_id"
}

object FitCubeRoutes {
    const val HOME = HOME_SCREEN
    const val WORKOUT_INFO = "$WORKOUT_INFO_SCREEN/{$WORKOUT_ID}"
    const val EXERCISE_CHOOSE = "$EXERCISE_CHOOSE_SCREEN/{$WORKOUT_ID}"
    const val EXERCISE_EDIT = "$EXERCISE_EDIT_SCREEN/{$EXERCISE_ID}"
    const val WORKOUT_SESSION = "$WORKOUT_SESSION_SCREEN/{$WORKOUT_ID}"
}

class FitCubeNavigationActions(private val navHostController: NavHostController) {
    fun openWorkout(workout: Workout) {
        navHostController.navigate("${WORKOUT_INFO_SCREEN}/${workout.id}")
    }

    fun openExercise(id: Int) {
        navHostController.navigate("${EXERCISE_EDIT_SCREEN}/${id}")
    }

    fun chooseExercise(id: Int) {
        navHostController.navigate("${EXERCISE_CHOOSE_SCREEN}/${id}")
    }

    fun startWorkout(id: Int) {
        navHostController.navigate("${WORKOUT_SESSION_SCREEN}/${id}")
    }

}