package com.lkonlesoft.displayinfo.view.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.StorageUtils
import kotlinx.coroutines.delay

@Composable
fun MemoryDashBoard(intervalMillis: Long = 2000L, onBack: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    BackHandler {
        onBack()
    }
    // Auto-refresh every 2 seconds
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
            Text("🧠 Memory", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))
            GeneralProgressBar(usedRAM, totalRAM, 1)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Total RAM", "$totalRAM MB")
            GeneralStatRow("Used RAM", "$usedRAM MB")
            GeneralStatRow("Free RAM", "$availableRAM MB")
        }
    }
}

@Composable
fun StorageDashboard(intervalMillis: Long = 60000L, onBack: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    BackHandler {
        onBack()
    }
    // Auto-refresh every 2 seconds
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
            Text("💾 Storage", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            GeneralProgressBar(usedStorage, totalStorage, 1)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Total Storage", StorageUtils.formatSize(totalStorage))
            GeneralStatRow("Used Storage", StorageUtils.formatSize(usedStorage))
            GeneralStatRow("Free Storage", StorageUtils.formatSize(freeStorage))
        }
    }
}
