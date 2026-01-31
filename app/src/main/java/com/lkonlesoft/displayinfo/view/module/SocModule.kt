package com.lkonlesoft.displayinfo.view.module

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.SocUtils
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.delay

@Composable
fun SoCDashBoard(intervalMillis: Long = 2000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val cpuUsageInfo by remember(refreshKey) { mutableStateOf(SocUtils(context).getCPUUsage()) }


    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++
        }
    }

    OutlinedCard(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.cpu_usage), icon = R.drawable.outline_developer_board_24)

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
    var refreshKey by remember { mutableIntStateOf(0) }
    val glEs by remember { mutableStateOf(SocUtils(context).getGlEsVersion()) }
    val cpuInfoList by remember { mutableStateOf(SocUtils(context).getCPUInfo()) }
    val cpuUsageInfo by remember(refreshKey) { mutableStateOf(SocUtils(context).getCPUUsage()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Update every 1 second
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
                HeaderLine(tittle = stringResource(R.string.cpu_info))
                cpuInfoList.forEach {
                    IndividualLine(tittle = if (it.type == 1) stringResource(it.name, it.value) else stringResource(it.name),
                        info = if (it.type == 1) it.extra else it.value.toString(),
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
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.cpu_usage))
                cpuUsageInfo.forEach {
                    IndividualLine(tittle = stringResource(it.name, it.value),
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
                IndividualLine(tittle = stringResource(R.string.gles_version),
                    info = glEs,
                    canLongPress = longPressCopy,
                    copyTitle = copyTitle,
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
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
    }
}