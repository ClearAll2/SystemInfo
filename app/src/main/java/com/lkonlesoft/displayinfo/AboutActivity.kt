package com.lkonlesoft.displayinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.AdSize
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ScreenInfoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AboutScaffoldContext(onClick = { this.finish()})
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScaffoldContext(onClick: () -> Unit){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Display Info", color = MaterialTheme.colorScheme.primary) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
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
            About()
        }
    }
}


@Composable
fun About(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
    ) {

        Text(text = "Built by Duc Nguyen as a hobby", textAlign = TextAlign.Center, modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth())
        Text(text = "Icon made by SANB from Flaticon", textAlign = TextAlign.Center, modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth())
        AdvertView(
            R.string.ad_banner_id_1,
            Modifier
                .fillMaxWidth(), AdSize.LARGE_BANNER
        )
        AdvertView(
            R.string.ad_banner_id_2,
            Modifier
                .fillMaxWidth(), AdSize.LARGE_BANNER
        )
    }
}



@Preview
@Composable
fun Preview(){
    ScreenInfoTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AboutScaffoldContext(onClick = { })
        }
    }
}