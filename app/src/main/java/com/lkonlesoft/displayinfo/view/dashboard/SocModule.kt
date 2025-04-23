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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.SocUtils
import kotlinx.coroutines.delay

@Composable
fun SoCDashBoard(intervalMillis: Long = 1000L, onBack: () -> Unit, onClick: () -> Unit) {
    var cpuFreqs by remember { mutableStateOf(listOf<Int>()) }

    BackHandler {
        onBack()
    }
    // Auto-refresh every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            cpuFreqs = SocUtils.getAllCpuFrequencies()
            delay(intervalMillis)
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
            Text("🤖 CPU Usage", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            cpuFreqs.forEachIndexed { index, freq ->
                GeneralStatRow("Core ${index+1}", "$freq MHz")
            }

        }
    }
}