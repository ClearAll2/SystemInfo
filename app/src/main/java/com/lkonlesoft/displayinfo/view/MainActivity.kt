package com.lkonlesoft.displayinfo.view

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.CameraInfo
import com.lkonlesoft.displayinfo.helper.connectionStateToString
import com.lkonlesoft.displayinfo.helper.getAllCpuFrequencies
import com.lkonlesoft.displayinfo.helper.getAllGovernors
import com.lkonlesoft.displayinfo.helper.getClearKeyInfo
import com.lkonlesoft.displayinfo.helper.getGlEsVersion
import com.lkonlesoft.displayinfo.helper.getGpuRenderer
import com.lkonlesoft.displayinfo.helper.getGpuVendor
import com.lkonlesoft.displayinfo.helper.getMinMaxFreq
import com.lkonlesoft.displayinfo.helper.getNetInfo
import com.lkonlesoft.displayinfo.helper.getNetwork
import com.lkonlesoft.displayinfo.helper.getNetworkOldApi
import com.lkonlesoft.displayinfo.helper.getNumberOfCores
import com.lkonlesoft.displayinfo.helper.getWidevineInfo
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.utils.AndroidUtils
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import com.lkonlesoft.displayinfo.utils.StorageUtils
import com.lkonlesoft.displayinfo.utils.SystemUtils
import com.lkonlesoft.displayinfo.view.dashboard.AndroidDashboard
import com.lkonlesoft.displayinfo.view.dashboard.BatteryDashboard
import com.lkonlesoft.displayinfo.view.dashboard.MemoryDashBoard
import com.lkonlesoft.displayinfo.view.dashboard.StorageDashboard
import com.lkonlesoft.displayinfo.view.dashboard.SystemDashboard
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ScreenInfoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background)
                {
                    ScaffoldContext()
                }
            }
        }
        val shortcutAndroid = ShortcutInfoCompat.Builder(this, "android")
            .setShortLabel("Android")
            .setLongLabel("Android")
            .setIcon(IconCompat.createWithResource(this, R.drawable.outline_android_24))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "si://info/android".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            .build()
        val shortcutBattery = ShortcutInfoCompat.Builder(this, "battery")
            .setShortLabel("Battery")
            .setLongLabel("Battery")
            .setIcon(IconCompat.createWithResource(this, R.drawable.outline_battery_4_bar_24))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "si://info/battery".toUri()
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            )
            .build()
        val shortcutMemory = ShortcutInfoCompat.Builder(this, "memory")
            .setShortLabel("Memory")
            .setLongLabel("Memory")
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
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
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
                        text = currentRoute.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 20.dp)
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
                           navController.returnToHome()
                       }) {
                           Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                       }
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AboutActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                R.drawable.outline_info_24
                            ), contentDescription = "Info"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        MainNavigation(navController = navController, paddingValues = paddingValues, currentRoute = currentRoute)
    }
}

@Composable
fun SystemScreen(onClick: () -> Unit, paddingValues: PaddingValues) {
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = "Device") }
        item { IndividualLine(tittle = "Model", info = SystemUtils.getModel()) }
        item { IndividualLine(tittle = "Product", info = SystemUtils.getProduct()) }
        item { IndividualLine(tittle = "Device", info = SystemUtils.getDevice()) }
        item { IndividualLine(tittle = "Board", info = SystemUtils.getBoard()) }
        item { IndividualLine(tittle = "Brand", info = SystemUtils.getBrand()) }
        item { IndividualLine(tittle = "Manufacturer", info = SystemUtils.getManufacturer()) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item { IndividualLine(tittle = "SKU", info = SystemUtils.getSku()) }
        }
        item { IndividualLine(tittle = "Radio", info = SystemUtils.getRadio()) }
        item { IndividualLine(tittle = "Instruction Sets", info = SystemUtils.getInstructions()) }
    }
}

