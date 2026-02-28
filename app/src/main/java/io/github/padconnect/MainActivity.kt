package io.github.padconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import io.github.padconnect.ui.theme.PadConnectTheme
import io.github.padconnect.utils.LayoutStorage
import io.github.padconnect.viewmodel.GPEmulationViewModel

class MainActivity : ComponentActivity() {
    private val gpEmulationViewModel by viewModels<GPEmulationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PadConnectTheme {
                PadConnectApp(viewmodel = remember { gpEmulationViewModel })
            }
        }
    }
}

@Composable
fun PadConnectApp(viewmodel: GPEmulationViewModel) {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute?.startsWith("emulation") != true

    if (showBottomBar) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = {
                            currentDestination = it
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                false
                            )
                        }
                    )
                }
            }
        ) {
            Scaffold { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AlertDialogHost()
                    HomeNavGraph(navController, viewmodel)
                }
            }
        }
    } else {
        HomeNavGraph(navController, viewmodel)
    }
}

@Composable
fun HomeNavGraph(navController: NavHostController, viewModel: GPEmulationViewModel) {
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = "layouts"
    ) {
        composable("layouts") {
            LayoutsScreen(
                onLayoutSelected = { layout ->
                    navController.navigate("emulation/${layout.name}")
                }
            )
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
    val icon: ImageVector,
) {
    HOME("Layouts", Icons.Default.Home),
    SETTINGS("Settings", Icons.Default.Settings),
}