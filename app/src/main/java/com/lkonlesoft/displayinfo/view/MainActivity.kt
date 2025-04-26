package com.lkonlesoft.displayinfo.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.CameraInfo
import com.lkonlesoft.displayinfo.helper.connectionStateToString
import com.lkonlesoft.displayinfo.`object`.AboutItem
import com.lkonlesoft.displayinfo.`object`.AppTheme
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.utils.AndroidUtils
import com.lkonlesoft.displayinfo.utils.BatteryUtils
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
import com.lkonlesoft.displayinfo.view.viewmodel.SettingsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ScaffoldContext()
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

fun NavHostController.returnToHome(){
    popBackStack()
    navigate(NavigationItem.Home.route) {
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(){
    val context = LocalContext.current
    val settings = SettingsViewModel(context)
    val appColor by settings.appColor.collectAsStateWithLifecycle()
    val useDynamicColors by settings.useDynamicColors.collectAsStateWithLifecycle()
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state)
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
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background
                        ),
                        title = {
                            Text(
                                text = currentRoute.toString().replaceFirstChar { it.uppercase() },
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        navigationIcon = {
                            AnimatedVisibility(visible = currentRoute == NavigationItem.Home.route){
                                Spacer(modifier = Modifier.padding(horizontal = 24.dp))
                            }
                            AnimatedVisibility(visible = currentRoute != NavigationItem.Home.route,
                                enter = slideInHorizontally() + fadeIn(),
                                exit = slideOutHorizontally() + fadeOut()
                            ){
                                IconButton(onClick = {
                                    navController.navigateUp()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        actions = {
                            AnimatedVisibility(visible = currentRoute == NavigationItem.Home.route,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(
                                    modifier = Modifier.padding(end = 5.dp),
                                    onClick = {
                                        navController.navigate(NavigationItem.Settings.route)
                                    }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(
                                            R.drawable.rounded_settings_24
                                        ), contentDescription = "Settings",
                                        modifier = Modifier.size(32.dp)
                                    )
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
                    appColor = appColor,
                    isDynamicColors = useDynamicColors,
                    paddingValues = paddingValues)
            }
        }
    }
}

@Composable
fun SystemScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = stringResource(R.string.device)) }
        item { IndividualLine(tittle = stringResource(R.string.model), info = SystemUtils.getModel()) }
        item { IndividualLine(tittle = stringResource(R.string.product), info = SystemUtils.getProduct()) }
        item { IndividualLine(tittle = stringResource(R.string.device), info = SystemUtils.getDevice()) }
        item { IndividualLine(tittle = stringResource(R.string.board), info = SystemUtils.getBoard()) }
        item { IndividualLine(tittle = stringResource(R.string.brand), info = SystemUtils.getBrand()) }
        item { IndividualLine(tittle = stringResource(R.string.manufacturer), info = SystemUtils.getManufacturer()) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item { IndividualLine(tittle = stringResource(R.string.sku), info = SystemUtils.getSku()) }
        }
        item { IndividualLine(tittle = stringResource(R.string.radio), info = SystemUtils.getRadio()) }
        item { IndividualLine(tittle = stringResource(R.string.instruction_sets), info = SystemUtils.getInstructions()) }
        item { IndividualLine(tittle = stringResource(R.string.up_time), info = SystemUtils.getUptime()) }
        item { IndividualLine(tittle = stringResource(R.string.boot_time), info = SystemUtils.getBootTime()) }
        //item { IndividualLine(tittle = stringResource(R.string.SELinux), info = SystemUtils.getSelinuxStatus()) }
        header { HeaderLine(tittle = stringResource(R.string.extra)) }
        item { IndividualLine(tittle = stringResource(R.string.usd_debug), info = if (SystemUtils.isUsbDebuggingEnabled(context)) stringResource(R.string.enabled) else stringResource(R.string.disabled)) }
        item { IndividualLine(tittle = stringResource(R.string.treble), info = if (SystemUtils.isTrebleSupported()) stringResource(R.string.supported) else stringResource(R.string.not_supported)) }
        item { IndividualLine(tittle = stringResource(R.string.seamless_update), info = if (SystemUtils.isSeamlessUpdateSupported()) stringResource(R.string.supported) else stringResource(R.string.not_supported)) }
        item { IndividualLine(tittle = stringResource(R.string.active_slot), info = SystemUtils.getActiveSlot()) }
        item { IndividualLine(tittle = stringResource(R.string.root), info = if (SystemUtils.isDeviceRooted()) stringResource(R.string.yes) else stringResource(R.string.no)) }
        header { HeaderLine(tittle = stringResource(R.string.device_features)) }
        item { IndividualLine(tittle = stringResource(R.string.device_features), info = SystemUtils.getAllSystemFeatures(context).joinToString("\n")) }
    }
}

