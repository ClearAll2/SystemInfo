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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.SystemUtils
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SystemDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val infoList by remember { mutableStateOf(SystemUtils(context).getDashboardData()) }
    OutlinedCard(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.system), icon = R.drawable.outline_settings_24)
            Spacer(modifier = Modifier.height(12.dp))

            infoList.forEach {
                GeneralStatRow(label = stringResource(it.name), value = it.value.toString())
            }
        }
    }
}

@Composable
fun SystemScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val deviceInfoList by remember { mutableStateOf(SystemUtils(context).getDeviceData()) }
    var rootInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    var extraInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rootInfoList = SystemUtils(context).getRootData()
            extraInfoList = SystemUtils(context).getExtraData()
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
                HeaderLine(tittle = stringResource(R.string.device))
                deviceInfoList.forEach {
                    IndividualLine(
                        tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = deviceInfoList.last() == it,
                        topStart = if (deviceInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (deviceInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (deviceInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (deviceInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.root_status))
                rootInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = rootInfoList.last() == it,
                        topStart = if (rootInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (rootInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (rootInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (rootInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.extra))
                extraInfoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        isLast = extraInfoList.last() == it,
                        topStart = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (extraInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (extraInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}