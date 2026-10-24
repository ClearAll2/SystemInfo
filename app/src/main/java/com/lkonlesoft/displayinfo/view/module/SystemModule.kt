package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lkonlesoft.displayinfo.utils.SystemUtils
import com.lkonlesoft.displayinfo.view.CardDashboard
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SystemDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val infoList = remember { SystemUtils(context).getDashboardData() }
    CardDashboard(
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.system),
                icon = R.drawable.manufacturing_24px
            )
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
    val layoutDirection = LocalLayoutDirection.current
    val systemUtils = remember { SystemUtils(context) }
    val deviceInfoList = remember { systemUtils.getDeviceData()}
    val deviceFeaturesList = remember { systemUtils.getDeviceFeatures() }
    var rootInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }
    var extraInfoList by remember { mutableStateOf(emptyList<DeviceInfo>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rootInfoList = systemUtils.getRootData()
            extraInfoList = systemUtils.getExtraData()
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection))
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(title = stringResource(R.string.device))
                deviceInfoList.forEach {
                    IndividualLine(
                        title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isFirst = deviceInfoList.first() == it,
                        isLast = deviceInfoList.last() == it
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(title = stringResource(R.string.root_status))
                rootInfoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isFirst = rootInfoList.first() == it,
                        isLast = rootInfoList.last() == it
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(title = stringResource(R.string.device_features))
                deviceFeaturesList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isFirst = deviceFeaturesList.first() == it,
                        isLast = deviceFeaturesList.last() == it
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(title = stringResource(R.string.extra))
                extraInfoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isFirst = extraInfoList.first() == it,
                        isLast = extraInfoList.last() == it
                    )
                }
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}