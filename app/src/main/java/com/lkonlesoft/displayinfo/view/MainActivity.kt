package com.lkonlesoft.displayinfo.view

import android.Manifest
import android.app.Application
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import com.lkonlesoft.displayinfo.helper.connectionStateToString
import com.lkonlesoft.displayinfo.helper.copyTextToClipboard
import com.lkonlesoft.displayinfo.helper.hasPermission
import com.lkonlesoft.displayinfo.`object`.AboutItem
import com.lkonlesoft.displayinfo.`object`.AppTheme
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.utils.AndroidUtils
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import com.lkonlesoft.displayinfo.utils.CameraUtils
import com.lkonlesoft.displayinfo.utils.DisplayUtils
import com.lkonlesoft.displayinfo.utils.NetworkUtils
import com.lkonlesoft.displayinfo.utils.SocUtils
import com.lkonlesoft.displayinfo.utils.StorageUtils
import com.lkonlesoft.displayinfo.utils.SystemUtils
import com.lkonlesoft.displayinfo.view.dashboard.AndroidDashboard
import com.lkonlesoft.displayinfo.view.dashboard.BatteryDashboard
import com.lkonlesoft.displayinfo.view.dashboard.DisplayDashboard
import com.lkonlesoft.displayinfo.view.dashboard.GeneralProgressBar
import com.lkonlesoft.displayinfo.view.dashboard.MemoryDashBoard
import com.lkonlesoft.displayinfo.view.dashboard.NetworkDashboard
import com.lkonlesoft.displayinfo.view.dashboard.SoCDashBoard
import com.lkonlesoft.displayinfo.view.dashboard.StorageDashboard
import com.lkonlesoft.displayinfo.view.dashboard.SystemDashboard
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel
import com.lkonlesoft.displayinfo.widget.BatReceiver
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

    private val settings: SettingsViewModel by viewModels{
        SettingsModelFactory(application)
    }

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

class SettingsModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.cast(SettingsViewModel(application))!!
    }
}

fun NavHostController.returnToHome(){
    popBackStack()
    navigate(NavigationItem.Home.route) {
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(settings: SettingsViewModel){
    val useNewDashboard by settings.useNewDashboard.collectAsStateWithLifecycle()
    val appColor by settings.appColor.collectAsStateWithLifecycle()
    val useDynamicColors by settings.useDynamicColors.collectAsStateWithLifecycle()
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    ScreenInfoTheme (
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
                                text = when(currentRoute){
                                    NavigationItem.SOC.route -> stringResource(NavigationItem.SOC.name)
                                    NavigationItem.Battery.route -> stringResource(NavigationItem.Battery.name)
                                    NavigationItem.Memory.route -> stringResource(NavigationItem.Memory.name)
                                    NavigationItem.Display.route -> stringResource(NavigationItem.Display.name)
                                    NavigationItem.Storage.route -> stringResource(NavigationItem.Storage.name)
                                    NavigationItem.Android.route -> stringResource(NavigationItem.Android.name)
                                    NavigationItem.Network.route -> stringResource(NavigationItem.Network.name)
                                    NavigationItem.System.route -> stringResource(NavigationItem.System.name)
                                    NavigationItem.About.route -> stringResource(NavigationItem.About.name)
                                    NavigationItem.Settings.route -> stringResource(NavigationItem.Settings.name)
                                    else -> "Home"
                                },
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
                                        modifier = Modifier.padding(end = 5.dp),
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
                    useNewDashboard = useNewDashboard,
                    appColor = appColor,
                    isDynamicColors = useDynamicColors,
                    paddingValues = paddingValues)
            }
        }
    }
}

