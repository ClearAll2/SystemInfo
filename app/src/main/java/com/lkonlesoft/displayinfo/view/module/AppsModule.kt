package com.lkonlesoft.displayinfo.view.module

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.AppInfo
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.PackageUtils
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun AppDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val listInfo = PackageUtils(context).getAppCountDetails()
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.apps),
                icon = R.drawable.outline_apps_24
            )
            Spacer(modifier = Modifier.height(8.dp))
            listInfo.forEach {
                GeneralStatRow(
                    label = stringResource(it.name),
                    value = it.value.toString()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppsScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resource = LocalResources.current
    val haptic = LocalHapticFeedback.current
    val width = LocalWindowInfo.current.containerDpSize.width
    val appTypes = remember {
        mapOf(
            -1 to R.string.all,
            0 to R.string.system,
            1 to R.string.user
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var allApps by remember { mutableStateOf(emptyList<AppInfo>()) }
    var appCountInfo by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectType by rememberSaveable { mutableIntStateOf(-1) }
    val filteredApps by remember {
        derivedStateOf {
            val appsByType = if (selectType != -1) allApps.filter { it.type == selectType } else allApps

            if (searchQuery.isEmpty()) {
                appsByType
            } else {
                appsByType.filter { it.name.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            allApps = PackageUtils(context).getAllPackages().sortedBy { it.name.lowercase() }
            appCountInfo = PackageUtils(context).getAppCountDetails()
            isLoading = false
        }
    }
    AnimatedContent (targetState = isLoading,
        transitionSpec = { fadeIn() togetherWith fadeOut() }) {
        if (it) {
            Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer).fillMaxSize(), contentAlignment = Alignment.Center) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        else {
            Column(modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newVal ->
                        searchQuery = newVal
                    },
                    modifier = Modifier
                        .fillMaxWidth(if (width < 840.dp) 1f else .7f)
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
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    appTypes.entries.forEach { type ->
                        OutlinedToggleButton (
                            checked = selectType == type.key,
                            onCheckedChange = {
                                selectType = type.key
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                            shapes = when(type.key) {
                                -1 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                appTypes.keys.last() -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                disabledContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(text = "${resource.getString(type.value)} (${appCountInfo[type.key+1].value})")
                        }
                    }
                }
                /*ButtonGroup(overflowIndicator = {},
                    modifier = Modifier.fillMaxWidth(if (width < 840.dp) 1f else .7f).padding(horizontal = 20.dp).padding(bottom = 8.dp)) {
                    appTypes.entries.forEach { type ->
                        toggleableItem(
                            weight = 1f,
                            checked = selectType == type.key,
                            onCheckedChange = {
                                selectType = type.key
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                            label = buildString {
                                append(resource.getString(type.value))
                                append("\n")
                                append("(${appCountInfo[type.key + 1].value})")
                            }
                        )
                    }
                }*/
                AnimatedContent(
                    targetState = filteredApps.isNotEmpty(),
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { exist ->
                    if (exist) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(1),
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                                .fillMaxHeight()
                                .fillMaxWidth(if (width < 840.dp) 1f else .7f)
                                .padding(horizontal = 20.dp)
                                .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()), //fix edge to edge
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(filteredApps) { app ->
                                IndividualLine(
                                    title = app.name,
                                    info = buildString {
                                        append(app.packageName)
                                        append("\n\n")
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
                            staggeredHeader {
                                Spacer(modifier = Modifier.padding(20.dp))
                            }
                        }
                    }
                    else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(contentAlignment = Alignment.Center,
                                modifier = Modifier.size(240.dp).clip(MaterialShapes.Cookie12Sided.toShape())
                                    .background(color = MaterialTheme.colorScheme.surfaceBright, shape = MaterialShapes.Cookie12Sided.toShape())
                            ) {
                                Text(text = stringResource(R.string.no_apps_found), fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}