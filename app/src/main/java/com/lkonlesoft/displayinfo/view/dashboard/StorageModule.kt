package com.lkonlesoft.displayinfo.view.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import com.lkonlesoft.displayinfo.utils.StorageUtils
import kotlinx.coroutines.delay

@Composable
fun MemoryDashBoard(intervalMillis: Long = 5000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val ramInfoList by remember (refreshKey) { mutableStateOf<List<DeviceInfo>>(StorageUtils(context).getRAMInfo()) }
    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++ // Triggers recomposition
        }
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
    val internalStorageStats = remember(refreshKey) { StorageUtils(context).getInternalStorageInfo() }
    // Auto-refresh every 60 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++ // Triggers recomposition
        }
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.storage), icon = R.drawable.outline_storage_24)
            Spacer(Modifier.height(12.dp))
            GeneralProgressBar((internalStorageStats[2].value as Number).toLong(), (internalStorageStats[3].value as Number).toLong(), 1)
            Spacer(modifier = Modifier.height(12.dp))
            internalStorageStats.forEach {
                GeneralStatRow(stringResource(it.name), if (it.type == 0) it.extra.toString() else it.value.toString() + it.extra)
            }
        }
    }
}
