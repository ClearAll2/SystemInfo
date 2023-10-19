package com.lkonlesoft.displayinfo.view

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            ScreenInfoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background)
                {
                    ScaffoldContext(onClick = {startAboutActivity()})
                }
            }
        }
    }

    private fun startAboutActivity(){
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(onClick: () -> Unit){
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text =
                when(currentRoute){
                    NavigationItem.System.route -> "System"
                    NavigationItem.Android.route -> "Android"
                    NavigationItem.Display.route -> "Display"
                    NavigationItem.Battery.route -> "Battery"
                    else -> "System Info"
                },
                color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                   if (currentRoute != NavigationItem.Home.route){
                       IconButton(onClick = {
                           navController.popBackStack()
                           navController.navigate(NavigationItem.Home.route) {
                               launchSingleTop = true
                           }
                       }) {
                           Icon(Icons.Filled.ArrowBack, "backIcon")
                       }
                   }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onClick) {
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
fun SystemScreen(navController: NavHostController) {
    val supportedABIS = Build.SUPPORTED_ABIS
    BackHandler {
        navController.popBackStack()
        navController.navigate(NavigationItem.Home.route) {
            launchSingleTop = true
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxWidth()

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
fun AndroidScreen(navController: NavHostController) {
    BackHandler {
        navController.popBackStack()
        navController.navigate(NavigationItem.Home.route) {
            launchSingleTop = true
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxWidth()

    ) {
        item { IndividualLine(tittle = "Android Version", info = Build.VERSION.RELEASE) }
        item {IndividualLine(tittle = "API Level", info = Build.VERSION.SDK_INT.toString())}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            item { IndividualLine(tittle = "Security Patch", info = Build.VERSION.SECURITY_PATCH)}
        }
        item { IndividualLine(tittle = "ID", info = Build.ID) }
        item { IndividualLine(tittle = "Build ID", info = Build.DISPLAY) }
        item { IndividualLine(tittle = "Incremental", info = Build.VERSION.INCREMENTAL) }
        item { IndividualLine(tittle = "Codename", info = Build.VERSION.CODENAME) }
        item { IndividualLine(tittle = "Type", info = Build.TYPE) }
        item { IndividualLine(tittle = "Tags", info = Build.TAGS) }
        item { IndividualLine(tittle = "Fingerprint", info = Build.FINGERPRINT) }

    }
}

@Composable
fun DisplayScreen(navController: NavHostController) {
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
    BackHandler {
        navController.popBackStack()
        navController.navigate(NavigationItem.Home.route) {
            launchSingleTop = true
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxWidth()

    ) {
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
            item {IndividualLine(tittle = "Display Type", info = LocalContext.current.display?.name.toString())}
            item {IndividualLine(tittle = "Refresh Rate", info = LocalContext.current.display?.refreshRate?.toInt().toString() + " Hz")}
        }
    }
}

@Composable
fun BatteryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
        context.registerReceiver(null, filter)
    }
    val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
            || status == BatteryManager.BATTERY_STATUS_FULL
    BackHandler {
        navController.popBackStack()
        navController.navigate(NavigationItem.Home.route) {
            launchSingleTop = true
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxWidth()

    ) {
        item { IndividualLine(tittle = "Status", info = if (isCharging) "Charging" else "Discharging")}
        item { IndividualLine(tittle = "Cycle Count", info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) batteryStatus?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0).toString() else "This feature requires Android 14" )}
        item { IndividualLine(tittle = "Temperature", info = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10F).toString() + " °C") }
        item { IndividualLine(tittle = "Voltage", info = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.div(1000F).toString() + " V") }
        item { IndividualLine(tittle = "Technology", info = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).toString()) }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, currentRoute: String?) {
    val listScreen = listOf(
        NavigationItem.System,
        NavigationItem.Android,
        NavigationItem.Display,
        NavigationItem.Battery,
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier
            .fillMaxWidth()

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
fun BigTitle(title: String, icon: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp)
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
fun IndividualLine(tittle: String, info: String){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 30.dp,
                vertical = 10.dp
            ),
        horizontalAlignment = Alignment.Start,
    ){
        Text(text = tittle, fontSize = 18.sp,  modifier = Modifier.padding(5.dp))
        Text(text = info, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(5.dp))
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
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(route = NavigationItem.Home.route){
            HomeScreen(navController = navController, currentRoute = currentRoute)
        }
        composable(route = NavigationItem.System.route){
            SystemScreen(navController = navController)
        }
        composable(route = NavigationItem.Android.route){
            AndroidScreen(navController = navController)
        }
        composable(route = NavigationItem.Display.route){
           DisplayScreen(navController = navController)
        }
        composable(route = NavigationItem.Battery.route){
            BatteryScreen(navController = navController)
        }
    }
}


