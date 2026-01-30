package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.AppInfo
import com.lkonlesoft.displayinfo.utils.PackageUtils
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppsScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var allApps by rememberSaveable { mutableStateOf(emptyList<AppInfo>()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            allApps = PackageUtils(context).getAllPackages().sortedBy { it.name.lowercase() }
            isLoading = false
        }
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        }
    }
    else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(320.dp),
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            staggeredHeader {
                Column {
                    allApps.forEach { app ->
                        IndividualLine(
                            tittle = app.name,
                            info = buildString {
                                append(app.packageName)
                                append("\n")
                                append(stringResource(R.string.version))
                                append(" ")
                                append(app.versionName)
                            },
                            icon = app.icon,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = allApps.last() == app,
                            topStart = if (allApps.first() == app) 20.dp else 5.dp,
                            topEnd = if (allApps.first() == app) 20.dp else 5.dp,
                            bottomStart = if (allApps.last() == app) 20.dp else 5.dp,
                            bottomEnd = if (allApps.last() == app) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
    }
}