@Composable
fun AndroidScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        item { IndividualLine(tittle = stringResource(R.string.android_version), info = AndroidUtils.getAndroidVersion()) }
        item {IndividualLine(tittle = stringResource(R.string.api_level), info = AndroidUtils.getApiLevel().toString())}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            item { IndividualLine(tittle = stringResource(R.string.security_patch), info = AndroidUtils.getSecurityPatch())}
            item { IndividualLine(tittle = stringResource(R.string.sdk), info = AndroidUtils.getSdkName()) }
        }
        item { IndividualLine(tittle = stringResource(R.string.id), info = AndroidUtils.getId()) }
        item { IndividualLine(tittle = stringResource(R.string.build_id), info = AndroidUtils.getDisplay()) }
        item { IndividualLine(tittle = stringResource(R.string.incremental), info = AndroidUtils.getIncremental()) }
        item { IndividualLine(tittle = stringResource(R.string.codename), info = AndroidUtils.getCodename()) }
        item { IndividualLine(tittle = stringResource(R.string.type), info = AndroidUtils.getType()) }
        item { IndividualLine(tittle = stringResource(R.string.tags), info = AndroidUtils.getTags()) }
        item { IndividualLine(tittle = stringResource(R.string.fingerprint), info = AndroidUtils.getFingerprint()) }
        item { IndividualLine(tittle = stringResource(R.string.kernel), info = AndroidUtils.getKernel()) }
        item { IndividualLine(tittle = stringResource(R.string.bootloader), info = AndroidUtils.getBootloader()) }
        item { IndividualLine(tittle = stringResource(R.string.hardware), info = AndroidUtils.getHardware())}
        item { IndividualLine(tittle = stringResource(R.string.host), info = AndroidUtils.getHost()) }
        item { IndividualLine(tittle = stringResource(R.string.board), info = AndroidUtils.getBoard()) }
        item {IndividualLine(tittle = stringResource(R.string.google_play_service), info = AndroidUtils.getGmsVersion(context))}
        item {IndividualLine(tittle = stringResource(R.string.performance_class), info = AndroidUtils.getPerformanceClass().toString())}
        item {IndividualLine(tittle = stringResource(R.string.device_language), info = AndroidUtils.getDeviceLanguage())}
        item {IndividualLine(tittle = stringResource(R.string.device_locale), info = AndroidUtils.getDeviceLocale())}
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NetworkScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var networkType by remember{
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils.getNetwork(context) else NetworkUtils.getNetworkOldApi(context))
    }
    var networkInfo by remember { mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) NetworkUtils.getNetInfo(context) else null) }
    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) {isGranted ->
        if (isGranted){
            Toast.makeText(context, context.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils.getNetwork(context) else NetworkUtils.getNetworkOldApi(context)
            networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) NetworkUtils.getNetInfo(context) else null
        }
        else{
            Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        while (true){
            delay(4000L)
            networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils.getNetwork(context) else NetworkUtils.getNetworkOldApi(context)
            networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) NetworkUtils.getNetInfo(context) else null
        }
    }
    Box (modifier = Modifier
        .fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues),
            contentPadding = paddingValues
        ) {
            item {IndividualLine(tittle = stringResource(R.string.network_type), info = networkType,
                canClick = true,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_PHONE_STATE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            startForResult.launch(Manifest.permission.READ_PHONE_STATE)
                        }
                    }
                })}
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                header { HeaderLine(tittle = stringResource(R.string.details)) }
                if (networkInfo != null){
                    item { IndividualLine(tittle = stringResource(R.string.interfaces), info = networkInfo?.interfaces.toString()) }
                    item { IndividualLine(tittle = stringResource(R.string.ip_address), info = networkInfo?.ip.toString()) }
                    item { IndividualLine(tittle = stringResource(R.string.domain), info = networkInfo?.domain.toString()) }
                    item { IndividualLine(tittle = stringResource(R.string.dns), info = networkInfo?.dnsServer?.replace("/", "").toString()) }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        item { IndividualLine(tittle = stringResource(R.string.dhcp_server), info = networkInfo?.dhcpServer.toString()) }
                        item { IndividualLine(tittle = stringResource(R.string.wake_on_lan_sp), info = if (networkInfo?.wakeOnLanSupported == true) stringResource(R.string.supported) else stringResource(R.string.not_supported)) }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        item { IndividualLine(tittle = stringResource(R.string.is_private_dns_on), info = if (networkInfo?.isPrivateDNSActive == true) stringResource(R.string.enabled) else stringResource(R.string.disabled)) }
                        item { IndividualLine(tittle = stringResource(R.string.private_dns_server), info = networkInfo?.privateDNS.toString()) }
                    }
                }
                else {
                    item { IndividualLine(tittle = stringResource(R.string.n_a), info = "")}
                }
            }
        }
    }

}


