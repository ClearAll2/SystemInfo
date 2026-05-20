package com.lkonlesoft.displayinfo.view.module

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.BluetoothUtils
import com.lkonlesoft.displayinfo.view.ConfirmActionPopup
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.delay


@Composable
fun BluetoothDashboard(intervalMillis: Long = 5000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val hasBluetoothPermission by remember(refreshKey)  {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val infoList by remember(refreshKey) {
        mutableStateOf(if (hasBluetoothPermission) BluetoothUtils(context).getStateData()
        else emptyList()
        )
    }
    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
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
                title = stringResource(R.string.connectivity),
                icon = R.drawable.bluetooth_connected_24px
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (hasBluetoothPermission) {
                infoList.forEach {
                    GeneralStatRow(
                        label = stringResource(it.name),
                        value = it.value.toString() + it.extra
                    )
                }
            }
            else {
                GeneralStatRow(
                    label = stringResource(R.string.bluetooth),
                    value = stringResource(R.string.require_permission)
                )
            }
        }
    }
}

@Composable
fun ConnectivityScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var refreshKey2 by remember { mutableIntStateOf(0) }
    var showWarningPopup by remember { mutableStateOf(false) }
    val hasBluetoothPermission by remember(refreshKey)  {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, resources.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            refreshKey++
            refreshKey2++
        }
        else {
            showWarningPopup = !showWarningPopup
        }
    }
    val stateInfoList by remember(refreshKey) { mutableStateOf(if (hasBluetoothPermission) BluetoothUtils(context).getStateData() else emptyList()) }
    val featuresList by remember(refreshKey2) { mutableStateOf(if (hasBluetoothPermission) BluetoothUtils(context).getFeatures() else emptyList()) }
    val deviceInfoList by remember(refreshKey2) { mutableStateOf(if (hasBluetoothPermission) BluetoothUtils(context).getDeviceData() else emptyList()) }
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
                context.startActivity(intent)
            }
        )
    }
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            refreshKey++
            if (refreshKey % 10 == 0) //load device data every 10 seconds
                refreshKey2++
        }
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || hasBluetoothPermission) {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.status))
                    stateInfoList.forEach {
                        IndividualLine(title = stringResource(it.name),
                            info = it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = stateInfoList.last() == it,
                            topStart = if (stateInfoList.first() == it) 20.dp else 5.dp,
                            topEnd = if (stateInfoList.first() == it) 20.dp else 5.dp,
                            bottomStart = if (stateInfoList.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (stateInfoList.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.features))
                    featuresList.forEach {
                        IndividualLine(title = stringResource(it.name),
                            info = it.value.toString(),
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = featuresList.last() == it,
                            topStart = if (featuresList.first() == it) 20.dp else 5.dp,
                            topEnd = if (featuresList.first() == it) 20.dp else 5.dp,
                            bottomStart = if (featuresList.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (featuresList.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        else {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.status))
                    IndividualLine(
                        title = stringResource(R.string.bluetooth),
                        info = stringResource(R.string.require_permission),
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
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
                deviceInfoList.forEachIndexed { index, device ->
                    HeaderLine(tittle = buildString {
                        append(stringResource(R.string.connected_devices))
                        append(" #${index + 1}")
                    })
                    device.forEach {
                        IndividualLine(
                            title = stringResource(it.name),
                            info = it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = device.last() == it,
                            topStart = if (device.first() == it) 20.dp else 5.dp,
                            topEnd = if (device.first() == it) 20.dp else 5.dp,
                            bottomStart = if (device.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (device.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}