package io.github.padconnect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.padconnect.dialogs.AlertDialogHost
import io.github.padconnect.ui.main.GPEmulationScreen
import io.github.padconnect.ui.main.LayoutsScreen
import io.github.padconnect.ui.main.SetupScreen
import io.github.padconnect.ui.settings.AdvancedSettingsScreen
import io.github.padconnect.ui.settings.SettingsScreen
import io.github.padconnect.utils.LayoutStorage
import io.github.padconnect.utils.settings.GlobalConfig
import io.github.padconnect.viewmodel.GPEmulationViewModel

class MainActivity : ComponentActivity() {
    private val gpEmulationViewModel by viewModels<GPEmulationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PadConnectTheme {
                PadConnectApp(viewmodel = remember { gpEmulationViewModel })
            }
        }
    }
}

@Composable
fun PadConnectApp(viewmodel: GPEmulationViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedDestination =  remember(currentRoute) {
        AppDestinations.entries.find { it.name == currentRoute } ?: AppDestinations.HOME
    }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d("NAV_DEBUG", "----- Destination Changed -----")
            Log.d("NAV_DEBUG", "Current Route: ${destination.route}")
            Log.d("NAV_DEBUG", "Arguments: $arguments")

            controller.previousBackStackEntry?.let {
                Log.d("NAV_DEBUG", "Previous Route: ${it.destination.route}")
            }

            Log.d("NAV_DEBUG", "-------------------------------")
        }
    }

    val showBottomBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute != null && !currentRoute.startsWith("emulation") && !currentRoute.endsWith("settings") && currentRoute != "SETUP"
        }
    }

    AlertDialogHost()

    NavigationSuiteScaffold(
        layoutType = if (showBottomBar) NavigationSuiteType.NavigationBar else NavigationSuiteType.None,
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(painterResource(it.icon), contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == selectedDestination,
                    alwaysShowLabel = false,
                    onClick = {
                        if (currentRoute != it.name) {
                            navController.navigate(it.name) {
                                popUpTo(AppDestinations.HOME.name) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        HomeNavGraph(navController, viewmodel)
    }
}

@Composable
fun HomeNavGraph(navController: NavHostController, viewModel: GPEmulationViewModel) {
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = if (GlobalConfig.INITIAL_SETUP_FINISHED.boolean) "HOME" else "SETUP"
    ) {
        composable("HOME") {
            LayoutsScreen(
                onLayoutSelected = { layout ->
                    navController.navigate("emulation/${layout.name}")
                }
            )
        }

        composable("SETUP") {
            SetupScreen(viewModel, navigateTo = {
                navController.navigate(it) {
                    popUpTo("SETUP") {
                        inclusive = true
                    }
                }
            })
        }

        composable("SETTINGS") {
            SettingsScreen(navigateTo = {
                navController.navigate(it)
            })
        }

        composable("advanced_settings") {
            AdvancedSettingsScreen()
        }

        composable(
            route = "emulation/{layoutName}",
            arguments = listOf(
                navArgument("layoutName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val layoutName =
                backStackEntry.arguments?.getString("layoutName")!!

            val layout = remember(layoutName) {
                LayoutStorage.load(context, layoutName)
            }

            GPEmulationScreen(
                layout = layout!!,
                viewModel
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Layouts", R.drawable.ic_home),
    SETTINGS("Settings", com.github.ishan09811.compose_preferences.R.drawable.ic_settings),
}