@Composable
fun DisplayScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resources = context.resources

    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = stringResource(R.string.display)) }
        item {IndividualLine(tittle = stringResource(R.string.smallest_dp), info = DisplayUtils.getSmallestDp(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.screen_dpi), info = DisplayUtils.getDensity(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.scale_density), info = DisplayUtils.getScaleDensity(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.xdpi), info = DisplayUtils.getXDpi(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.ydpi), info = DisplayUtils.getYDpi(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.width_dp), info = DisplayUtils.getWidthDp(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.height_dp), info = DisplayUtils.getHeightDp(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.orientation), info = if (DisplayUtils.getOrientation(resources) == 1) stringResource(R.string.portrait) else stringResource(R.string.landscape))}
        item {IndividualLine(tittle = stringResource(R.string.width_px), info = DisplayUtils.getWidthPx(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.height_px), info = DisplayUtils.getHeightPx(resources).toString())}
        item {IndividualLine(tittle = stringResource(R.string.touch_screen), info = if (DisplayUtils.getTouchScreen(resources) == 1) stringResource(R.string.no_touch) else stringResource(R.string.finger))}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            item {IndividualLine(tittle = stringResource(R.string.hdr), info = if (DisplayUtils.getIsHdr(resources)) stringResource(R.string.supported) else stringResource(R.string.not_supported))}
            item {IndividualLine(tittle = stringResource(R.string.wcg), info = if (DisplayUtils.getIsScreenWideColorGamut(resources)) stringResource(R.string.supported) else stringResource(R.string.not_supported))}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            item {IndividualLine(tittle = stringResource(R.string.display_type), info = DisplayUtils.getDisPlayType(context))}
            item {IndividualLine(tittle = stringResource(R.string.refresh_rate), info = DisplayUtils.getDisplayRefreshRate(context).toString() + " Hz")}
        }
        header { HeaderLine(tittle = stringResource(R.string.widevine)) }
        items(DisplayUtils.getWidevineInfo().toList()){
            IndividualLine(tittle = it.first, info = it.second)
        }
        header { HeaderLine(tittle = stringResource(R.string.clearkey)) }
        items(DisplayUtils.getClearKeyInfo().toList()){
            IndividualLine(tittle = it.first, info = it.second)
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
    val bluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        localContext.getSystemService(BluetoothManager::class.java)
    }
    else {
        TODO("VERSION.SDK_INT < M")
    }
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
fun BatteryScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var health by remember { mutableStateOf(BatteryUtils.getBatteryHealth(context)) }
    var capacity by remember { mutableIntStateOf(BatteryUtils.getBatteryCapacity(context).toInt()) }
    var chargeStatus by remember { mutableStateOf(BatteryUtils.getBatteryStatus(context)) }
    var temper by remember { mutableFloatStateOf(BatteryUtils.getBatteryTemperature(context)) }
    var currentInMilliAmps by remember { mutableIntStateOf(BatteryUtils.getDischargeCurrent(context)) }
    var voltage by remember { mutableFloatStateOf(BatteryUtils.getChargingVoltage(context)) }
    var batteryPercent by remember { mutableIntStateOf(BatteryUtils.getBatteryPercentage(context)) }
    var cycleCount by remember { mutableIntStateOf(BatteryUtils.getBatteryCycleCount(context)) }
    var tech by remember { mutableStateOf(BatteryUtils.getBatteryTechnology(context)) }
    LaunchedEffect(Unit) {
        while (true){
            chargeStatus = BatteryUtils.getBatteryStatus(context)
            temper = BatteryUtils.getBatteryTemperature(context)
            voltage = BatteryUtils.getChargingVoltage(context)
            batteryPercent = BatteryUtils.getBatteryPercentage(context)
            currentInMilliAmps = BatteryUtils.getDischargeCurrent(context)
            delay(1000L)
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues
    ) {
        item { IndividualLine(tittle = stringResource(R.string.status), info = chargeStatus)}
        item { IndividualLine(tittle = stringResource(R.string.battery_level), info = "$batteryPercent%")}
        item { IndividualLine(tittle = stringResource(R.string.health), info = health)}
        item { IndividualLine(tittle = stringResource(R.string.capacity), info = if (capacity > 0) ("$capacity mAh") else "Unknown")}
        item { IndividualLine(tittle = stringResource(R.string.cycle_count), info = if (cycleCount >= 0) cycleCount.toString() else "N/A")}
        item { IndividualLine(tittle = stringResource(R.string.current), info = "$currentInMilliAmps mA")}
        item { IndividualLine(tittle = stringResource(R.string.temperature), info = if (temper >= 0) ("$temper °C") else "N/A") }
        item { IndividualLine(tittle = stringResource(R.string.voltage), info = if (voltage >= 0) ("$voltage V") else "N/A") }
        item { IndividualLine(tittle = stringResource(R.string.technology), info = tech) }
    }
}


