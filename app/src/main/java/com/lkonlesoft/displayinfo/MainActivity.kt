package com.lkonlesoft.displayinfo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    ScaffoldContext(onClick = {startAboutActivity()})
                }
            }
        }
        MobileAds.initialize(this)
    }

    private fun startAboutActivity(){
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldContext(onClick: () -> Unit){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val deviceModel = DeviceName.getDeviceName()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = deviceModel, color = MaterialTheme.colorScheme.primary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Icons.Filled.Info, contentDescription = "Info"
                        )
                    }
                },
                modifier = Modifier.padding(4.dp)
            )
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
fun InfoContext(){
    val resources = LocalContext.current.resources
    val density = resources.displayMetrics.densityDpi.toString()
    val scaleDensity = resources.displayMetrics.scaledDensity.toString()
    val xDpi = resources.displayMetrics.xdpi.toString()
    val yDpi = resources.displayMetrics.ydpi.toString()
    val screenOrientation = resources.configuration.orientation
    val screenHeightDp = resources.configuration.screenHeightDp.toString()
    val screenWidthDp = resources.configuration.screenWidthDp.toString()
    val screenHeightPx = resources.displayMetrics.heightPixels.toString()
    val screenWidthPx = resources.displayMetrics.widthPixels.toString()

    Column(
        Modifier
            .padding(10.dp)
            .fillMaxWidth()

        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IndividualLine(tittle = "Android version", info = android.os.Build.VERSION.RELEASE)
        IndividualLine(tittle = "API level", info = android.os.Build.VERSION.SDK_INT.toString())
        IndividualLine(tittle = "Smallest dp", info = resources.configuration.smallestScreenWidthDp.toString())
        IndividualLine(tittle = "Screen (dpi)", info = density)
        IndividualLine(tittle = "Scaled Density", info = scaleDensity)
        IndividualLine(tittle = "X dpi", info = xDpi)
        IndividualLine(tittle = "Y dpi", info = yDpi)
        IndividualLine(tittle = "Width (dp)", info = screenWidthDp)
        IndividualLine(tittle = "Height (dp)", info = screenHeightDp)
        IndividualLine(tittle = "Orientation", info = if (screenOrientation == 1) "Portrait" else "Landscape")
        IndividualLine(tittle = "Usable Width (px)", info = screenWidthPx)
        IndividualLine(tittle = "Usable Height (px)", info = screenHeightPx)


        IndividualLine(tittle = "Touch screen", info = if (resources.configuration.touchscreen == 1) "No touch" else "Finger")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            IndividualLine(tittle = "Support HDR", info = if (resources.configuration.isScreenHdr) "Yes" else "No")
            IndividualLine(tittle = "Support HLG", info = if (resources.configuration.isScreenWideColorGamut) "Yes" else "No")
        }

    }
}






@Composable
fun MainScreen(){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    if (screenWidth < 480.dp)
    {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp
                )) {

            AdvertView(
                R.string.ad_banner_id_1,
                Modifier
                    .fillMaxWidth()
                    .weight(1f), AdSize.BANNER

            )
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(8f)

            ) {
                item{ }

                item {InfoContext()}

            }
            AdvertView(
                R.string.ad_banner_id_2,
                Modifier
                    .fillMaxWidth()
                    .weight(1f), AdSize.BANNER
            )
        }

    }
    else
    {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp
                )) {

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(8f)

            ) {
                item{ }

                item {InfoContext()}

            }
            Row(Modifier.fillMaxWidth().weight(2f)) {
                AdvertView(
                    R.string.ad_banner_id_1,
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), AdSize.BANNER

                )
                AdvertView(
                    R.string.ad_banner_id_2,
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), AdSize.BANNER
                )
            }

        }

    }
}



@Composable
fun AdvertView(adId: Int, modifier: Modifier = Modifier, adSize: AdSize){
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
                    setAdSize(adSize)
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


