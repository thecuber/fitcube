package fr.cuber.fitcube

import androidx.compose.material3.DrawerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.cuber.fitcube.home.HomeScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun FitCubeNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: String = FitCubeRoutes.HOME,
    navActions: FitCubeNavigationActions = remember(navController) {
        FitCubeNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(FitCubeRoutes.HOME) {
            HomeScreen(
                modifier = modifier,
                openWorkout = { task -> navActions.openWorkout(task)}
            )
        }
    }
}