@OptIn(FlowPreview::class)
@Composable
fun HomeScreen(useNewDashboard: Boolean, navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val width = LocalConfiguration.current.screenWidthDp.dp
    var index by rememberSaveable { mutableIntStateOf(0) }
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = 0)
    val listScreen = listOf(
        NavigationItem.System,
        NavigationItem.Android,
        NavigationItem.SOC,
        NavigationItem.Display,
        NavigationItem.Battery,
        NavigationItem.Memory,
        NavigationItem.Storage,
        NavigationItem.Network,
        //NavigationItem.Camera,
        //NavigationItem.Connectivity
    )
    LaunchedEffect(state) {
        snapshotFlow {
            state.firstVisibleItemIndex
        }.debounce(500L).collectLatest{ latest ->
            index = latest
        }
    }
    LazyVerticalGrid(
        state = state,
        columns = if (width < 600.dp) GridCells.Fixed(if (!useNewDashboard) 2 else 1)
        else GridCells.Adaptive(
            400.dp
        ),
        contentPadding = paddingValues,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 7.5.dp)
            .consumeWindowInsets(paddingValues)
    ) {
        if (!useNewDashboard) {
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
        if (useNewDashboard){
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
}

@Composable
fun CameraInfoScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    var cameraInfoList by remember { mutableStateOf<List<CameraInfo>>(emptyList()) }
    BackHandler {
        onClick()
    }
    // Retrieve camera info once when the composable enters the composition.
    LaunchedEffect(Unit) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val infoList = cameraManager.cameraIdList.map { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            // Determine which way the camera faces.
            val lensFacingValue = characteristics.get(CameraCharacteristics.LENS_FACING)
            val lensFacing = when (lensFacingValue) {
                CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                CameraCharacteristics.LENS_FACING_BACK -> "Back"
                CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                else -> "Unknown"
            }
            // Retrieve sensor orientation.
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            // Determine supported hardware level.
            val hardwareLevelInt = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            val hardwareLevel = when (hardwareLevelInt) {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                else -> "Unknown"
            }
            // Compute megapixels from sensor pixel array size.
            val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
            val megapixels = pixelArraySize?.let {
                it.width.toLong() * it.height.toLong() / 1_000_000.0
            }

            // Retrieve available aperture(s) and use the smallest f-number as maximum aperture.
            val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val maxAperture = apertures?.minOrNull() // Lower f-number: larger aperture

            // Retrieve focal length, usually the first value is selected.
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalLength = focalLengths?.firstOrNull()
            // Attempt to get physical camera IDs if available (API 28+).
            val physicalCameraIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                characteristics.physicalCameraIds
            } else {
                emptySet()
            }
            CameraInfo(
                id = cameraId,
                lensFacing = lensFacing,
                sensorOrientation = sensorOrientation,
                hardwareLevel = hardwareLevel,
                megapixels = megapixels,
                maxAperture = maxAperture,
                focalLength = focalLength,
                physicalCameraIds = physicalCameraIds
            )
        }
        cameraInfoList = infoList
    }

    // Display the list of camera details.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        cameraInfoList.forEach { info ->
            header { HeaderLine(tittle = "Camera ${info.id}") }
            item {IndividualLine(tittle = "Lens", info = info.lensFacing)}
            item {IndividualLine(tittle = "Sensor Orientation", info = info.sensorOrientation.toString())}
            item {IndividualLine(tittle = "Hardware Level", info = info.hardwareLevel)}
            item {IndividualLine(tittle = "Megapixels", info = info.megapixels.toString())}
            item {IndividualLine(tittle = "Max Aperture", info = info.maxAperture.toString())}
            item {IndividualLine(tittle = "Focal Length", info = info.focalLength.toString())}
            if (info.physicalCameraIds.isNotEmpty())
                item {IndividualLine(tittle = "Physical Camera ID", info = info.physicalCameraIds.joinToString(","))}
        }
    }
}

