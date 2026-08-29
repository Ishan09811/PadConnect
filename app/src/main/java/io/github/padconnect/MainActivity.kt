/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */

package io.github.padconnect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.padconnect.dialogs.AlertDialogHost
import io.github.padconnect.ui.main.AboutScreen
import io.github.padconnect.ui.main.GPEmulationScreen
import io.github.padconnect.ui.main.LayoutsScreen
import io.github.padconnect.ui.main.SetupScreen
import io.github.padconnect.ui.settings.AdvancedSettingsScreen
import io.github.padconnect.ui.settings.CoreSettingsScreen
import io.github.padconnect.ui.settings.DisplaySettingsScreen
import io.github.padconnect.ui.settings.SettingsScreen
import io.github.padconnect.ui.settings.ThemeSettingsScreen
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
            currentRoute != null && AppDestinations.entries.find { it.name == currentRoute } != null
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

    val animSpec = tween<IntOffset>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    NavHost(
        navController = navController,
        startDestination = if (GlobalConfig.INITIAL_SETUP_FINISHED.boolean) "HOME" else "SETUP",
        enterTransition = {
            slideInHorizontally(animSpec, initialOffsetX = { it }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(animSpec, initialOffsetX = { -it }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {
        composable("HOME") {
            LayoutsScreen(
                navigateTo = {
                    navController.navigate(it)
                },
                viewModel
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
            AdvancedSettingsScreen(navigateTo = {
                navController.navigate(it)
            }, navigateBack = navController::navigateUp)
        }

        composable(route = "core_settings") {
            CoreSettingsScreen(navigateBack = navController::navigateUp)
        }

        composable(route = "display_settings") {
            DisplaySettingsScreen(navigateBack = navController::navigateUp)
        }

        composable("theme_settings") {
            ThemeSettingsScreen(navigateBack = navController::navigateUp)
        }

        composable("about") {
            AboutScreen()
        }

        composable(
            route = "emulation/{layoutName}/{isEditMode}",
            arguments = listOf(
                navArgument("layoutName") { type = NavType.StringType },
                navArgument("isEditMode") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val layoutName =
                backStackEntry.arguments?.getString("layoutName")!!

            val isEditMode =
                backStackEntry.arguments?.getBoolean("isEditMode") ?: false

            val layout = remember(layoutName) {
                LayoutStorage.load(context, layoutName)
            }

            GPEmulationScreen(
                layout = layout!!,
                viewModel,
                isEditMode
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Layouts", R.drawable.ic_home),
    SETTINGS("Settings", io.github.compose_preferences.R.drawable.ic_settings),
}