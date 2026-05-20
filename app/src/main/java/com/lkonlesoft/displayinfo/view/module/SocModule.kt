package com.lkonlesoft.displayinfo.view.module

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.SocUtils
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SoCDashBoard(intervalMillis: Long = 2000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var cpuUsageInfo by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while (true) {
                cpuUsageInfo = SocUtils(context).getCPUUsage()
                delay(intervalMillis)
            }
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
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.cpu_usage),
                icon = R.drawable.developer_board_24px
            )

            Spacer(modifier = Modifier.height(12.dp))
            cpuUsageInfo.forEach {
                GeneralStatRow(label = stringResource(it.name, it.value), value = it.extra)
            }
        }
    }
}

@Composable
fun HardwareScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues, showNotice: Boolean) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var cpuClusterInfo by remember { mutableStateOf<List<List<DeviceInfo>>>(emptyList()) }
    var cpuInfoList by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var cpuUsageInfo by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var gpuInfo by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        SocUtils(context).fetchGpuInfoOptimized {
            gpuInfo = it
        }
        withContext(Dispatchers.IO) {
            cpuInfoList = SocUtils(context).getCPUInfo()
            cpuClusterInfo = SocUtils(context).getCPUClusterInfo()
            while (true) {
                cpuUsageInfo = SocUtils(context).getCPUUsage()
                delay(1000L) // Update every 1 second
            }
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
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.cpu_info))
                cpuInfoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cpuInfoList.last() == it,
                        topStart = if (cpuInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (cpuInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cpuInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cpuInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        itemsIndexed(cpuClusterInfo) { index, cluster ->
            Column {
                HeaderLine(tittle = buildString {
                    append(stringResource(R.string.cluster))
                    append(" #${index+1}")
                })
                cluster.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cluster.last() == it,
                        topStart = if (cluster.first() == it) 20.dp else 5.dp,
                        topEnd = if (cluster.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cluster.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cluster.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.cpu_usage))
                cpuUsageInfo.forEach {
                    IndividualLine(title = stringResource(it.name, it.value),
                        info = it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cpuUsageInfo.last() == it,
                        topStart = if (cpuUsageInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (cpuUsageInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cpuUsageInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cpuUsageInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }

        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.gpu_info))
                gpuInfo.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = gpuInfo.last() == it,
                        topStart = if (gpuInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (gpuInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (gpuInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (gpuInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        if (showNotice) {
            staggeredHeader {
                GeneralWarning(
                    title = R.string.soc_notice_title,
                    text = R.string.soc_notice
                )
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}