@Composable
fun MemoryScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            refreshKey++ // Triggers recomposition
        }
    }

    val totalRAM = remember(refreshKey) { StorageUtils.getTotalRAM(context) }
    val availableRAM = remember(refreshKey) { StorageUtils.getAvailableRAM(context) }
    val usedRAM = totalRAM - availableRAM
    val percentage = (usedRAM.toDouble() / totalRAM.toDouble() * 100).toInt()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {

        item { IndividualLine(tittle = stringResource(R.string.used), info = "${percentage}%")}
        item {GeneralProgressBar(usedRAM, totalRAM, 1, horizontalPadding = 30.dp, verticalPadding = 5.dp)}
        item { IndividualLine(tittle = stringResource(R.string.available_ram), info = "$availableRAM MB")}
        item { IndividualLine(tittle = stringResource(R.string.used_ram), info = "$usedRAM MB")}
        item { IndividualLine(tittle = stringResource(R.string.total_ram), info = "$totalRAM MB")}
    }
}

@Composable
fun StorageScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    // Auto-refresh every 60 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000L)
            refreshKey++ // Triggers recomposition
        }
    }

    val (totalStorage, freeStorage) = remember(refreshKey) { StorageUtils.getInternalStorageStats() }
    val usedStorage = totalStorage - freeStorage
    val usedPercent = (usedStorage.toDouble() / totalStorage.toDouble() * 100).toInt()

    val (extTotal, extFree) = remember(refreshKey) { StorageUtils.getExternalStorageStats(context) }
    val usedExtStorage = extTotal - extFree
    val usedExtPercent = (usedExtStorage.toDouble() / extTotal.toDouble() * 100).toInt()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = stringResource(R.string.internal_storage)) }
        item { IndividualLine(tittle = stringResource(R.string.used), info = "${usedPercent}%")}
        item {GeneralProgressBar(usedStorage, totalStorage, 1, horizontalPadding = 30.dp, verticalPadding = 5.dp)}
        item { IndividualLine(tittle = stringResource(R.string.total), info = StorageUtils.formatSize(totalStorage)) }
        item { IndividualLine(tittle = stringResource(R.string.free), info = StorageUtils.formatSize(freeStorage)) }
        item { IndividualLine(tittle = stringResource(R.string.used), info = StorageUtils.formatSize(usedStorage)) }

        if (extTotal > 0) {
            header { HeaderLine(tittle = stringResource(R.string.external_storage)) }
            item { IndividualLine(tittle = stringResource(R.string.used), info = "${usedExtPercent}%")}
            item {GeneralProgressBar(usedExtStorage, extTotal, 1, horizontalPadding = 30.dp, verticalPadding = 5.dp)}
            item { IndividualLine(tittle = stringResource(R.string.total), info = StorageUtils.formatSize(extTotal)) }
            item { IndividualLine(tittle = stringResource(R.string.free), info = StorageUtils.formatSize(extFree)) }
            item { IndividualLine(tittle = stringResource(R.string.used), info = StorageUtils.formatSize(usedExtStorage)) }
        }
    }
}

