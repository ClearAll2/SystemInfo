package com.lkonlesoft.displayinfo.view

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExitTransition
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
import com.lkonlesoft.displayinfo.view.module.MediaScreen
import com.lkonlesoft.displayinfo.view.module.MemoryScreen
import com.lkonlesoft.displayinfo.view.module.NetworkScreen
import com.lkonlesoft.displayinfo.view.module.SensorsScreen
import com.lkonlesoft.displayinfo.view.module.StorageScreen
import com.lkonlesoft.displayinfo.view.module.SystemScreen
import com.lkonlesoft.displayinfo.view.module.TestScreen
import com.lkonlesoft.displayinfo.view.test.AccelerometerTestScreen
import com.lkonlesoft.displayinfo.view.test.ColorTestScreen
import com.lkonlesoft.displayinfo.view.test.FlashlightTestScreen
import com.lkonlesoft.displayinfo.view.test.LightSensorTestScreen
import com.lkonlesoft.displayinfo.view.test.ProximityTestScreen
import com.lkonlesoft.displayinfo.view.test.TouchTestScreen
import com.lkonlesoft.displayinfo.view.test.VibrationTestScreen
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel

@Composable
fun MainNavigation(
    modifier: Modifier,
    settings: SettingsViewModel,
    navController: NavHostController,
    currentRoute: String?,
    appColor: Int,
    typographyType: Int,
    currentView: Int,
    isDynamicColors: Boolean,
    paddingValues: PaddingValues
) {
    val longPressCopy by settings.longPressCopy.collectAsStateWithLifecycle()
    val copyTitle by settings.copyTitle.collectAsStateWithLifecycle()
    val showNotice by settings.showNotice.collectAsStateWithLifecycle()
    NavHost(
        modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainer),
        navController = navController,
        startDestination = NavigationItem.Home.route,
        predictivePopExitTransition = {
            slideOutOfContainer(targetOffset = { -it },
                towards = AnimatedContentTransitionScope.SlideDirection.Left) +
                    ExitTransition.KeepUntilTransitionsFinished
        },
        predictivePopEnterTransition = {
            scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(220, delayMillis = 100)
            ) + slideIntoContainer(initialOffset = { it }, towards = AnimatedContentTransitionScope.SlideDirection.Right)
        },
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
        }
    ) {
        composable(
            route = NavigationItem.Settings.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
                }
            )) {
            AboutScreen(paddingValues = paddingValues)
        }

        composable(
            route = NavigationItem.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            HomeScreen(
                navController = navController,
                currentRoute = currentRoute,
                currentView = currentView,
                paddingValues = paddingValues
            )
        }
        composable(
            route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
                }
            )) {
            DisplayScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Battery.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
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
                    uriPattern = "si://info/$route"
                })
        ) {
            StorageScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Network.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
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
                uriPattern = "si://info/$route"
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
                uriPattern = "si://info/$route"
            }
        )) {
            ConnectivityScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Media.route, deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            MediaScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(
            route = NavigationItem.Apps.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/$route"
            }
        )) {
            AppsScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Sensors.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/$route"
            }
        )) {
            SensorsScreen(
                paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(
            route = NavigationItem.Test.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            TestScreen(
                navController = navController,
                currentRoute = currentRoute,
                paddingValues = paddingValues
            )
        }
        composable(
            route = NavigationItem.ColorTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            ColorTestScreen {
                navController.navigateUp()
            }
        }
        composable(
            route = NavigationItem.TouchTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            TouchTestScreen {
                navController.navigateUp()
            }
        }
        composable(
            route = NavigationItem.FlashlightTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            FlashlightTestScreen (paddingValues = paddingValues)
        }
        composable(
            route = NavigationItem.VibrationTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            VibrationTestScreen(paddingValues = paddingValues)
        }
        composable(
            route = NavigationItem.LightSensorTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            LightSensorTestScreen(paddingValues = paddingValues)
        }
        composable(
            route = NavigationItem.ProximityTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            ProximityTestScreen(paddingValues = paddingValues)
        }
        composable(
            route = NavigationItem.AccelerometerTest.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/$route"
                }
            )) {
            AccelerometerTestScreen(paddingValues = paddingValues)
        }
    }
}