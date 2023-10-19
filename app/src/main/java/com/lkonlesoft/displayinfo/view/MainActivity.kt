package com.lkonlesoft.displayinfo.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.jaredrummler.android.device.DeviceName
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceName.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
    val deviceModel = DeviceName.getDeviceName()
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
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Display Info", color = MaterialTheme.colorScheme.primary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_info_24), contentDescription = "Info"
                        )
                    }
                }
            )
        }
    ) {paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(250.dp),
            modifier = Modifier
                .padding(
                    paddingValues
                )
                .fillMaxWidth()

        ) {
            item { IndividualLine(tittle = "Device", info = deviceModel) }
            item { IndividualLine(tittle = "Android version", info = Build.VERSION.RELEASE) }
            item {IndividualLine(tittle = "API level", info = Build.VERSION.SDK_INT.toString())}
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


            item {IndividualLine(tittle = "Touch screen", info = if (resources.configuration.touchscreen == 1) "No touch" else "Finger")}
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                item {IndividualLine(tittle = "Support HDR", info = if (resources.configuration.isScreenHdr) "Yes" else "No")}
                item {IndividualLine(tittle = "Support Wide Color Gamut", info = if (resources.configuration.isScreenWideColorGamut) "Yes" else "No")}
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                item {IndividualLine(tittle = "Display type", info = LocalContext.current.display?.name.toString())}
                item {IndividualLine(tittle = "Refresh rate", info = LocalContext.current.display?.refreshRate?.toInt()
                    .toString() + " Hz")}
            }
        }
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