@Composable
fun HardwareScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val coreNum = SocUtils.getNumberOfCores()
    var cpuGovernors by remember { mutableStateOf(listOf<String>()) }
    //val cpuName = remember { getCpuName() }
    var cpuFreqs by remember { mutableStateOf(listOf<Int>()) }

    LaunchedEffect(Unit) {
        while (true) {
            cpuFreqs = SocUtils.getAllCpuFrequencies()
            cpuGovernors = SocUtils.getAllGovernors()
            delay(1000L) // Update every 1 second
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues
    ) {
        header { HeaderLine(tittle = stringResource(R.string.cpu_info)) }
        //item { IndividualLine(tittle = "Name", info = cpuName) }
        item { IndividualLine(tittle = stringResource(R.string.cores), info = coreNum.toString()) }
        items(coreNum) {
            val coreValue = SocUtils.getMinMaxFreq(it)
            val governor = cpuGovernors.getOrNull(it) ?: "Unknown"
            IndividualLine(
                tittle = stringResource(R.string.core, "${it+1}"),
                info = stringResource(R.string.min_freq, "${coreValue.first} MHz"),
                info2 = stringResource(R.string.max_freq, "${coreValue.second} MHz"),
                info3 =  stringResource(R.string.governor, governor)
            )
        }
        header { HeaderLine(tittle = stringResource(R.string.cpu_usage))}
        itemsIndexed (cpuFreqs) { index: Int, freq: Int ->
            IndividualLine(tittle = stringResource(R.string.core, "${index+1}"), info = "$freq MHz")
        }
        //item { IndividualLine(tittle = "Raw Info", info = getSocRawInfo()) }
        header { HeaderLine(tittle = stringResource(R.string.gpu_info)) }
        item { IndividualLine(tittle = stringResource(R.string.gles_version), info = SocUtils.getGlEsVersion(context)) }
        //item { IndividualLine(tittle = stringResource(R.string.gpu_renderer), info = SocUtils.getGpuRenderer()) }
        //item { IndividualLine(tittle = stringResource(R.string.gpu_vendor), info = SocUtils.getGpuVendor()) }

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
            },

    ){
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.padding(10.dp))
            Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = title, modifier = Modifier
                .padding(10.dp)
                .size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Text(text = title, fontSize = 25.sp, modifier = Modifier.padding(10.dp))

        }
    Spacer(modifier = Modifier.padding(10.dp))
    }
}


@Composable
fun IndividualLine(tittle: String, info: String, info2: String = "", info3: String = "", canClick: Boolean = false, onClick: () -> Unit = { }){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 30.dp,
                vertical = 10.dp
            )
            .clickable(enabled = canClick, onClick = onClick),
        horizontalAlignment = Alignment.Start,
    ){
        Text(text = tittle, fontSize = 18.sp, fontWeight = FontWeight.Medium,  modifier = Modifier.padding(vertical = 5.dp))
        if (info.isNotEmpty())
            Text(text = info, modifier = Modifier.padding(vertical = 5.dp))
        if (info2.isNotEmpty())
            Text(text = info2, modifier = Modifier.padding(vertical = 5.dp))
        if (info3.isNotEmpty())
            Text(text = info3, modifier = Modifier.padding(vertical = 5.dp))
    }
}


fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

@Composable
fun HeaderLine(tittle: String, horizontalPadding: Dp = 30.dp, verticalPadding: Dp = 10.dp) {
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    useNewDashboard: Boolean,
    appColor: Int,
    isDynamicColors: Boolean,
    settings: SettingsViewModel,
    paddingValues: PaddingValues
) {
    val uriHandler = LocalUriHandler.current
    val items = listOf(
        AboutItem.AppVer,
        AboutItem.Privacy,
        AboutItem.More,
        AboutItem.Contact
    )
    LazyVerticalGrid (
        columns = GridCells.Adaptive(400.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = stringResource(R.string.display)) }
        item {
            ThemeSelector(
                paddingValues = 30.dp,
                selectedTheme = appColor,
                onThemeSelected = {
                    settings.setAppColor(it)
                }
            )
        }
        item {
            CommonSwitchOption(
                text = R.string.material_you,
                subText = R.string.material_you_details,
                extra = "",
                horizontalPadding = 30.dp,
                checked = isDynamicColors,
                onClick = {
                    settings.setUseDynamicColors(!isDynamicColors)
                },
                onSwitch = {
                    settings.setUseDynamicColors(it)
                }
            )
        }
        item {
            CommonSwitchOption(
                text = R.string.use_details_dashboard,
                subText = R.string.use_new_dashboard_details,
                extra = "",
                horizontalPadding = 30.dp,
                checked = useNewDashboard,
                onClick = {
                    settings.setUseNewDashboard(!useNewDashboard)
                },
                onSwitch = {
                    settings.setUseNewDashboard(it)
                }
            )
        }
        header { HeaderLine(tittle = stringResource(R.string.about)) }
        items(items){ item ->
            val url = stringResource(id = item.url)
            AboutMenuItem(tittle = stringResource(id = item.title),
                text = stringResource(id = item.text),
                onItemClick = {
                    uriHandler.openUri(url)
                }
            )
        }
    }
}