@Composable
fun AndroidScreen(onClick: () -> Unit, paddingValues: PaddingValues) {
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        item { IndividualLine(tittle = "Android Version", info = AndroidUtils.getAndroidVersion()) }
        item {IndividualLine(tittle = "API Level", info = AndroidUtils.getApiLevel().toString())}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            item { IndividualLine(tittle = "Security Patch", info = AndroidUtils.getSecurityPatch())}
            item { IndividualLine(tittle = "SDK", info = AndroidUtils.getSdkName()) }
        }
        item { IndividualLine(tittle = "ID", info = AndroidUtils.getId()) }
        item { IndividualLine(tittle = "Build ID", info = AndroidUtils.getDisplay()) }
        item { IndividualLine(tittle = "Incremental", info = AndroidUtils.getIncremental()) }
        item { IndividualLine(tittle = "Codename", info = AndroidUtils.getCodename()) }
        item { IndividualLine(tittle = "Type", info = AndroidUtils.getType()) }
        item { IndividualLine(tittle = "Tags", info = AndroidUtils.getTags()) }
        item { IndividualLine(tittle = "Fingerprint", info = AndroidUtils.getFingerprint()) }
        item { IndividualLine(tittle = "Kernel", info = AndroidUtils.getKernel()) }
        item { IndividualLine(tittle = "Bootloader", info = AndroidUtils.getBootloader()) }
        item { IndividualLine(tittle = "Hardware", info = AndroidUtils.getHardware())}
        item { IndividualLine(tittle = "Host", info = AndroidUtils.getHost()) }
        item { IndividualLine(tittle = "Board", info = AndroidUtils.getBoard()) }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NetworkScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    var networkType by remember{
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) getNetwork(context) else getNetworkOldApi(context))
    }
    var networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getNetInfo(context) else null
    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) {isGranted ->
        if (isGranted){
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) getNetwork(context) else getNetworkOldApi(context)
            networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getNetInfo(context) else null
        }
        else{
            Toast.makeText(context, "Permission is not granted", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) getNetwork(context) else getNetworkOldApi(context)
            networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getNetInfo(context) else null
            delay(1000L)
        }
    }
    BackHandler {
        onClick()
    }
    Box (modifier = Modifier
        .fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            modifier = Modifier.fillMaxSize()

        ) {
            item {IndividualLine(tittle = "Network Type", info = networkType,
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
                header { HeaderLine(tittle = "Details") }
                item { IndividualLine(tittle = "Interface", info = networkInfo?.interfaces.toString()) }
                item { IndividualLine(tittle = "IP Addresses", info = networkInfo?.ip.toString()) }
                item { IndividualLine(tittle = "Domain", info = networkInfo?.domain.toString()) }
                item { IndividualLine(tittle = "DNS Servers", info = networkInfo?.dnsServer?.replace("/", "").toString()) }
            }

        }
    }

}


