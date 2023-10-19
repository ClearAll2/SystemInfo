package com.lkonlesoft.displayinfo.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.`object`.AboutItem
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                title = { Text(text = "About") },
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
            AboutScreen()
        }
    }
}



@Composable
private fun AboutMenuItem(
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
        Text(text = tittle, fontSize = 18.sp,  modifier = Modifier.padding(5.dp))
        Text(text = text, color = Color.Gray, modifier = Modifier.padding(5.dp))
    }
}

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    val items = listOf(
        AboutItem.AppVer,
        AboutItem.IconCredit,
        AboutItem.Privacy,
        AboutItem.More,
        AboutItem.Contact
    )
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()

    ) {
        items(items){ item ->
            val url = stringResource(id = item.url)
            AboutMenuItem(tittle = item.tittle,
                text = stringResource(id = item.text),
                onItemClick = {
                    uriHandler.openUri(url)
                }
            )
        }
    }
}