@Composable
fun SystemScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val deviceInfoList by remember { mutableStateOf(
        SystemUtils(context).getDeviceData()) }
    var rootInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    var extraInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    LaunchedEffect(Unit) {
        rootInfoList = SystemUtils(context).getRootData()
        extraInfoList = SystemUtils(context).getExtraData()
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.device))
                deviceInfoList.forEach {
                    IndividualLine(
                        tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = deviceInfoList.last() == it,
                        topStart = if (deviceInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (deviceInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (deviceInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (deviceInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.root_status))
                rootInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = rootInfoList.last() == it,
                        topStart = if (rootInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (rootInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (rootInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (rootInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.extra))
                extraInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        isLast = extraInfoList.last() == it,
                        topStart = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (extraInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (extraInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AndroidScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val androidInfoList = AndroidUtils(context).getAndroidInfo()
    val extraInfoList = AndroidUtils(context).getExtraInfo()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.general))
                androidInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = androidInfoList.last() == it,
                        topStart = if (androidInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (androidInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (androidInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (androidInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.other))
                extraInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = extraInfoList.last() == it,
                        topStart = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (extraInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (extraInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NetworkScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resource = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var showWarningPopup by remember { mutableStateOf(false) }
    var hasPermission by remember(refreshKey) { mutableStateOf(context.hasPermission(Manifest.permission.READ_PHONE_STATE)) }
    val networkType by remember(refreshKey) {
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils(context).getNetwork() else NetworkUtils(context).getNetworkOldApi())
    }
    val infoList by remember(refreshKey) { mutableStateOf(NetworkUtils(context).getDetailsInfo()) }
    val simInfoList by remember(refreshKey) { mutableStateOf(NetworkUtils(context).getSimInfo()) }
    val startForPermissionResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) {isGranted ->
        hasPermission = isGranted
        if (isGranted){
            Toast.makeText(context, resource.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(context, resource.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            showWarningPopup = !showWarningPopup
        }
    }
    val startSettingForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasPermission = context.hasPermission(Manifest.permission.READ_PHONE_STATE)
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AnimatedVisibility(visible = showWarningPopup,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        ConfirmActionPopup(
            content = {},
            mainText = stringResource(id = R.string.permission_denied),
            subText = stringResource(id = R.string.permission_denied_details),
            confirmText = stringResource(id = R.string.settings),
            cancelText = stringResource(id = R.string.cancel),
            onDismiss = {
                showWarningPopup = !showWarningPopup
            },
            onClick = {
                showWarningPopup = !showWarningPopup
                startSettingForResult.launch(intent)
            }
        )
    }
    LaunchedEffect(Unit) {
        while (true){
            delay(4000L)
            refreshKey++
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                IndividualLine(tittle = stringResource(R.string.network_type), info = networkType,
                    onClick = {
                        startForPermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
                    },
                    canLongPress = longPressCopy,
                    copyTitle = copyTitle,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                    isLast = true
                )
            }
        }
        if (simInfoList.isNotEmpty() && hasPermission) {
            itemsIndexed(simInfoList) { index, simInfo ->
                Column {
                    HeaderLine(tittle = "SIM #${index+1}")
                    simInfo.forEach {
                        IndividualLine(
                            tittle = stringResource(it.name),
                            info = it.value.toString(),
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = simInfo.last() == it,
                            topStart = if (simInfo.first() == it) 20.dp else 5.dp,
                            topEnd = if (simInfo.first() == it) 20.dp else 5.dp,
                            bottomStart = if (simInfo.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (simInfo.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        else{
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.sim_info))
                    IndividualLine(tittle = stringResource(R.string.sim_info), info = stringResource(R.string.require_permission),
                        onClick = {
                            startForPermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp,
                        isLast = true
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.details))
                infoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = infoList.last() == it,
                        topStart = if (infoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (infoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (infoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (infoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}


@Composable
fun DisplayScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var widevineInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var clearKeyInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var infoList by remember(refreshKey) { mutableStateOf(DisplayUtils(context, resources).getAllData()) }
    LaunchedEffect(Unit) {
        widevineInfo = DisplayUtils(context, resources).getWidevineInfo()
        clearKeyInfo = DisplayUtils(context, resources).getClearKeyInfo()
        while (true){
            delay(1000L)
            refreshKey++
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.display))
                infoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = infoList.last() == it,
                        topStart = if (infoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (infoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (infoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (infoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.widevine))
                val widevineList = widevineInfo.toList()
                widevineList.forEach {
                    IndividualLine(tittle = it.first.replaceFirstChar { c -> c.uppercase() },
                        info = it.second,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = widevineList.last() == it,
                        topStart = if (widevineList.first() == it) 20.dp else 5.dp,
                        topEnd = if (widevineList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (widevineList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (widevineList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.clearkey))
                val clearKeyList = clearKeyInfo.toList()
                clearKeyList.forEach {
                    IndividualLine(tittle = it.first.replaceFirstChar { c -> c.uppercase() },
                        info = it.second,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = clearKeyList.last() == it,
                        topStart = if (clearKeyList.first() == it) 20.dp else 5.dp,
                        topEnd = if (clearKeyList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (clearKeyList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (clearKeyList.last() == it) 20.dp else 5.dp
                    )
                }

            }
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.drm_notice_title,
                    text = R.string.drm_notice,
                )
            }
        }
    }
}

@Composable
fun BluetoothStatusScreen(onClick: () -> Unit) {
    val localContext = LocalContext.current

    // Check for Bluetooth connect permission on API 31+ (Android 12+)
    var hasBluetoothPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                ContextCompat.checkSelfPermission(
                    localContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }

    // Create a launcher for the permission request.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasBluetoothPermission = isGranted
    }

    // If the permission is not granted, show a prompt and a button to request it.
    if (!hasBluetoothPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bluetooth connect permission is required to display Bluetooth status.",
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }) {
                Text(text = "Grant Permission")
            }
        }
        return
    }

    // Define Compose state variables for Bluetooth enablement and profile connection states.
    var isBluetoothEnabled by remember { mutableStateOf(false) }
    var headsetConnectionState by remember { mutableIntStateOf(BluetoothProfile.STATE_DISCONNECTED) }
    var a2dpConnectionState by remember { mutableIntStateOf(BluetoothProfile.STATE_DISCONNECTED) }

    // Obtain the default Bluetooth adapter.
    val bluetoothManager = localContext.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager?.adapter
    // State variable to hold the list of connected Bluetooth devices.
    var connectedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    // Initialize the state using the adapter's current information.
    LaunchedEffect(Unit) {
        isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
        headsetConnectionState = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
            ?: BluetoothProfile.STATE_DISCONNECTED
        a2dpConnectionState = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.A2DP)
            ?: BluetoothProfile.STATE_DISCONNECTED
        val headsetDevices =
            bluetoothManager?.getConnectedDevices(BluetoothProfile.HEADSET) ?: emptyList()
        val a2dpDevices =
            bluetoothManager?.getConnectedDevices(BluetoothProfile.A2DP) ?: emptyList()
        // Combine the lists and remove duplicate devices (using device address as the unique key).
        connectedDevices = (headsetDevices + a2dpDevices).distinctBy { it.address }
    }

    // Register a BroadcastReceiver to listen for Bluetooth state changes.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    when (it.action) {
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                            val state = it.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                            isBluetoothEnabled = (state == BluetoothAdapter.STATE_ON)
                        }
                        BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                            if (ActivityCompat.checkSelfPermission(
                                    localContext,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return
                            }
                            headsetConnectionState = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
                                ?: BluetoothProfile.STATE_DISCONNECTED
                            a2dpConnectionState = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.A2DP)
                                ?: BluetoothProfile.STATE_DISCONNECTED
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(
            localContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            localContext.unregisterReceiver(receiver)
        }
    }
    BackHandler {
        onClick()
    }
    // Build the UI to display Bluetooth status.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        header { HeaderLine(tittle = "Status") }
        item { IndividualLine(tittle = "Bluetooth Enabled", info = if (isBluetoothEnabled) "Yes" else "No")}
        item { IndividualLine(tittle = "Headset Connection", info = connectionStateToString(headsetConnectionState))}
        item { IndividualLine(tittle = "A2DP Connection", info = connectionStateToString(a2dpConnectionState))}
        header { HeaderLine(tittle = "Connected Devices") }
        items(connectedDevices) { device ->
            IndividualLine(tittle = device.name, info = device.address)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BatteryScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList by remember(refreshKey) { mutableStateOf(BatteryUtils(context).getAllData()) }
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            refreshKey++
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                GeneralProgressBar((infoList[0].value as Number).toLong(), 100L, 1, height = 30.dp, verticalPadding = 15.dp)
                infoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = infoList.last() == it,
                        topStart = if (infoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (infoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (infoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (infoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            GeneralWarning(
                title = R.string.cycle_count,
                text = R.string.battery_notice_2,
                icon = R.drawable.outline_info_24,
                extra = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val widgetProvider = ComponentName(context, BatReceiver::class.java)
                        Button(
                            modifier = Modifier.padding(bottom = 10.dp),
                            onClick = {
                                appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
                            }
                        ) {
                            Text(stringResource(R.string.add_battery_widget))
                        }
                    }
                }
            )
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.battery_notice_title,
                    text = R.string.battery_notice
                )
            }
        }
    }
}


@OptIn(FlowPreview::class)
@Composable
fun HomeScreen(useNewDashboard: Boolean, navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val width = LocalWindowInfo.current.containerDpSize.width
    val state = rememberLazyStaggeredGridState()
    val listScreen = listOf(
        NavigationItem.System,
        NavigationItem.Android,
        NavigationItem.SOC,
        NavigationItem.Display,
        NavigationItem.Battery,
        NavigationItem.Memory,
        NavigationItem.Storage,
        NavigationItem.Network,
        NavigationItem.Camera,
        //NavigationItem.Connectivity
    )
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
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraInfoScreen(paddingValues: PaddingValues, longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean) {
    val context = LocalContext.current
    var cameraInfoList by remember { mutableStateOf<List<List<DeviceInfo>>>(emptyList()) }
    LaunchedEffect(Unit) {
       cameraInfoList = CameraUtils(context).getAllData()
    }

    // Display the list of camera details.
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(cameraInfoList) { index, cameraItemList ->
            Column {
                HeaderLine(tittle = "#${index+1}")
                cameraItemList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cameraItemList.last() == it,
                        topStart = if (cameraItemList.first() == it) 20.dp else 5.dp,
                        topEnd = if (cameraItemList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cameraItemList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cameraItemList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        if (showNotice) {
            staggeredHeader {
                GeneralWarning(
                    title = R.string.camera_notice_title,
                    text = R.string.camera_notice
                )
            }
        }
    }
}

@Composable
fun MemoryScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfo by remember(refreshKey) { mutableStateOf(StorageUtils(context).getRAMInfo()) }
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            refreshKey++ // Triggers recomposition
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        header {GeneralProgressBar((ramInfo[2].value as Number).toLong(), (ramInfo[3].value as Number).toLong(), 1,
            height = 30.dp,
            verticalPadding = 15.dp)}
        item {
            Column {
                ramInfo.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = ramInfo.last() == it,
                        topStart = if (ramInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (ramInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (ramInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (ramInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun StorageScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val internalStorageStats = remember(refreshKey) { StorageUtils(context).getInternalStorageInfo() }
    val externalStorageStats = remember(refreshKey) { StorageUtils(context).getExternalStorageInfo() }
    // Auto-refresh every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L)
            refreshKey++ // Triggers recomposition
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)

    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.internal_storage))
                GeneralProgressBar((internalStorageStats[2].value as Number).toLong(), (internalStorageStats[3].value as Number).toLong(), 1,
                height = 30.dp, verticalPadding = 5.dp)
                Spacer(modifier = Modifier.padding(10.dp))
                internalStorageStats.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = internalStorageStats.last() == it,
                        topStart = if (internalStorageStats.first() == it) 20.dp else 5.dp,
                        topEnd = if (internalStorageStats.first() == it) 20.dp else 5.dp,
                        bottomStart = if (internalStorageStats.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (internalStorageStats.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        if (externalStorageStats.isNotEmpty()) {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.external_storage))
                    GeneralProgressBar(
                        (externalStorageStats[2].value as Number).toLong(),
                        (externalStorageStats[3].value as Number).toLong(),
                        1,
                        height = 30.dp,
                        verticalPadding = 5.dp
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    externalStorageStats.forEach {
                        IndividualLine(
                            tittle = stringResource(it.name),
                            info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = externalStorageStats.last() == it,
                            topStart = if (externalStorageStats.first() == it) 20.dp else 5.dp,
                            topEnd = if (externalStorageStats.first() == it) 20.dp else 5.dp,
                            bottomStart = if (externalStorageStats.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (externalStorageStats.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.storage_notice_title,
                    text = R.string.storage_notice
                )
            }
        }
    }
}

@Composable
fun HardwareScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues, showNotice: Boolean) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val glEs by remember(refreshKey) { mutableStateOf(SocUtils(context).getGlEsVersion()) }
    val cpuInfoList by remember { mutableStateOf(SocUtils(context).getCPUInfo()) }
    val cpuUsageInfo by remember(refreshKey) { mutableStateOf(SocUtils(context).getCPUUsage()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Update every 1 second
            refreshKey++
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.cpu_info))
                cpuInfoList.forEach {
                    IndividualLine(tittle = if (it.type == 1) stringResource(it.name, it.value) else stringResource(it.name),
                        info = if (it.type == 1) it.extra else it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cpuInfoList.last() == it,
                        topStart = if (cpuInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (cpuInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cpuInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cpuInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.cpu_usage))
                cpuUsageInfo.forEach {
                    IndividualLine(tittle = stringResource(it.name, it.value),
                        info = it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cpuUsageInfo.last() == it,
                        topStart = if (cpuUsageInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (cpuUsageInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cpuUsageInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cpuUsageInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.gpu_info))
                IndividualLine(tittle = stringResource(R.string.gles_version),
                    info = glEs,
                    canLongPress = longPressCopy,
                    copyTitle = copyTitle,
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            }
        }
        if (showNotice) {
            staggeredHeader {
                GeneralWarning(
                    title = R.string.soc_notice_title,
                    text = R.string.soc_notice
                )
            }
        }
    }
}

@Composable
fun BigTitle(title: String, icon: Int, onClick: () -> Unit) {
    OutlinedCard (
        modifier = Modifier
            .padding(horizontal = 7.5.dp, vertical = 7.5.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ){
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.padding(10.dp))
            Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = title, modifier = Modifier
                .padding(10.dp)
                .size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = title, fontSize = 25.sp, modifier = Modifier.padding(10.dp))

        }
        Spacer(modifier = Modifier.padding(10.dp))
    }
}


@Composable
fun IndividualLine(
    tittle: String,
    info: String,
    info2: String = "",
    info3: String = "",
    onClick: () -> Unit = { },
    canLongPress: Boolean = true,
    copyTitle: Boolean = true,
    topStart: Dp = 5.dp,
    topEnd: Dp = 5.dp,
    bottomStart: Dp = 5.dp,
    bottomEnd: Dp = 5.dp,
    isLast: Boolean = false
){
    val context = LocalContext.current
    val resource = LocalResources.current
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (canLongPress) {
                            if (copyTitle) {
                                context.copyTextToClipboard(buildString {
                                    append(tittle)
                                    append("\n")
                                    append(info)
                                })
                            }
                            else {
                                context.copyTextToClipboard(info)
                            }
                            Toast.makeText(
                                context,
                                resource.getString(R.string.copied_to_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = tittle.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            if (info.isNotEmpty())
                Text(text = info, fontSize = 15.sp, modifier = Modifier.padding(vertical = 5.dp))
            if (info2.isNotEmpty())
                Text(text = info2, fontSize = 15.sp, modifier = Modifier.padding(vertical = 5.dp))
            if (info3.isNotEmpty())
                Text(text = info3, fontSize = 15.sp, modifier = Modifier.padding(vertical = 5.dp))

        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}


fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

fun LazyStaggeredGridScope.staggeredHeader(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit
) {
    item(
        span = StaggeredGridItemSpan.FullLine, // Use this to span the full width
        content = content
    )
}

@Composable
fun HeaderLine(tittle: String, horizontalPadding: Dp = 10.dp, verticalPadding: Dp = 10.dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = tittle,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    val uriHandler = LocalUriHandler.current
    val appInfoItems = listOf(
        AboutItem.AppVer,
        AboutItem.Rate,
        AboutItem.More,
        AboutItem.Contact,

        )
    val legalInfoItems = listOf(
        AboutItem.Privacy,
        AboutItem.Terms
    )
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.app_info))
                appInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    AboutMenuItem(tittle = stringResource(id = item.title),
                        text = stringResource(id = item.text),
                        onItemClick = {
                            uriHandler.openUri(url)
                        },
                        isLast = appInfoItems.last() == item,
                        topStart = if (appInfoItems.first() == item) 20.dp else 5.dp,
                        topEnd = if (appInfoItems.first() == item) 20.dp else 5.dp,
                        bottomStart = if (appInfoItems.last() == item) 20.dp else 5.dp,
                        bottomEnd = if (appInfoItems.last() == item) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.legal_info))
                legalInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    AboutMenuItem(tittle = stringResource(id = item.title),
                        text = stringResource(id = item.text),
                        onItemClick = {
                            uriHandler.openUri(url)
                        },
                        isLast = legalInfoItems.last() == item,
                        topStart = if (legalInfoItems.first() == item) 20.dp else 5.dp,
                        topEnd = if (legalInfoItems.first() == item) 20.dp else 5.dp,
                        bottomStart = if (legalInfoItems.last() == item) 20.dp else 5.dp,
                        bottomEnd = if (legalInfoItems.last() == item) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSelectionPopup(
    modifier: Modifier,
    currentLang: String,
    onClick: (String) -> Unit,
    onDismiss: () -> Unit) {
    val height = LocalWindowInfo.current.containerDpSize.height
    val localeOptions = mapOf(
        "default" to R.string.system_default,
        "vi" to R.string.vi,
        "ru" to R.string.ru,
        "zh" to R.string.zh,
        "ja" to R.string.ja,
        "ko" to R.string.ko,
        "en" to R.string.en,
        "fr" to R.string.fr,
        "nl" to R.string.nl,
        "de" to R.string.de,
        "it" to R.string.it,
        "pt" to R.string.pt,
        "es" to R.string.es
    )
    var selectLang by remember { mutableStateOf(currentLang) }
    val firstIndex = localeOptions.entries.toList().indexOfFirst { it.key == currentLang } -1
    val state = rememberLazyListState(initialFirstVisibleItemIndex = if (firstIndex >= 0) firstIndex else 0)
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 10.dp,
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                Text(text = stringResource(id = R.string.language), fontSize = 25.sp, modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp))
                LazyColumn(state = state, modifier = Modifier
                    .fillMaxWidth()
                    .height(height.times(0.4f))){
                    items(localeOptions.entries.toList()) { item ->
                        PopupSelectionLine(
                            name = stringResource(item.value),
                            onSelected = selectLang == item.key,
                            onItemClick = {
                                selectLang = item.key
                            }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(5.dp)) {
                        Text(text = stringResource(id = R.string.cancel), modifier = Modifier.padding(5.dp))
                    }
                    TextButton(onClick = { onClick(selectLang) }, modifier = Modifier.padding(5.dp)) {
                        Text(text = stringResource(id = R.string.OK), modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    longPressCopy: Boolean,
    copyTitle: Boolean,
    showNotice: Boolean,
    appColor: Int,
    isDynamicColors: Boolean,
    settings: SettingsViewModel,
    onAboutClick: () -> Unit,
    paddingValues: PaddingValues
) {
    var showLangDialog by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf("")}
    val localeOptions = mapOf(
        "default" to R.string.system_default,
        "vi" to R.string.vi,
        "ru" to R.string.ru,
        "zh" to R.string.zh,
        "ja" to R.string.ja,
        "ko" to R.string.ko,
        "en" to R.string.en,
        "fr" to R.string.fr,
        "nl" to R.string.nl,
        "de" to R.string.de,
        "it" to R.string.it,
        "pt" to R.string.pt,
        "es" to R.string.es
    )
    LaunchedEffect(Unit) {
        currentLang = getCurrentLanguage()
    }
    AnimatedVisibility(visible = showLangDialog,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        LanguageSelectionPopup(
            modifier = Modifier.fillMaxWidth(),
            currentLang = currentLang,
            onDismiss = {
                showLangDialog = !showLangDialog
            },
            onClick = { newLang ->
                if (currentLang != newLang){
                    currentLang = newLang
                    if (newLang == "default")
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    else
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(currentLang))
                }
            }
        )
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.language))
                AboutMenuItem(tittle = stringResource(R.string.language),
                    text = stringResource(localeOptions.entries.firstOrNull { it.key == currentLang }?.value
                        ?: R.string.system_default),
                    onItemClick = {
                        showLangDialog = !showLangDialog
                    },
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.display))
                ThemeSelector(
                    selectedTheme = appColor,
                    onThemeSelected = {
                        settings.setAppColor(it)
                    },
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                )
                CommonSwitchOption(
                    text = R.string.material_you,
                    subText = R.string.material_you_details,
                    extra = "",
                    checked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isDynamicColors else false,
                    onClick = {
                        settings.setUseDynamicColors(!isDynamicColors)
                    },
                    onSwitch = {
                        settings.setUseDynamicColors(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp,
                    isLast = false,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    clickable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                )
                CommonSwitchOption(
                    text = R.string.show_warning_notice,
                    subText = R.string.show_warning_notice_details,
                    extra = "",
                    checked = showNotice,
                    onClick = {
                        settings.setShowNotice(!showNotice)
                    },
                    onSwitch = {
                        settings.setShowNotice(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    isLast = true
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.general))
                CommonSwitchOption(
                    text = R.string.long_press_to_copy,
                    subText = R.string.long_press_to_copy_details,
                    extra = "",
                    checked = longPressCopy,
                    onClick = {
                        settings.setLongPressCopy(!longPressCopy)
                    },
                    onSwitch = {
                        settings.setLongPressCopy(it)
                    },
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp,
                    isLast = false
                )
                CommonSwitchOption(
                    text = R.string.copy_title,
                    subText = R.string.copy_title_details,
                    extra = "",
                    checked = copyTitle,
                    onClick = {
                        settings.setCopyTitle(!copyTitle)
                    },
                    onSwitch = {
                        settings.setCopyTitle(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    enabled = longPressCopy,
                    clickable = longPressCopy,
                    isLast = true
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.about))
                AboutMenuItem(tittle = stringResource(R.string.app_info),
                    text = stringResource(id = R.string.app_ver),
                    onItemClick = onAboutClick,
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            }
        }
    }
}

@Composable
fun AboutMenuItem(
    tittle: String,
    text: String,
    topStart: Dp = 5.dp,
    topEnd: Dp = 5.dp,
    bottomStart: Dp = 5.dp,
    bottomEnd: Dp = 5.dp,
    isLast: Boolean = false,
    onItemClick: () -> Unit){
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .clickable(onClick = onItemClick)
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = tittle.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Text(text = text, modifier = Modifier.padding(vertical = 5.dp),
                fontSize = 15.sp)
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }

}

fun getCurrentLanguage(): String {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    return if (!currentLocales.isEmpty) {
        currentLocales[0]?.language
            ?: "default"
    }
    else {
        "default"
    }
}

@Composable
fun PopupSelectionLine(name: String, onSelected: Boolean, onItemClick: () -> Unit) {
    // Animate scale with keyframes for a bouncy effect
    val scale by animateFloatAsState(
        targetValue = 1.0f, // Always return to 1.0f
        animationSpec = if (onSelected) {
            keyframes {
                durationMillis = 400 // Faster animation (400ms total)
                1.0f at 0 // Start at normal scale
                1.3f at 150 // Peak scale (bouncy overshoot)
                0.9f at 300 // Slight undershoot for bounce
                1.0f at 400 // Settle back to normal
            }
        } else {
            keyframes {
                durationMillis = 400 // Fast animation (400ms)
                1.0f at 0 // Start at normal scale
                0.8f at 150 // Scale down for unselection
                1.4f at 300 // Slight overshoot for bounce
                1.0f at 400 // Settle back to normal
            }
        }
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (onSelected) 16.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = 0.3f,
            stiffness = 300f
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(shape = RoundedCornerShape(cornerRadius))
            .scale(scale)
            .background(if (!onSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            .clickable {
                onItemClick()
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, modifier = Modifier
            .padding(vertical = 20.dp))
        AnimatedVisibility (visible = onSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.baseline_check_circle_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 5.dp))
        }

    }
}

@Composable
fun CommonSwitchOption(
    text: Int,
    subText: Int,
    extra: Any,
    clickable: Boolean = true,
    enabled: Boolean = true,
    separator: Boolean = false,
    checked: Boolean,
    horizontalPadding: Dp = 20.dp,
    topStart: Dp = 20.dp,
    topEnd: Dp = 20.dp,
    bottomStart: Dp = 20.dp,
    bottomEnd: Dp = 20.dp,
    isLast: Boolean = false,
    onClick: () -> Unit,
    onSwitch: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .clickable(enabled = clickable) {
                    onClick()
                }
                .height(IntrinsicSize.Min)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = stringResource(id = text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 5.dp),
                        color = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                    )
                    if (subText != -1)
                        Text(text = stringResource(id = subText, extra),
                            fontSize = 15.sp,
                            color = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                        )
                }
            }
            if (separator) {
                VerticalDivider(
                    color = Color.Gray,
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                )
            }
            Switch(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(start = 15.dp),
                enabled = enabled,
                checked = checked,
                onCheckedChange = onSwitch,
                thumbContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (checked) R.drawable.baseline_check_24 else R.drawable.baseline_close_24),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            )
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit,
    topStart: Dp = 20.dp,
    topEnd: Dp = 20.dp,
    bottomStart: Dp = 20.dp,
    bottomEnd: Dp = 20.dp,
    paddingValues: Dp = 20.dp,
    isLast: Boolean = false
) {
    val themeOptions = mutableListOf(
        AppTheme.System,
        AppTheme.Light,
        AppTheme.Dark
    )
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
        themeOptions.remove(AppTheme.System)
    }
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .padding(horizontal = paddingValues)
        ) {
            Text(
                text = stringResource(R.string.app_color),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .padding(vertical = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { theme ->
                    FilterChip(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(theme.icon),
                                contentDescription = null
                            )
                        },
                        selected = selectedTheme == theme.value,
                        onClick = { onThemeSelected(theme.value) },
                        label = { Text(stringResource(theme.title), fontSize = 15.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun GeneralWarning(
    canClick: Boolean = false,
    title: Int,
    text: Int,
    icon: Int = R.drawable.outline_comment_24,
    onClick: () -> Unit = {},
    extra: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(canClick) { onClick() }
            .padding(vertical = 10.dp, horizontal = 20.dp),
    ) {
        Row (modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(text = stringResource(title))
        }

        Text(text = stringResource(text),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
        extra()
    }
}

@Composable
fun ConfirmActionPopup(
    content: @Composable () -> Unit = { },
    mainText: String = stringResource(id = R.string.are_you_sure),
    subText: String = stringResource(id = R.string.n_a),
    confirmText: String = stringResource(id = R.string.yes),
    cancelText: String = stringResource(id = R.string.no),
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 10.dp,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(text = mainText, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp))
                content()
                Text(text = subText, modifier = Modifier.padding(horizontal = 30.dp))
                Spacer(modifier = Modifier.padding(15.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(5.dp)) {
                        Text(text = cancelText, modifier = Modifier.padding(5.dp))
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    TextButton(onClick = onClick, modifier = Modifier.padding(5.dp)) {
                        Text(text = confirmText, modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun MainNavigation(
    settings: SettingsViewModel,
    navController: NavHostController,
    currentRoute: String?,
    appColor: Int,
    useNewDashboard: Boolean,
    isDynamicColors: Boolean,
    paddingValues: PaddingValues
) {
    val longPressCopy by settings.longPressCopy.collectAsStateWithLifecycle()
    val copyTitle by settings.copyTitle.collectAsStateWithLifecycle()
    val showNotice by settings.showNotice.collectAsStateWithLifecycle()
    NavHost(navController, startDestination = NavigationItem.Home.route,
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

        composable(route = NavigationItem.Settings.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/settings"
                }
            )){
            SettingsScreen(
                settings= settings,
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
        composable(route = NavigationItem.About.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/about"
                }
            )){
            AboutScreen(paddingValues = paddingValues)
        }

        composable(route = NavigationItem.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/home"
                }
            )){
            HomeScreen(navController = navController,
                currentRoute = currentRoute,
                useNewDashboard = useNewDashboard,
                paddingValues = paddingValues
            )
        }
        composable(route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/system"
                }
            )){
            SystemScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(route = NavigationItem.Android.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/android"
                }
            )){
            AndroidScreen (paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(route = NavigationItem.SOC.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/soc"
                }
            )){
            HardwareScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice)
        }
        composable(route = NavigationItem.Display.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/display"
                }
            )){
           DisplayScreen (paddingValues = paddingValues,
               longPressCopy = longPressCopy,
               copyTitle = copyTitle,
               showNotice = showNotice)
        }
        composable(route = NavigationItem.Battery.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/battery"
                }
            )){
            BatteryScreen (paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice)
        }
        composable(route = NavigationItem.Memory.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/memory"
                }
            )){
            MemoryScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(route = NavigationItem.Storage.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/storage"
                })
        ){
            StorageScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice)
        }
        composable(route = NavigationItem.Network.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/network"
                }
            )){
            NetworkScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle
            )
        }
        composable(route = NavigationItem.Camera.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/camera"
            }
        )){
            CameraInfoScreen(paddingValues = paddingValues,
                longPressCopy = longPressCopy,
                copyTitle = copyTitle,
                showNotice = showNotice
            )
        }
        composable(route = NavigationItem.Connectivity.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/connectivity"
            }
        )) {
            BluetoothStatusScreen {
                navController.returnToHome()
            }
        }
    }
}


