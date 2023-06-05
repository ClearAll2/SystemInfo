package com.lkonlesoft.displayinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jaredrummler.android.device.DeviceName
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceName.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        MobileAds.initialize(this)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Info", color = MaterialTheme.colorScheme.primary) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Rate us 5 stars!", modifier = Modifier.padding(5.dp))
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen(){
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    val screenOrientation = configuration.orientation
    val screenHeightDp = screenHeight.toString()
    val screenWidthDp = screenWidth.toString()
    val screenHeightPx = with(density) {screenHeight.dp.roundToPx()}
    val screenWidthPx = with(density) {screenWidth.dp.roundToPx()}

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 10.dp
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(15.dp)
            )) {
        AdvertView(R.string.ad_banner_id_1, Modifier.fillMaxWidth().weight(1.5f))
        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            IndividualLine(tittle = "Device Model", info = DeviceName.getDeviceName())
            IndividualLine(tittle = "Android version", info = android.os.Build.VERSION.RELEASE)
            IndividualLine(tittle = "API level", info = android.os.Build.VERSION.SDK_INT.toString())
            IndividualLine(tittle = "Drawable Density", info = densityReturn(density.density))
            IndividualLine(tittle = "Smallest Dp", info = configuration.smallestScreenWidthDp.toString())
            IndividualLine(tittle = "Screen (dpi)", info = configuration.densityDpi.toString())
            AdvertView(R.string.ad_banner_id_2, Modifier.fillMaxWidth())
            IndividualLine(tittle = "Width (dp)", info = screenWidthDp)
            IndividualLine(tittle = "Height (dp)", info = screenHeightDp)
            IndividualLine(tittle = "Orientation", info = if (screenOrientation == 1) "Portrait" else "Landscape")
            IndividualLine(tittle = "Usable Width (px)", info = screenWidthPx.toString())
            IndividualLine(tittle = "Usable Height (px)", info = screenHeightPx.toString())
            IndividualLine(tittle = "Touch screen", info = if (configuration.touchscreen == 1) "No touch" else "Finger")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            {
                IndividualLine(tittle = "Support HDR", info = if (configuration.isScreenHdr) "Yes" else "No")
                IndividualLine(tittle = "Support HLG", info = if (configuration.isScreenWideColorGamut) "Yes" else "No")


            }
        }
        AdvertView(R.string.ad_banner_id_3, Modifier.fillMaxWidth().weight(1.5f))

    }

}

fun densityReturn(density: Float): String{
    if (density >= 4.0) {
        return "xxxhdpi"
    }
    if (density >= 3.0) {
        return "xxhdpi"
    }
    if (density >= 2.0) {
        return "xhdpi"
    }
    if (density >= 1.5) {
        return "hdpi"
    }
    if (density >= 1.0) {
        return "mdpi"
    }
    return "ldpi"
}

@Composable
fun AdvertView(adId: Int, modifier: Modifier = Modifier){
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = context.getString(adId)
                    loadAd(AdRequest.Builder().build())
                }
            })
    }
}



@Composable
fun IndividualLine(tittle: String, info: String){
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)) {

        Text(text = tittle, textAlign = TextAlign.End,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .weight(1f))

        Text(text = info, textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .weight(1f), fontWeight = FontWeight.Bold)
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScreenInfoTheme {
        IndividualLine(tittle = "API level", info = "24")
    }
}