package fr.cuber.fitcube

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.cuber.fitcube.FitCubeRouteArgs.EXERCISE_ID
import fr.cuber.fitcube.FitCubeRouteArgs.WORKOUT_ID
import fr.cuber.fitcube.exercise.choose.ExerciseChooseScreen
import fr.cuber.fitcube.exercise.workoutedit.WorkoutExerciseScreen
import fr.cuber.fitcube.home.HomeScreen
import fr.cuber.fitcube.workout.info.WorkoutInfoScreen
import fr.cuber.fitcube.workout.session.WorkoutSessionScreen

@Composable
fun FitCubeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FitCubeRoutes.HOME,
    navActions: FitCubeNavigationActions = remember(navController) {
        FitCubeNavigationActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(FitCubeRoutes.HOME) {
            HomeScreen(
                openWorkout = { task -> navActions.openWorkout(task) },
                startWorkout = { task -> navActions.startWorkout(task) }
            )
        }
        composable(
            FitCubeRoutes.WORKOUT_INFO,
            arguments = listOf(
                navArgument(WORKOUT_ID) { type = NavType.IntType }
            )
        ) { entry ->
            val id = entry.arguments?.getInt(WORKOUT_ID)!!
            WorkoutInfoScreen(
                back = { navController.popBackStack() },
                workoutId = id,
                openExercise = { navActions.openExercise(it.id) },
                startWorkout = { navActions.startWorkout(id) },
                addExercise = { navActions.chooseExercise(id) }
            )
        }
        composable(
            FitCubeRoutes.EXERCISE_CHOOSE,
            arguments = listOf(
                navArgument(WORKOUT_ID) { type = NavType.IntType }
            )
        ) { entry ->
            val id = entry.arguments?.getInt(WORKOUT_ID)!!
            ExerciseChooseScreen(
                back = { navController.popBackStack() },
                workout = id,
                openExercise = { navActions.openExercise(it) }
            )
        }
        composable(
            FitCubeRoutes.EXERCISE_EDIT,
            arguments = listOf(
                navArgument(EXERCISE_ID) { type = NavType.IntType }
            )
        ) { entry ->
            val id = entry.arguments?.getInt(EXERCISE_ID)!!
            WorkoutExerciseScreen(
                back = { navController.popBackStack() },
                id = id
            )
        }
        composable(
            FitCubeRoutes.WORKOUT_SESSION,
            arguments = listOf(
                navArgument(WORKOUT_ID) { type = NavType.IntType }
            )
        ) { entry ->
            val id = entry.arguments?.getInt(WORKOUT_ID)!!
            WorkoutSessionScreen(
                back = { navController.popBackStack() },
                workoutId = id
            )
        }
    }
}