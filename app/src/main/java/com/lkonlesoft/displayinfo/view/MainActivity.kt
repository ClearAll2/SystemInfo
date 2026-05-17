package com.lkonlesoft.displayinfo.view

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.view.module.AndroidDashboard
import com.lkonlesoft.displayinfo.view.module.AppDashboard
import com.lkonlesoft.displayinfo.view.module.BatteryDashboard
import com.lkonlesoft.displayinfo.view.module.BluetoothDashboard
import com.lkonlesoft.displayinfo.view.module.CameraDashboard
import com.lkonlesoft.displayinfo.view.module.DisplayDashboard
import com.lkonlesoft.displayinfo.view.module.MemoryDashBoard
import com.lkonlesoft.displayinfo.view.module.NetworkDashboard
import com.lkonlesoft.displayinfo.view.module.SoCDashBoard
import com.lkonlesoft.displayinfo.view.module.StorageDashboard
import com.lkonlesoft.displayinfo.view.module.SystemDashboard
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private val settings: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MainContext(settings = settings)
        }
        val shortcutAndroid = ShortcutInfoCompat.Builder(this, "android")
            .setShortLabel(getString(R.string.android))
            .setLongLabel(getString(R.string.android))
            .setIcon(createTintedIconCompat(context = this, resId = R.drawable.outline_android_24,
                ContextCompat.getColor(this, R.color.teal_700)))
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
            .setIcon(createTintedIconCompat(context = this, resId = R.drawable.battery_android_4_24px,
                ContextCompat.getColor(this, R.color.teal_700)))
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
            .setIcon(createTintedIconCompat(context = this, resId = R.drawable.memory_24px,
                ContextCompat.getColor(this, R.color.teal_700)))
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainContext(settings: SettingsViewModel){
    val typographyType by settings.typographyType.collectAsStateWithLifecycle()
    val currentView by settings.currentView.collectAsStateWithLifecycle()
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
            color = MaterialTheme.colorScheme.surfaceContainer)
        {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeFlexibleTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
                                        shapes = IconButtonDefaults.shapes(),
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                        modifier = Modifier.padding(start = 20.dp),
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
                                        shapes = IconButtonDefaults.shapes(),
                                        modifier = Modifier.rotate(rotateSettingGear),
                                        onClick = {
                                            settings.setCurrentView(if (currentView == 2) 0 else currentView + 1)
                                        }) {
                                        Crossfade(
                                            targetState = currentView,
                                            label = "TitleAnimation"
                                        ) { view ->
                                            when(view){
                                                0 -> Icon(imageVector = ImageVector.vectorResource(R.drawable.dashboard_24px), contentDescription = "newDashboard")
                                                1 -> Icon(imageVector = ImageVector.vectorResource(R.drawable.view_list_24px), contentDescription = "listDashboard")
                                                else -> Icon(imageVector = ImageVector.vectorResource(R.drawable.dashboard_2_24px), contentDescription = "oldDashboard")
                                            }
                                        }
                                    }
                                    IconButton(
                                        shapes = IconButtonDefaults.shapes(),
                                        modifier = Modifier.padding(end = 5.dp).rotate(rotateSettingGear),
                                        onClick = {
                                            navController.navigate(NavigationItem.Settings.route)
                                        }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(
                                                R.drawable.settings_24px
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
                    currentView = currentView,
                    appColor = appColor,
                    isDynamicColors = useDynamicColors,
                    paddingValues = paddingValues)
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun HomeScreen(currentView: Int, navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val width = LocalWindowInfo.current.containerDpSize.width
    val state = rememberLazyStaggeredGridState()
    val resources = LocalResources.current
    val listScreen = remember {
        listOf(
            Pair(
                NavigationItem.System,
                buildDetailsSubTextSetting(resources, R.string.model, R.string.kernel, R.string.root_status)
            ),
            Pair(
                NavigationItem.Android,
                buildDetailsSubTextSetting(resources, R.string.android_version, R.string.security_patch)
            ),
            Pair(
                NavigationItem.SOC,
                buildDetailsSubTextSetting(resources, R.string.cpu_info, R.string.cpu_usage)
            ),
            Pair(
                NavigationItem.Display,
                buildDetailsSubTextSetting(resources, R.string.size, R.string.refresh_rate, R.string.widevine)
            ),
            Pair(
                NavigationItem.Battery,
                buildDetailsSubTextSetting(resources, R.string.health, R.string.cycle_count, R.string.temperature)
            ),
            Pair(
                NavigationItem.Memory,
                buildDetailsSubTextSetting(resources, R.string.available_ram, R.string.total_ram)
            ),
            Pair(
                NavigationItem.Storage,
                buildDetailsSubTextSetting(resources, R.string.internal_storage, R.string.external_storage)
            ),
            Pair(
                NavigationItem.Network,
                buildDetailsSubTextSetting(resources, R.string.network_type, R.string.sim_info)
            ),
            Pair(
                NavigationItem.Camera,
                buildDetailsSubTextSetting(resources, R.string.hardware_level, R.string.resolution)
            ),
            Pair(
                NavigationItem.Connectivity,
                buildDetailsSubTextSetting(resources, R.string.bluetooth, R.string.connected_devices)
            ),
            Pair(
                NavigationItem.Apps,
                buildDetailsSubTextSetting(resources,  R.string.system, R.string.user)
            )
        )
    }
    AnimatedContent(targetState = currentView,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            } else {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        }
    ) { view ->
        when(view){
            0 -> {
                LazyVerticalStaggeredGrid (
                    state = state,
                    columns = if (width < 600.dp) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Adaptive(240.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 7.5.dp)
                        .padding(top = paddingValues.calculateTopPadding())
                        .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                ) {
                    items(listScreen) { item ->
                        val isSelected = currentRoute == item.first.route
                        BigTitle(title = stringResource(item.first.name), icon = item.first.icon) {
                            if (!isSelected) {
                                navController.navigate(item.first.route)
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyVerticalStaggeredGrid (
                    state = state,
                    columns = if (width < 600.dp) StaggeredGridCells.Fixed(1) else StaggeredGridCells.Adaptive(400.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 7.5.dp)
                        .padding(top = paddingValues.calculateTopPadding())
                        .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
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
                    item {
                        AppDashboard {
                            navController.navigate(NavigationItem.Apps.route)
                        }
                    }
                    item {
                        CameraDashboard {
                            navController.navigate(NavigationItem.Camera.route)
                        }
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surfaceContainer),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyVerticalStaggeredGrid(
                        state = state,
                        columns = StaggeredGridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (width < 840.dp) 1f else .7f)
                            .padding(horizontal = 20.dp)
                            .padding(top = paddingValues.calculateTopPadding())
                            .clip(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                    ) {
                        items(listScreen) { item ->
                            val isSelected = currentRoute == item.first.route
                            MainSettingItem(
                                modifier = Modifier,
                                iconId = item.first.icon,
                                mainText = stringResource(item.first.name),
                                subText = item.second,
                                isLast = item == listScreen.last(),
                                tintColor = Color.DarkGray,
                                iconBackgroundColor = item.first.color,
                                topStart = if (item == listScreen.first()) 20.dp else 5.dp,
                                topEnd = if (item == listScreen.first()) 20.dp else 5.dp,
                                bottomStart = if (item == listScreen.last()) 20.dp else 5.dp,
                                bottomEnd = if (item == listScreen.last()) 20.dp else 5.dp,
                            ) {
                                if (!isSelected) {
                                    navController.navigate(item.first.route)
                                }
                            }
                        }
                        staggeredHeader {
                            Spacer(modifier = Modifier.padding(20.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun buildDetailsSubTextSetting(resources: Resources, vararg resIds: Int): String {
    return when (resIds.size) {
        0 -> ""
        1 -> resources.getString(resIds[0])
        2 -> "${resources.getString(resIds[0])} & ${resources.getString(resIds[1])}"
        else -> {
            val lastString = resources.getString(resIds.last())
            // Drop the last element to join the rest with commas
            val remainingStrings = resIds.dropLast(1).joinToString(", ") { resources.getString(it) }
            "$remainingStrings & $lastString" // Added the Oxford comma for grammatical correctness
        }
    }
}

private fun createTintedIconCompat(
    context: Context,
    @DrawableRes resId: Int,
    @ColorInt tintColor: Int
): IconCompat {

    val drawable = ContextCompat.getDrawable(context, resId)?.mutate()
        ?: return IconCompat.createWithResource(context, resId) // fallback

    // Force tint with SRC_IN (most reliable across versions)
    drawable.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)

    // Create bitmap with explicit config for better compatibility
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)

    val bitmap = createBitmap(width, height)

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)

    return IconCompat.createWithBitmap(bitmap)
}