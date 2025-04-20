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
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.lkonlesoft.displayinfo.helper.byteToHuman
import com.lkonlesoft.displayinfo.helper.connectionStateToString
import com.lkonlesoft.displayinfo.helper.getAllCpuFrequencies
import com.lkonlesoft.displayinfo.helper.getAllGovernors
import com.lkonlesoft.displayinfo.helper.getBatteryPercentage
import com.lkonlesoft.displayinfo.helper.getChargeStatus
import com.lkonlesoft.displayinfo.helper.getClearKeyInfo
import com.lkonlesoft.displayinfo.helper.getDischargeCurrent
import com.lkonlesoft.displayinfo.helper.getFreeMemory
import com.lkonlesoft.displayinfo.helper.getGlEsVersion
import com.lkonlesoft.displayinfo.helper.getKernelVersion
import com.lkonlesoft.displayinfo.helper.getMinMaxFreq
import com.lkonlesoft.displayinfo.helper.getNetInfo
import com.lkonlesoft.displayinfo.helper.getNetwork
import com.lkonlesoft.displayinfo.helper.getNetworkOldApi
import com.lkonlesoft.displayinfo.helper.getNumberOfCores
import com.lkonlesoft.displayinfo.helper.getTotalMemory
import com.lkonlesoft.displayinfo.helper.getUsedMemory
import com.lkonlesoft.displayinfo.helper.getWidevineInfo
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


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
        Box (modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            MainNavigation(navController = navController, currentRoute = currentRoute)
        }
    }
}

@Composable
fun SystemScreen(onClick: () -> Unit) {
    val supportedABIS = Build.SUPPORTED_ABIS
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        header { HeaderLine(tittle = "Device") }
        item { IndividualLine(tittle = "Model", info = Build.MODEL) }
        item { IndividualLine(tittle = "Product", info = Build.PRODUCT) }
        item { IndividualLine(tittle = "Device", info = Build.DEVICE) }
        item { IndividualLine(tittle = "Board", info = Build.BOARD) }
        item { IndividualLine(tittle = "Brand", info = Build.BRAND) }
        item { IndividualLine(tittle = "Manufacturer", info = Build.MANUFACTURER) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item { IndividualLine(tittle = "SKU", info = Build.SKU) }
        }
        item { IndividualLine(tittle = "Radio", info = Build.getRadioVersion()) }
        item { IndividualLine(tittle = "Instruction Sets", info = supportedABIS.joinToString(", ")) }
    }
}

@Composable
fun AndroidScreen(onClick: () -> Unit) {
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        item { IndividualLine(tittle = "Android Version", info = Build.VERSION.RELEASE) }
        item {IndividualLine(tittle = "API Level", info = Build.VERSION.SDK_INT.toString())}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            item { IndividualLine(tittle = "Security Patch", info = Build.VERSION.SECURITY_PATCH)}
            item { IndividualLine(tittle = "SDK", info = Build.VERSION.SDK_INT.toString()) }
        }
        item { IndividualLine(tittle = "ID", info = Build.ID) }
        item { IndividualLine(tittle = "Build ID", info = Build.DISPLAY) }
        item { IndividualLine(tittle = "Incremental", info = Build.VERSION.INCREMENTAL) }
        item { IndividualLine(tittle = "Codename", info = Build.VERSION.CODENAME) }
        item { IndividualLine(tittle = "Type", info = Build.TYPE) }
        item { IndividualLine(tittle = "Tags", info = Build.TAGS) }
        item { IndividualLine(tittle = "Fingerprint", info = Build.FINGERPRINT) }
        item { IndividualLine(tittle = "Kernel", info = getKernelVersion().toString()) }
        item { IndividualLine(tittle = "Bootloader", info = Build.BOOTLOADER) }
        item { IndividualLine(tittle = "Hardware", info = Build.HARDWARE) }

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
fun BatteryScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    var batteryStatus: Intent? = null
    var chargeStatus by remember { mutableStateOf("?") }
    var temper by remember { mutableStateOf("?") }
    var voltage by remember { mutableStateOf("?") }
    var batteryPercent = getBatteryPercentage(context)
    var usage by remember {  mutableStateOf(getDischargeCurrent(context)) }
    var cycleCount by remember { mutableStateOf("0") }
    var tech by remember { mutableStateOf("?") }
    var currentInMilliAmps by remember {  mutableStateOf(usage?.div(1000))}
    BackHandler {
        onClick()
    }
    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                batteryStatus = intent
                cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0).toString()
                else "Only available from Android 14"
                tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).toString()
                chargeStatus = getChargeStatus (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1))
                temper = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).div(10F).toString() + " °C"
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).div(1000F).toString() + " V"
            }
        }

        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Clean up when the composable leaves the Composition
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }
    LaunchedEffect(Unit) {
        while (true){
            chargeStatus = getChargeStatus(batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1))
            temper = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10F).toString() + " °C"
            voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.div(1000F).toString() + " V"
            batteryPercent = getBatteryPercentage(context)
            usage = getDischargeCurrent(context)
            currentInMilliAmps = usage?.div(1000)
            delay(1000L)
        }
    }
    Box (modifier = Modifier
        .fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            modifier = Modifier.fillMaxSize()

        ) {
            item { IndividualLine(tittle = "Status", info = chargeStatus)}
            item { IndividualLine(tittle = "Current", info = "$currentInMilliAmps mA")}
            item { IndividualLine(tittle = "Percentage", info = "$batteryPercent%")}
            item { IndividualLine(tittle = "Cycle Count", info = cycleCount)}
            item { IndividualLine(tittle = "Temperature", info = temper) }
            item { IndividualLine(tittle = "Voltage", info = voltage) }
            item { IndividualLine(tittle = "Technology", info = tech) }
        }
    }

}

