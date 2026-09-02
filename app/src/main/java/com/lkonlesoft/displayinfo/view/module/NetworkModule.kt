package com.lkonlesoft.displayinfo.view.module

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.helper.hasPermission
import com.lkonlesoft.displayinfo.utils.NetworkUtils
import com.lkonlesoft.displayinfo.view.ConfirmActionPopup
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NetworkDashboard(intervalMillis: Long = 5000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList = remember(refreshKey) { NetworkUtils(context).getDashboardData() }
    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis.milliseconds)
            refreshKey++ // Triggers recomposition
        }
    }
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.network),
                icon = R.drawable.android_cell_4_bar_24px
            )
            Spacer(modifier = Modifier.height(12.dp))
            infoList.forEach {
                GeneralStatRow(
                    label = stringResource(it.name),
                    value = it.value.toString() + it.extra
                )
            }
        }
    }
}

@Composable
fun NetworkScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resource = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var showWarningPopup by remember { mutableStateOf(false) }
    val hasPhonePermission = remember(refreshKey) { context.hasPermission(Manifest.permission.READ_PHONE_STATE) }
    val hasLocationPermission = remember(refreshKey) { context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
    val networkType = remember(refreshKey) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils(context).getNetwork() else NetworkUtils(context).getNetworkOldApi()
    }
    val infoList = remember(refreshKey) { NetworkUtils(context).getDetailsInfo() }
    var wifiInfoList by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    val simInfoList = remember(refreshKey) { NetworkUtils(context).getSimInfo() }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, resource.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            refreshKey++
        } else {
            Toast.makeText(context, resource.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            showWarningPopup = true
        }
    }
    val startSettingForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshKey++
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    //only reload data when network changes or has location permission, reduce location access frequency
    LaunchedEffect(networkType, hasLocationPermission) {
        wifiInfoList = NetworkUtils(context).getWifiDetails()
    }
    LaunchedEffect(Unit) {
        while (true){
            delay(3000L.milliseconds)
            refreshKey++
        }
    }
    AnimatedVisibility(visible = showWarningPopup,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        ConfirmActionPopup(
            content = {},
            mainText = stringResource(id = R.string.permission_denied),
            subText = stringResource(id = R.string.permission_denied_details),
            confirmText = stringResource(id = R.string.settings),
            cancelText = stringResource(id = R.string.cancel),
            onDismiss = {
                showWarningPopup = !showWarningPopup
            },
            onClick = {
                showWarningPopup = !showWarningPopup
                startSettingForResult.launch(intent)
            }
        )
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = paddingValues.calculateTopPadding())
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
        ),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                IndividualLine(title = stringResource(R.string.network_type), info = networkType,
                    onClick = {
                        if (!hasPhonePermission)
                            permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                    },
                    canLongPress = longPressCopy,
                    copyTitle = copyTitle,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                    isLast = true
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.wifi))
                if (wifiInfoList.isNotEmpty()) {
                    wifiInfoList.forEach {
                        IndividualLine(
                            title = stringResource(it.name),
                            info = if (wifiInfoList.first() == it && !hasLocationPermission) stringResource(R.string.require_permission) else it.value.toString() + it.extra,
                            onClick = {
                                if (wifiInfoList.first() == it && !hasLocationPermission)
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = wifiInfoList.last() == it,
                            topStart = if (wifiInfoList.first() == it) 20.dp else 5.dp,
                            topEnd = if (wifiInfoList.first() == it) 20.dp else 5.dp,
                            bottomStart = if (wifiInfoList.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (wifiInfoList.last() == it) 20.dp else 5.dp
                        )
                    }
                }
                else {
                    IndividualLine(
                        title = stringResource(R.string.wifi),
                        info = stringResource(R.string.n_a),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp,
                        isLast = true
                    )
                }
            }
        }
        if (simInfoList.isNotEmpty() && hasPhonePermission) {
            itemsIndexed(simInfoList) { index, simInfo ->
                Column {
                    HeaderLine(tittle = "SIM #${index+1}")
                    simInfo.forEach {
                        IndividualLine(
                            title = stringResource(it.name),
                            info = it.value.toString(),
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = simInfo.last() == it,
                            topStart = if (simInfo.first() == it) 20.dp else 5.dp,
                            topEnd = if (simInfo.first() == it) 20.dp else 5.dp,
                            bottomStart = if (simInfo.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (simInfo.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        else{
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.sim_info))
                    IndividualLine(title = stringResource(R.string.sim_info), info = if (!hasPhonePermission) stringResource(R.string.require_permission)
                    else stringResource(R.string.n_a),
                        onClick = {
                            if (!hasPhonePermission)
                                permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        },
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp,
                        isLast = true
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.details))
                infoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = infoList.last() == it,
                        topStart = if (infoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (infoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (infoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (infoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}

