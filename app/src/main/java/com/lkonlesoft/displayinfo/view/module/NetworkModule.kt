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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.hasPermission
import com.lkonlesoft.displayinfo.utils.NetworkUtils
import com.lkonlesoft.displayinfo.view.ConfirmActionPopup
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import kotlinx.coroutines.delay

@Composable
fun NetworkDashboard(intervalMillis: Long = 5000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList by remember(refreshKey) { mutableStateOf(NetworkUtils(context).getDashboardData()) }
    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++ // Triggers recomposition
        }
    }
    OutlinedCard(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.network), icon = R.drawable.outline_network_cell_24)
            Spacer(modifier = Modifier.height(12.dp))
            infoList.forEach {
                GeneralStatRow(label = stringResource(it.name), value = it.value.toString() + it.extra)
            }
        }
    }
}

@Composable
fun NetworkScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resource = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var showWarningPopup by remember { mutableStateOf(false) }
    var hasPermission by remember(refreshKey) { mutableStateOf(context.hasPermission(Manifest.permission.READ_PHONE_STATE)) }
    val networkType by remember(refreshKey) {
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NetworkUtils(context).getNetwork() else NetworkUtils(context).getNetworkOldApi())
    }
    val infoList by remember(refreshKey) { mutableStateOf(NetworkUtils(context).getDetailsInfo()) }
    val simInfoList by remember(refreshKey) { mutableStateOf(NetworkUtils(context).getSimInfo()) }
    val startForPermissionResult = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) {isGranted ->
        hasPermission = isGranted
        if (isGranted){
            Toast.makeText(context, resource.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            refreshKey++
        }
        else{
            Toast.makeText(context, resource.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            showWarningPopup = !showWarningPopup
        }
    }
    val startSettingForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasPermission = context.hasPermission(Manifest.permission.READ_PHONE_STATE)
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
    LaunchedEffect(Unit) {
        while (true){
            delay(5000L)
            refreshKey++
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                IndividualLine(tittle = stringResource(R.string.network_type), info = networkType,
                    onClick = {
                        startForPermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
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
        if (simInfoList.isNotEmpty() && hasPermission) {
            itemsIndexed(simInfoList) { index, simInfo ->
                Column {
                    HeaderLine(tittle = "SIM #${index+1}")
                    simInfo.forEach {
                        IndividualLine(
                            tittle = stringResource(it.name),
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
                    IndividualLine(tittle = stringResource(R.string.sim_info), info = stringResource(R.string.require_permission),
                        onClick = {
                            startForPermissionResult.launch(Manifest.permission.READ_PHONE_STATE)
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
                    IndividualLine(tittle = stringResource(it.name),
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
    }
}