@Composable
fun DisplayScreen(onClick: () -> Unit) {
    val resources = LocalContext.current.resources
    val density = resources.displayMetrics.densityDpi.toString()
    val scaleDensity = resources.displayMetrics.density.toString()
    val xDpi = resources.displayMetrics.xdpi.toString()
    val yDpi = resources.displayMetrics.ydpi.toString()
    val screenOrientation = resources.configuration.orientation
    val screenHeightDp = resources.configuration.screenHeightDp.toString()
    val screenWidthDp = resources.configuration.screenWidthDp.toString()
    val screenHeightPx = resources.displayMetrics.heightPixels.toString()
    val screenWidthPx = resources.displayMetrics.widthPixels.toString()
    var drmInfo = getWidevineInfo()
    var clearKeyInfo = getClearKeyInfo()
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        header { HeaderLine(tittle = "Display") }
        item {IndividualLine(tittle = "Smallest dp", info = resources.configuration.smallestScreenWidthDp.toString())}
        item {IndividualLine(tittle = "Screen (dpi)", info = density)}
        item {IndividualLine(tittle = "Scaled Density", info = scaleDensity)}
        item {IndividualLine(tittle = "X dpi", info = xDpi)}
        item {IndividualLine(tittle = "Y dpi", info = yDpi)}
        item {IndividualLine(tittle = "Width (dp)", info = screenWidthDp)}
        item {IndividualLine(tittle = "Height (dp)", info = screenHeightDp)}
        item {IndividualLine(tittle = "Orientation", info = if (screenOrientation == 1) "Portrait" else "Landscape")}
        item {IndividualLine(tittle = "Usable Width (px)", info = screenWidthPx)}
        item {IndividualLine(tittle = "Usable Height (px)", info = screenHeightPx)}
        item {IndividualLine(tittle = "Touch Screen", info = if (resources.configuration.touchscreen == 1) "No touch" else "Finger")}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            item {IndividualLine(tittle = "HDR", info = if (resources.configuration.isScreenHdr) "Supported" else "Not Supported")}
            item {IndividualLine(tittle = "Wide Color Gamut", info = if (resources.configuration.isScreenWideColorGamut) "Supported" else "Not Supported")}
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            item {IndividualLine(tittle = "Display Type", info = LocalContext.current.display.name.toString())}
            item {IndividualLine(tittle = "Refresh Rate", info = LocalContext.current.display.refreshRate.toInt()
                .toString() + " Hz")}
        }
        header { HeaderLine(tittle = "Widevine CDM") }
        items(drmInfo.toList()){
            IndividualLine(tittle = it.first, info = it.second)
        }
        header { HeaderLine(tittle = "ClearKey CDM") }
        items(clearKeyInfo.toList()){
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
        columns = GridCells.Adaptive(300.dp),
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
fun BatteryScreen(onClick: () -> Unit, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var health by remember { mutableStateOf(BatteryUtils.getBatteryHealth(context)) }
    var capacity by remember { mutableIntStateOf(BatteryUtils.getBatteryCapacity(context).toInt()) }
    var chargeStatus by remember { mutableStateOf(BatteryUtils.getBatteryStatus(context)) }
    var temper by remember { mutableFloatStateOf(BatteryUtils.getBatteryTemperature(context)) }
    var currentInMilliAmps by remember { mutableIntStateOf(BatteryUtils.getChargingCurrent()) }
    var voltage by remember { mutableFloatStateOf(BatteryUtils.getChargingVoltage()) }
    var batteryPercent by remember { mutableIntStateOf(BatteryUtils.getBatteryPercentage(context)) }
    var cycleCount by remember { mutableIntStateOf(BatteryUtils.getBatteryCycleCount(context)) }
    var tech by remember { mutableStateOf(BatteryUtils.getBatteryTechnology(context)) }
    BackHandler {
        onClick()
    }
    LaunchedEffect(Unit) {
        while (true){
            chargeStatus = BatteryUtils.getBatteryStatus(context)
            temper = BatteryUtils.getBatteryTemperature(context)
            voltage = BatteryUtils.getChargingVoltage()
            batteryPercent = BatteryUtils.getBatteryPercentage(context)
            delay(1000L)
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
        contentPadding = paddingValues
    ) {
        item { IndividualLine(tittle = "Status", info = chargeStatus)}
        item { IndividualLine(tittle = "Health", info = health)}
        item { IndividualLine(tittle = "Capacity", info = if (capacity >= 0) ("$capacity mAh") else "Unknown")}
        item { IndividualLine(tittle = "Cycle Count", info = if (cycleCount >= 0) cycleCount.toString() else "N/A")}
        item { IndividualLine(tittle = "Current", info = if (currentInMilliAmps >= 0) ("$currentInMilliAmps mA") else "N/A")}
        item { IndividualLine(tittle = "Percentage", info = "$batteryPercent%")}
        item { IndividualLine(tittle = "Temperature", info = if (temper >= 0) ("$temper °C") else "N/A") }
        item { IndividualLine(tittle = "Voltage", info = if (voltage >= 0) ("$voltage V") else "N/A") }
        item { IndividualLine(tittle = "Technology", info = tech) }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val width = LocalConfiguration.current.screenWidthDp.dp
    val listScreen = listOf(
        NavigationItem.System,
        NavigationItem.Android,
        NavigationItem.SOC,
        NavigationItem.Display,
        NavigationItem.Battery,
        NavigationItem.Memory,
        NavigationItem.Network,
        NavigationItem.Camera,
        //NavigationItem.Connectivity
    )
    LazyVerticalGrid(
        columns = if (width < 600.dp) GridCells.Fixed(1) else GridCells.Adaptive(300.dp),
        contentPadding = paddingValues,
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(10.dp)

    ) {
        item {
            SystemDashboard(
                onBack = { navController.returnToHome() },
                onClick = { navController.navigate(NavigationItem.System.route) })
        }
        item {
            AndroidDashboard(
                onBack = { navController.returnToHome() },
                onClick = { navController.navigate(NavigationItem.Android.route) })
        }
        item {
            BatteryDashboard(
                onBack = { navController.returnToHome() },
                onClick = { navController.navigate(NavigationItem.Battery.route) })
        }
        item {
            MemoryDashBoard(
                onBack = { navController.returnToHome() },
                onClick = { navController.navigate(NavigationItem.Memory.route) })
        }
        item {
            StorageDashboard(
                onBack = { navController.returnToHome() },
                onClick = { navController.navigate(NavigationItem.Memory.route) })
        }
        /*items(listScreen){ item ->
            val isSelected = currentRoute == item.route
            BigTitle(title = item.route, icon = item.icon) {
                if (!isSelected) {
                    navController.popBackStack()
                    navController.navigate(item.route) {
                        launchSingleTop = true
                    }
                }
            }
        }*/
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
        columns = GridCells.Adaptive(300.dp),
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
fun MemoryScreen(onClick: () -> Unit, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    BackHandler {
        onClick()
    }
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

    val (totalStorage, freeStorage) = remember(refreshKey) { StorageUtils.getInternalStorageStats() }
    val usedStorage = totalStorage - freeStorage

    val (extTotal, extFree) = remember(refreshKey) { StorageUtils.getExternalStorageStats(context) }
    val appStorage = remember(refreshKey) { StorageUtils.getAppStorageUsage(context) }
    val cacheStorage = remember(refreshKey) { StorageUtils.getCacheStorageUsage(context) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize().consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
        header { HeaderLine(tittle = "Memory") }
        item { IndividualLine(tittle = "Percentage", info = "${percentage}%")}
        item { IndividualLine(tittle = "Available RAM", info = "$availableRAM MB")}
        item { IndividualLine(tittle = "Used RAM", info = "$usedRAM MB")}
        item { IndividualLine(tittle = "Total RAM", info = "$totalRAM MB")}
        header { HeaderLine(tittle = "Internal Storage (User Space)") }
        item { IndividualLine(tittle = "Total", info = StorageUtils.formatSize(totalStorage)) }
        item { IndividualLine(tittle = "Free", info = StorageUtils.formatSize(freeStorage)) }
        item { IndividualLine(tittle = "Used", info = StorageUtils.formatSize(usedStorage)) }
        item { IndividualLine(tittle = "App Storage", info = StorageUtils.formatSize(appStorage)) }
        item { IndividualLine(tittle = "Cache Storage", info = StorageUtils.formatSize(cacheStorage)) }

        if (extTotal > 0) {
            header { HeaderLine(tittle = "External Storage") }
            item { IndividualLine(tittle = "Total", info = StorageUtils.formatSize(extTotal)) }
            item { IndividualLine(tittle = "Free", info = StorageUtils.formatSize(extFree)) }
        }
    }
}

@Composable
fun HardwareScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    val activityManager: ActivityManager = context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val coreNum = getNumberOfCores()
    var cpuGovernors by remember { mutableStateOf(listOf<String>()) }
    //val cpuName = remember { getCpuName() }
    var cpuFreqs by remember { mutableStateOf(listOf<Int>()) }
    BackHandler {
        onClick()
    }
    LaunchedEffect(Unit) {
        while (true) {
            cpuFreqs = getAllCpuFrequencies()
            cpuGovernors = getAllGovernors()
            delay(1000L) // Update every 1 second
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        header { HeaderLine(tittle = "CPU Info") }
        //item { IndividualLine(tittle = "Name", info = cpuName) }
        item { IndividualLine(tittle = "Cores", info = coreNum.toString()) }
        items(coreNum) {
            val coreValue = getMinMaxFreq(it)
            val governor = cpuGovernors.getOrNull(it) ?: "Unknown"
            IndividualLine(
                tittle = "Core ${it+1}",
                info = "Minimum Frequency: ${coreValue.first} MHz",
                info2 = "Maximum Frequency: ${coreValue.second} MHz",
                info3 = "Governor: $governor"
            )
        }
        header { HeaderLine(tittle = "CPU Usage")}
        itemsIndexed (cpuFreqs) { index: Int, freq: Int ->
            IndividualLine(tittle = "Core ${index+1}", info = "$freq MHz")
        }
        //item { IndividualLine(tittle = "Raw Info", info = getSocRawInfo()) }
        header { HeaderLine(tittle = "GPU Info") }
        item { IndividualLine(tittle = "glEs Version", info = activityManager.getGlEsVersion()) }
        item { IndividualLine(tittle = "GPU Renderer", info = getGpuRenderer()) }
        item { IndividualLine(tittle = "GPU Vendor", info = getGpuVendor()) }

    }
}

@Composable
fun BigTitle(title: String, icon: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .clip(shape = RoundedCornerShape(25.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(25.dp)
            )
            .clickable {
                onClick()
            }) {
        Spacer(modifier = Modifier.padding(10.dp))
        Icon(imageVector = ImageVector.vectorResource(icon), contentDescription = title, modifier = Modifier
            .padding(10.dp)
            .size(40.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = title, fontSize = 25.sp, modifier = Modifier.padding(10.dp))
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
        Text(text = tittle, fontSize = 18.sp,  modifier = Modifier.padding(5.dp))
        Text(text = info, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(5.dp))
        if (info2.isNotEmpty())
            Text(text = info2, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(5.dp))
        if (info3.isNotEmpty())
            Text(text = info3, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(5.dp))
    }
}


fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

@Composable
fun HeaderLine(tittle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 30.dp,
                vertical = 10.dp
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = tittle,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
        )
    }
}



@Composable
fun MainNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    currentRoute: String?
) {
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
        composable(route = NavigationItem.Home.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/home"
                }
            )){
            HomeScreen(navController = navController, currentRoute = currentRoute, paddingValues = paddingValues)
        }
        composable(route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/system"
                }
            )){
            SystemScreen(paddingValues = paddingValues, onClick = {
                navController.returnToHome()
            })
        }
        composable(route = NavigationItem.Android.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/android"
                }
            )){
            AndroidScreen (paddingValues = paddingValues, onClick = {
                navController.returnToHome()
            })
        }
        composable(route = NavigationItem.SOC.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/soc"
                }
            )){
            HardwareScreen{
                navController.returnToHome()
            }
        }
        composable(route = NavigationItem.Display.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/display"
                }
            )){
           DisplayScreen {
               navController.returnToHome()
           }
        }
        composable(route = NavigationItem.Battery.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/battery"
                }
            )){
            BatteryScreen (paddingValues = paddingValues, onClick = {
                navController.returnToHome()
            })
        }
        composable(route = NavigationItem.Memory.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/memory"
                }
            )){
            MemoryScreen(paddingValues = paddingValues, onClick = {
                navController.returnToHome()
            })
        }
        composable(route = NavigationItem.Network.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/network"
                }
            )){
            NetworkScreen{
                navController.returnToHome()
            }
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


