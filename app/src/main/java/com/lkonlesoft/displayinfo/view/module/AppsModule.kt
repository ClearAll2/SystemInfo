package com.lkonlesoft.displayinfo.view.module

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.AppInfo
import com.lkonlesoft.displayinfo.utils.PackageUtils
import com.lkonlesoft.displayinfo.view.IndividualLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppsScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var allApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredApps by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                allApps
            } else {
                allApps.filter { it.name.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            allApps = PackageUtils(context).getAllPackages().sortedBy { it.name.lowercase() }
            isLoading = false
        }
    }
    AnimatedContent (targetState = isLoading,
        transitionSpec = { fadeIn() togetherWith fadeOut() }) {
        if (it) {
            Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    strokeWidth = 10.dp,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        else {
            Column(modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(top = paddingValues.calculateTopPadding())) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newVal ->
                        searchQuery = newVal
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    placeholder = { Text(stringResource(R.string.find_app)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_search_24),
                            contentDescription = "search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.baseline_close_24),
                                    contentDescription = "clear"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(25.dp)
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surfaceContainer)
                        .consumeWindowInsets(paddingValues)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredApps) { app ->
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
                            isLast = filteredApps.last() == app,
                            topStart = if (filteredApps.first() == app) 20.dp else 5.dp,
                            topEnd = if (filteredApps.first() == app) 20.dp else 5.dp,
                            bottomStart = if (filteredApps.last() == app) 20.dp else 5.dp,
                            bottomEnd = if (filteredApps.last() == app) 20.dp else 5.dp,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", app.packageName, null)
                                })
                            }
                        )
                    }
                }
            }
        }
    }
}