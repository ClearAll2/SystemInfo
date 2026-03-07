package com.lkonlesoft.displayinfo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.view.module.AndroidDashboard
import com.lkonlesoft.displayinfo.view.module.BatteryDashboard
import com.lkonlesoft.displayinfo.view.module.BluetoothDashboard
import com.lkonlesoft.displayinfo.view.module.DisplayDashboard
import com.lkonlesoft.displayinfo.view.module.MemoryDashBoard
import com.lkonlesoft.displayinfo.view.module.NetworkDashboard
import com.lkonlesoft.displayinfo.view.module.SoCDashBoard
import com.lkonlesoft.displayinfo.view.module.StorageDashboard
import com.lkonlesoft.displayinfo.view.module.SystemDashboard
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != RESULT_OK){
            Toast.makeText(applicationContext, getString(R.string.update_not_download), Toast.LENGTH_LONG).show()
        }
        if (result.resultCode == RESULT_OK){
            Toast.makeText(applicationContext, getString(R.string.downloading_update), Toast.LENGTH_LONG).show()
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Toast.makeText(
                applicationContext,
                getString(R.string.download_update_complete),
                Toast.LENGTH_SHORT
            ).show()
            lifecycleScope.launch {
                delay(3000)
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun checkForAppUpdates(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
            }
        }
    }

    private val settings: SettingsViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext).apply {
            registerListener(installStateUpdatedListener)
        }
        checkForAppUpdates()
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ScaffoldContext(settings = settings)
        }
        val shortcutAndroid = ShortcutInfoCompat.Builder(this, "android")
            .setShortLabel(getString(R.string.android))
            .setLongLabel(getString(R.string.android))
            .setIcon(IconCompat.createWithResource(this, R.drawable.outline_android_24))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "si://info/android".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            .build()
        val shortcutBattery = ShortcutInfoCompat.Builder(this, "battery")
            .setShortLabel(getString(R.string.battery))
            .setLongLabel(getString(R.string.battery))
            .setIcon(IconCompat.createWithResource(this, R.drawable.outline_battery_4_bar_24))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "si://info/battery".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            .build()
        val shortcutMemory = ShortcutInfoCompat.Builder(this, "memory")
            .setShortLabel(getString(R.string.memory))
            .setLongLabel(getString(R.string.memory))
            .setIcon(IconCompat.createWithResource(this, R.drawable.outline_memory_24))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "si://info/memory".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcutAndroid)
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcutBattery)
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcutMemory)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(settings: SettingsViewModel){
    val typographyType by settings.typographyType.collectAsStateWithLifecycle()
    val useNewDashboard by settings.useNewDashboard.collectAsStateWithLifecycle()
    val appColor by settings.appColor.collectAsStateWithLifecycle()
    val useDynamicColors by settings.useDynamicColors.collectAsStateWithLifecycle()
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val routes = remember {
        listOf(
            NavigationItem.Home,
            NavigationItem.SOC,
            NavigationItem.Battery,
            NavigationItem.Memory,
            NavigationItem.Display,
            NavigationItem.Storage,
            NavigationItem.Android,
            NavigationItem.Network,
            NavigationItem.System,
            NavigationItem.Connectivity,
            NavigationItem.About,
            NavigationItem.Camera,
            NavigationItem.Settings,
            NavigationItem.Apps
        )
    }
    val rotateSettingGear by animateFloatAsState(
        targetValue = if (currentRoute == NavigationItem.Home.route) 0f else 360f,
        animationSpec = tween(250)
    )
    ScreenInfoTheme (
        typographyType = typographyType,
        darkTheme = when(appColor) {
            0 -> isSystemInDarkTheme()
            1 -> true
            else -> false
        },
        dynamicColor = useDynamicColors
    ) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background)
        {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background
                        ),
                        title = {
                            Text(
                                text = stringResource(routes.find { it.route == currentRoute }?.name ?: R.string.home),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            AnimatedContent(
                                contentAlignment = Alignment.Center,
                                targetState = currentRoute == NavigationItem.Home.route,
                                transitionSpec = {
                                    fadeIn() + slideInHorizontally(initialOffsetX = { -it  }) togetherWith
                                            fadeOut() + slideOutHorizontally(targetOffsetX = { -it  })
                                },
                                label = "NavIconAnimation"
                            ) { isHomeRoute ->
                                if (isHomeRoute) {
                                    Spacer(modifier = Modifier.padding(start = 20.dp))
                                } else {
                                    IconButton(
                                        modifier = Modifier
                                            .padding(start = 20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainer,
                                                shape = CircleShape
                                            ),
                                        onClick = { navController.navigateUp() }
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_back_24),
                                            contentDescription = "backIcon",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        actions = {
                            AnimatedVisibility(visible = currentRoute == NavigationItem.Home.route,
                                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it  }),
                                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it  })
                            ) {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        modifier = Modifier.rotate(rotateSettingGear),
                                        onClick = {
                                            settings.setUseNewDashboard(!useNewDashboard)
                                        }) {
                                        Crossfade(
                                            targetState = useNewDashboard,
                                            label = "TitleAnimation"
                                        ) { new ->
                                            if (new) {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.outline_tile_small_24), contentDescription = "OldView"
                                                )
                                            }
                                            else {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.outline_dashboard_24), contentDescription = "NewView"
                                                )
                                            }
                                        }
                                    }
                                    IconButton(
                                        modifier = Modifier.padding(end = 5.dp).rotate(rotateSettingGear),
                                        onClick = {
                                            navController.navigate(NavigationItem.Settings.route)
                                        }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                R.drawable.rounded_settings_24
                                            ), contentDescription = "Settings"
                                        )
                                    }
                                }

                            }
                        }
                    )
                }
            ) { paddingValues ->
                MainNavigation(
                    settings = settings,
                    navController = navController,
                    currentRoute = currentRoute,
                    typographyType = typographyType,
                    useNewDashboard = useNewDashboard,
                    appColor = appColor,
                    isDynamicColors = useDynamicColors,
                    paddingValues = paddingValues)
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun HomeScreen(useNewDashboard: Boolean, navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val width = LocalWindowInfo.current.containerDpSize.width
    val state = rememberLazyStaggeredGridState()
    val listScreen = remember {
        listOf(
            NavigationItem.System,
            NavigationItem.Android,
            NavigationItem.SOC,
            NavigationItem.Display,
            NavigationItem.Battery,
            NavigationItem.Memory,
            NavigationItem.Storage,
            NavigationItem.Network,
            NavigationItem.Camera,
            NavigationItem.Connectivity,
            NavigationItem.Apps
        )
    }
    AnimatedContent(targetState = useNewDashboard,
        transitionSpec = {
            if (targetState && !initialState) {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            } else {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            }
        }
    ) {
        if (it){
            LazyVerticalStaggeredGrid (
                state = state,
                columns = if (width < 600.dp) StaggeredGridCells.Fixed(1) else StaggeredGridCells.Adaptive(400.dp),
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 7.5.dp)
                    .consumeWindowInsets(paddingValues)
            ) {
                item {
                    SystemDashboard(
                        onClick = { navController.navigate(NavigationItem.System.route) })
                }
                item {
                    AndroidDashboard(
                        onClick = { navController.navigate(NavigationItem.Android.route) })
                }
                item {
                    SoCDashBoard(
                        onClick = { navController.navigate(NavigationItem.SOC.route) })
                }
                item {
                    BatteryDashboard(
                        onClick = { navController.navigate(NavigationItem.Battery.route) })
                }
                item {
                    DisplayDashboard(
                        onClick = { navController.navigate(NavigationItem.Display.route) })
                }
                item {
                    MemoryDashBoard(
                        onClick = { navController.navigate(NavigationItem.Memory.route) })
                }
                item {
                    StorageDashboard(
                        onClick = { navController.navigate(NavigationItem.Storage.route) })
                }
                item {
                    NetworkDashboard(
                        onClick = { navController.navigate(NavigationItem.Network.route) })
                }
                item {
                    BluetoothDashboard {
                        navController.navigate(NavigationItem.Connectivity.route)
                    }
                }
            }
        }
        else {
            LazyVerticalStaggeredGrid (
                state = state,
                columns = if (width < 600.dp) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Adaptive(240.dp),
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 7.5.dp)
                    .consumeWindowInsets(paddingValues)
            ) {
                items(listScreen) { item ->
                    val isSelected = currentRoute == item.route
                    BigTitle(title = stringResource(item.name), icon = item.icon) {
                        if (!isSelected) {
                            navController.navigate(item.route)
                        }
                    }
                }
            }
        }
    }
}