@Composable
fun HomeScreen(navController: NavHostController, currentRoute: String?) {
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
        columns = if (width < 600.dp) GridCells.Fixed(2) else GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)

    ) {
        items(listScreen){ item ->
            val isSelected = currentRoute == item.route
            BigTitle(title = item.route, icon = item.icon) {
                if (!isSelected) {
                    navController.popBackStack()
                    navController.navigate(item.route) {
                        launchSingleTop = true
                    }
                }
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
fun MemoryScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    val actManager = context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    var availMem by remember {
        mutableLongStateOf(0L)
    }
    var totalMem by remember {
        mutableLongStateOf(0L)
    }
    var percentage by remember {
        mutableIntStateOf(0)
    }
    val internalDataMem = getTotalMemory(Environment.getDataDirectory())
    val internalUsedMem = getUsedMemory(Environment.getDataDirectory())
    val internalFreeMem = getFreeMemory(Environment.getDataDirectory())
    var externalToTal = -1L
    var externalFree = -1L
    var externalUsed = -1L
    val externalFiles = context.getExternalFilesDirs(null)
    if (externalFiles.size > 1 && externalFiles[0] != null && externalFiles[1] != null){
        externalToTal = getTotalMemory(externalFiles[1])
        externalUsed = getUsedMemory(externalFiles[1])
        externalFree = getFreeMemory(externalFiles[1])
    }
    LaunchedEffect(Unit){
        while (true) {
            actManager.getMemoryInfo(memInfo)
            availMem = memInfo.availMem / 1048576L
            totalMem = memInfo.totalMem / 1048576L
            percentage = 100 - availMem.toFloat().div(totalMem).times(100).roundToInt()
            delay(2000L)
        }
    }
    BackHandler {
        onClick()
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize()

    ) {
        header { HeaderLine(tittle = "RAM") }
        item { IndividualLine(tittle = "Used", info = "${percentage}%")}
        item { IndividualLine(tittle = "Available RAM", info = "$availMem MB")}
        item { IndividualLine(tittle = "Total RAM", info = "$totalMem MB")}
        header { HeaderLine(tittle = "Internal Storage (User Space)") }
        item { IndividualLine(tittle = "Total", info = internalDataMem.byteToHuman()) }
        item { IndividualLine(tittle = "Free", info = internalFreeMem.byteToHuman()) }
        item { IndividualLine(tittle = "Used", info = internalUsedMem.byteToHuman()) }
        if (externalToTal != -1L){
            header { HeaderLine(tittle = "SD Card") }
            item { IndividualLine(tittle = "Total", info = externalToTal.byteToHuman()) }
            item { IndividualLine(tittle = "Free", info = externalFree.byteToHuman()) }
            item { IndividualLine(tittle = "Used", info = externalUsed.byteToHuman()) }
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
        )
    }
}



@Composable
fun MainNavigation(
    navController: NavHostController,
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
            HomeScreen(navController = navController, currentRoute = currentRoute)
        }
        composable(route = NavigationItem.System.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/system"
                }
            )){
            SystemScreen{
                navController.returnToHome()
            }
        }
        composable(route = NavigationItem.Android.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/android"
                }
            )){
            AndroidScreen{
                navController.returnToHome()
            }
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
            BatteryScreen{
                navController.returnToHome()
            }
        }
        composable(route = NavigationItem.Memory.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "si://info/memory"
                }
            )){
            MemoryScreen{
                navController.returnToHome()
            }
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


