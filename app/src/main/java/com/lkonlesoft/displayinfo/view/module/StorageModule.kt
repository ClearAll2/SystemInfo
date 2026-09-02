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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.StorageUtils
import com.lkonlesoft.displayinfo.view.BigPercentageValue
import com.lkonlesoft.displayinfo.view.GeneralProgressBar
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MemoryDashBoard(intervalMillis: Long = 5000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfoList = remember (refreshKey) { StorageUtils(context).getRAMInfo() }
    val detailsInfo = remember(ramInfoList) { ramInfoList.filter { it.type != 1 } }

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
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.memory),
                icon = R.drawable.memory_24px
            )
            BigPercentageValue(value = ramInfoList.first().value.toString(), fontSize = 36.sp)
            Spacer(Modifier.height(12.dp))
            GeneralProgressBar(
                (ramInfoList[2].value as Number).toLong(),
                (ramInfoList[3].value as Number).toLong(),
                1
            )
            Spacer(modifier = Modifier.height(12.dp))
            detailsInfo.forEach {
                GeneralStatRow(stringResource(it.name), it.value.toString() + it.extra)
            }
        }
    }
}

@Composable
fun StorageDashboard(intervalMillis: Long = 60000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var internalStorageStats by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    val detailsInfo = remember(internalStorageStats) { internalStorageStats.filter { it != internalStorageStats.first() } }
    // Auto-refresh every 60 seconds
    LaunchedEffect(Unit) {
        while (true) {
            withContext(Dispatchers.IO) {
                internalStorageStats = StorageUtils(context).getInternalStorageInfo()
            }
            delay(intervalMillis.milliseconds)
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
                title = stringResource(R.string.storage),
                icon = R.drawable.outline_storage_24
            )
            if (internalStorageStats.isNotEmpty()) {
                BigPercentageValue(
                    value = internalStorageStats.first().value.toString(),
                    fontSize = 36.sp
                )
                Spacer(Modifier.height(12.dp))
                GeneralProgressBar(
                    (internalStorageStats[2].value as Number).toLong(),
                    (internalStorageStats[3].value as Number).toLong(),
                    1
                )
                Spacer(modifier = Modifier.height(12.dp))
                detailsInfo.forEach {
                    GeneralStatRow(
                        stringResource(it.name),
                        if (it.type == 0) it.extra else it.value.toString() + it.extra
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfo = remember(refreshKey) { StorageUtils(context).getRAMInfo() }
    val filteredRamInfo = remember(ramInfo) { ramInfo.filter { it != ramInfo.first() } }
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L.milliseconds)
            refreshKey++ // Triggers recomposition
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(320.dp),
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
                BigPercentageValue(value = ramInfo.first().value.toString())
                GeneralProgressBar(
                    (ramInfo[2].value as Number).toLong(), (ramInfo[3].value as Number).toLong(), 1,
                    height = 32.dp,
                    verticalPadding = 15.dp
                )
                filteredRamInfo.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = filteredRamInfo.last() == it,
                        topStart = if (filteredRamInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (filteredRamInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (filteredRamInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (filteredRamInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        header {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
fun StorageScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var internalStorageStats by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    val detailsInternal = remember(internalStorageStats) { internalStorageStats.filter { it != internalStorageStats.first() } }
    var externalStorageStats by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    val detailsExternal = remember(externalStorageStats) { externalStorageStats.filter { it != externalStorageStats.first() } }
    LaunchedEffect(Unit) {
        while (true) {
            withContext(Dispatchers.IO) {
                internalStorageStats = StorageUtils(context).getInternalStorageInfo()
                externalStorageStats = StorageUtils(context).getExternalStorageInfo()
            }
            delay(30000L.milliseconds)
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(320.dp),
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
        if (detailsInternal.isNotEmpty()) {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.internal_storage))
                    BigPercentageValue(value = internalStorageStats.first().value.toString())
                    GeneralProgressBar(
                        (internalStorageStats[2].value as Number).toLong(),
                        (internalStorageStats[3].value as Number).toLong(),
                        1,
                        height = 32.dp,
                        verticalPadding = 15.dp
                    )
                    detailsInternal.forEach {
                        IndividualLine(
                            title = stringResource(it.name),
                            info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = detailsInternal.last() == it,
                            topStart = if (detailsInternal.first() == it) 20.dp else 5.dp,
                            topEnd = if (detailsInternal.first() == it) 20.dp else 5.dp,
                            bottomStart = if (detailsInternal.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (detailsInternal.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        if (externalStorageStats.isNotEmpty()) {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.external_storage))
                    BigPercentageValue(value = externalStorageStats.first().value.toString())
                    GeneralProgressBar(
                        (externalStorageStats[2].value as Number).toLong(),
                        (externalStorageStats[3].value as Number).toLong(),
                        1,
                        height = 32.dp,
                        verticalPadding = 15.dp
                    )
                    detailsExternal.forEach {
                        IndividualLine(
                            title = stringResource(it.name),
                            info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = detailsExternal.last() == it,
                            topStart = if (detailsExternal.first() == it) 20.dp else 5.dp,
                            topEnd = if (detailsExternal.first() == it) 20.dp else 5.dp,
                            bottomStart = if (detailsExternal.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (detailsExternal.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        header {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}
