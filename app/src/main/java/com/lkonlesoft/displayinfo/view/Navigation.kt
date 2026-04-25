package com.lkonlesoft.displayinfo.view

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.view.module.AndroidScreen
import com.lkonlesoft.displayinfo.view.module.AppsScreen
import com.lkonlesoft.displayinfo.view.module.BatteryScreen
import com.lkonlesoft.displayinfo.view.module.CameraInfoScreen
import com.lkonlesoft.displayinfo.view.module.ConnectivityScreen
import com.lkonlesoft.displayinfo.view.module.DisplayScreen
import com.lkonlesoft.displayinfo.view.module.HardwareScreen
import com.lkonlesoft.displayinfo.view.module.MemoryScreen
import com.lkonlesoft.displayinfo.view.module.NetworkScreen
import com.lkonlesoft.displayinfo.view.module.StorageScreen
import com.lkonlesoft.displayinfo.view.module.SystemScreen
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel

@Composable
fun MainNavigation(
    settings: SettingsViewModel,
    navController: NavHostController,
    currentRoute: String?,
    appColor: Int,
    typographyType: Int,
    useNewDashboard: Boolean,
    isDynamicColors: Boolean,
    paddingValues: PaddingValues
) {
    val longPressCopy by settings.longPressCopy.collectAsStateWithLifecycle()
    val copyTitle by settings.copyTitle.collectAsStateWithLifecycle()
    val showNotice by settings.showNotice.collectAsStateWithLifecycle()
    NavHost(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer),
        navController = navController,
        startDestination = NavigationItem.Home.route,
        enterTransition = {
            fadeIn(
                animationSpec = tween(220, delayMillis = 100)
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(220, delayMillis = 100)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(100))
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(220, delayMillis = 100)
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(220, delayMillis = 100)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(100))
        }) {

        composable(
            route = NavigationItem.Settings.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/settings"
                }
            )) {
            SettingsScreen(
                settings = settings,
                typographyType = typographyType,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice,
                paddingValues = paddingValues,
                appColor = appColor,
                isDynamicColors = isDynamicColors,
                onAboutClick = {
                    navController.navigate(NavigationItem.About.route)
                }
            )
        }
        composable(
            route = NavigationItem.About.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/about"
                }
            )) {
            AboutScreen(paddingValues = paddingValues)
        }

        composable(
            route = NavigationItem.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/home"
                }
            )) {
            HomeScreen(
                navController = navController,
                currentRoute = currentRoute,
                useNewDashboard = useNewDashboard,
                paddingValues = paddingValues
            )
        }
        composable(
            route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/system"
                }
            )) {
            SystemScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Android.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/android"
                }
            )) {
            AndroidScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.SOC.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/soc"
                }
            )) {
            HardwareScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Display.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/display"
                }
            )) {
            DisplayScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Battery.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/battery"
                }
            )) {
            BatteryScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Memory.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/memory"
                }
            )) {
            MemoryScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Storage.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/storage"
                })
        ) {
            StorageScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Network.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/network"
                }
            )) {
            NetworkScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Camera.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/camera"
            }
        )) {
            CameraInfoScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Connectivity.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/connectivity"
            }
        )) {
            ConnectivityScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Apps.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/apps"
            }
        )) {
            AppsScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
    }
}