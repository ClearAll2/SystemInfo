package com.lkonlesoft.displayinfo.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.`object`.AboutItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val color = this.intent.getIntExtra("color", 0)
        val isDynamic = this.intent.getBooleanExtra("isDynamic", false)
        setContent {
            AboutScaffoldContext(
                color = color,
                isDynamic = isDynamic,
                onClick = { this.finish()})

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScaffoldContext(color: Int, isDynamic: Boolean, onClick: () -> Unit){
    val state = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state)
    ScreenInfoTheme (
        dynamicColor = isDynamic,
        darkTheme = when(color) {
            0 -> isSystemInDarkTheme()
            1 -> true
            else -> false
        },
    ) {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "About") },
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(onClick = onClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                            }
                        },
                    )
                }
            ) { paddingValues ->
                AboutScreen(paddingValues = paddingValues)
            }
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
                horizontal = 20.dp,
                vertical = 10.dp
            ),
        horizontalAlignment = Alignment.Start,
    ){
        Text(text = tittle, fontSize = 18.sp,  modifier = Modifier.padding(5.dp))
        Text(text = text, color = Color.Gray, modifier = Modifier.padding(5.dp))
    }
}

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    val uriHandler = LocalUriHandler.current
    val items = listOf(
        AboutItem.AppVer,
        AboutItem.Privacy,
        AboutItem.More,
        AboutItem.Contact
    )
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues),
        contentPadding = paddingValues

    ) {
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
