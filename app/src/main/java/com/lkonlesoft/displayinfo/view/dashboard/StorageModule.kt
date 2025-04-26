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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.StorageUtils
import kotlinx.coroutines.delay

@Composable
fun MemoryDashBoard(intervalMillis: Long = 5000L, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++ // Triggers recomposition
        }
    }

    val totalRAM = remember(refreshKey) { StorageUtils.getTotalRAM(context) }
    val availableRAM = remember(refreshKey) { StorageUtils.getAvailableRAM(context) }
    val usedRAM = totalRAM - availableRAM


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
            GeneralProgressBar(usedRAM, totalRAM, 1)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow(stringResource(R.string.total_ram), "$totalRAM MB")
            GeneralStatRow(stringResource(R.string.used_ram), "$usedRAM MB")
            GeneralStatRow(stringResource(R.string.available_ram), "$availableRAM MB")
        }
    }
}

@Composable
fun StorageDashboard(intervalMillis: Long = 60000L, onClick: () -> Unit) {
    var refreshKey by remember { mutableIntStateOf(0) }
    // Auto-refresh every 60 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++ // Triggers recomposition
        }
    }

    val (totalStorage, freeStorage) = remember(refreshKey) { StorageUtils.getInternalStorageStats() }
    val usedStorage = totalStorage - freeStorage

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
            GeneralProgressBar(usedStorage, totalStorage, 1)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow(stringResource(R.string.total), StorageUtils.formatSize(totalStorage))
            GeneralStatRow(stringResource(R.string.used), StorageUtils.formatSize(usedStorage))
            GeneralStatRow(stringResource(R.string.free), StorageUtils.formatSize(freeStorage))
        }
    }
}