@Composable
fun AboutMenuItem(
    tittle: String,
    text: String,
    onItemClick: () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(
                horizontal = 30.dp,
                vertical = 10.dp
            ),
        horizontalAlignment = Alignment.Start,
    ){
        Text(text = tittle, fontSize = 18.sp, fontWeight = FontWeight.Medium,  modifier = Modifier.padding(vertical = 5.dp))
        Text(text = text, modifier = Modifier.padding(vertical = 5.dp))
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
    horizontalPadding: Dp = 30.dp,
    onClick: () -> Unit,
    onSwitch: (Boolean) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = clickable) {
            onClick()
        }
        .height(IntrinsicSize.Min)
        .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(0.7f),
            verticalAlignment = Alignment.CenterVertically) {
            //Spacer(modifier = Modifier.width(30.dp))
           /* Icon(imageVector = ImageVector.vectorResource(id = iconId),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 10.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
            )*/
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = stringResource(id = text),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 5.dp),
                    color = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
                if (subText != -1)
                    Text(text = stringResource(id = subText, extra), fontSize = 15.sp)
            }
        }
        if (separator){
            VerticalDivider(
                color = Color.Gray,
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp)
            )
        }
        Switch(modifier = Modifier
            .weight(0.2f)
            .padding(start = 15.dp),
            enabled = enabled,
            checked = checked,
            onCheckedChange = onSwitch,
            thumbContent = {
                Icon(
                    imageVector =  if (checked) Icons.Outlined.Check else Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        )
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit,
    paddingValues: Dp = 20.dp
) {
    val themeOptions = listOf(
        AppTheme.System,
        AppTheme.Light,
        AppTheme.Dark
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingValues)
    ) {
        Text(
            text = stringResource(R.string.app_color),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(vertical = 5.dp)
                .padding(bottom = 5.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themeOptions.forEach { theme ->
                FilterChip(
                    leadingIcon = {
                        Icon(imageVector = ImageVector.vectorResource(theme.icon),
                            contentDescription = null
                        )
                    },
                    selected = selectedTheme == theme.value,
                    onClick = { onThemeSelected(theme.value) },
                    label = { Text(stringResource(theme.title)) }
                )
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
    isDynamicColors: Boolean,
    paddingValues: PaddingValues
) {
    val useNewDashboard by settings.useNewDashboard.collectAsStateWithLifecycle()
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
                useNewDashboard = useNewDashboard,
                paddingValues = paddingValues,
                appColor = appColor,
                isDynamicColors = isDynamicColors
            )
        }

        composable(route = NavigationItem.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/home"
                }
            )){
            HomeScreen(navController = navController, currentRoute = currentRoute, useNewDashboard = useNewDashboard, paddingValues = paddingValues)
        }
        composable(route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/system"
                }
            )){
            SystemScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Android.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/android"
                }
            )){
            AndroidScreen (paddingValues = paddingValues)
        }
        composable(route = NavigationItem.SOC.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/soc"
                }
            )){
            HardwareScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Display.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/display"
                }
            )){
           DisplayScreen (paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Battery.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/battery"
                }
            )){
            BatteryScreen (paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Memory.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/memory"
                }
            )){
            MemoryScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Storage.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/storage"
                })
        ){
            StorageScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Network.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/network"
                }
            )){
            NetworkScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationItem.Camera.route, deepLinks = listOf(
            navDeepLink {
                uriPattern = "si://info/camera"
            }
        )){
            CameraInfoScreen{
                navController.returnToHome()
            }
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


