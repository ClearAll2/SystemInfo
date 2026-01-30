package com.lkonlesoft.displayinfo.view.module

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.StorageUtils
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.header
import kotlinx.coroutines.delay

@Composable
fun MemoryDashBoard(intervalMillis: Long = 5000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfoList by remember (refreshKey) { mutableStateOf(StorageUtils(context).getRAMInfo()) }
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
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.memory), icon = R.drawable.outline_memory_24)
            Spacer(Modifier.height(12.dp))
            GeneralProgressBar((ramInfoList[2].value as Number).toLong(), (ramInfoList[3].value as Number).toLong(), 1)
            Spacer(modifier = Modifier.height(12.dp))
            ramInfoList.forEach {
                GeneralStatRow(stringResource(it.name), it.value.toString() + it.extra)
            }
        }
    }
}

@Composable
fun StorageDashboard(intervalMillis: Long = 60000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val internalStorageStats by remember(refreshKey) { mutableStateOf(StorageUtils(context).getInternalStorageInfo()) }
    // Auto-refresh every 60 seconds
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
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.storage), icon = R.drawable.outline_storage_24)
            Spacer(Modifier.height(12.dp))
            GeneralProgressBar((internalStorageStats[2].value as Number).toLong(), (internalStorageStats[3].value as Number).toLong(), 1)
            Spacer(modifier = Modifier.height(12.dp))
            internalStorageStats.forEach {
                GeneralStatRow(stringResource(it.name), if (it.type == 0) it.extra else it.value.toString() + it.extra)
            }
        }
    }
}

@Composable
fun MemoryScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfo by remember(refreshKey) { mutableStateOf(StorageUtils(context).getRAMInfo()) }
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            refreshKey++ // Triggers recomposition
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        header {GeneralProgressBar((ramInfo[2].value as Number).toLong(), (ramInfo[3].value as Number).toLong(), 1,
            height = 30.dp,
            verticalPadding = 15.dp)}
        item {
            Column {
                ramInfo.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = ramInfo.last() == it,
                        topStart = if (ramInfo.first() == it) 20.dp else 5.dp,
                        topEnd = if (ramInfo.first() == it) 20.dp else 5.dp,
                        bottomStart = if (ramInfo.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (ramInfo.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun StorageScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val internalStorageStats by remember(refreshKey) { mutableStateOf(StorageUtils(context).getInternalStorageInfo()) }
    val externalStorageStats by remember(refreshKey) { mutableStateOf(StorageUtils(context).getExternalStorageInfo()) }
    // Auto-refresh every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            refreshKey++ // Triggers recomposition
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)

    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.internal_storage))
                GeneralProgressBar((internalStorageStats[2].value as Number).toLong(), (internalStorageStats[3].value as Number).toLong(), 1,
                    height = 30.dp, verticalPadding = 5.dp)
                Spacer(modifier = Modifier.padding(10.dp))
                internalStorageStats.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = internalStorageStats.last() == it,
                        topStart = if (internalStorageStats.first() == it) 20.dp else 5.dp,
                        topEnd = if (internalStorageStats.first() == it) 20.dp else 5.dp,
                        bottomStart = if (internalStorageStats.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (internalStorageStats.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        if (externalStorageStats.isNotEmpty()) {
            item {
                Column {
                    HeaderLine(tittle = stringResource(R.string.external_storage))
                    GeneralProgressBar(
                        (externalStorageStats[2].value as Number).toLong(),
                        (externalStorageStats[3].value as Number).toLong(),
                        1,
                        height = 30.dp,
                        verticalPadding = 5.dp
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    externalStorageStats.forEach {
                        IndividualLine(
                            tittle = stringResource(it.name),
                            info = if (it.type == 0) it.extra else it.value.toString() + it.extra,
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isLast = externalStorageStats.last() == it,
                            topStart = if (externalStorageStats.first() == it) 20.dp else 5.dp,
                            topEnd = if (externalStorageStats.first() == it) 20.dp else 5.dp,
                            bottomStart = if (externalStorageStats.last() == it) 20.dp else 5.dp,
                            bottomEnd = if (externalStorageStats.last() == it) 20.dp else 5.dp
                        )
                    }
                }
            }
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.storage_notice_title,
                    text = R.string.storage_notice
                )
            }
        }
    }
}
