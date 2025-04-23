package com.lkonlesoft.displayinfo.view.dashboard


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.helper.getBatteryLevelColor
import com.lkonlesoft.displayinfo.helper.getMemoryLevelColor
import com.lkonlesoft.displayinfo.helper.getStatusColor
import com.lkonlesoft.displayinfo.helper.getTemperatureColor
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import kotlinx.coroutines.delay

@Composable
fun BatteryDashboard(onBack: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val batteryPercentage = remember { mutableIntStateOf(0) }
    val batteryHealth = remember { mutableStateOf("Unknown") }
    val batteryStatus = remember { mutableStateOf("Unknown") }
    val batteryTemperature = remember { mutableFloatStateOf(0f) }
    val batteryCycles = remember { mutableIntStateOf(-1) }

    BackHandler {
        onBack()
    }
    LaunchedEffect(Unit) {
        while (true) {
            batteryPercentage.intValue = BatteryUtils.getBatteryPercentage(context)
            batteryHealth.value = BatteryUtils.getBatteryHealth(context)
            batteryStatus.value = BatteryUtils.getBatteryStatus(context)
            batteryTemperature.floatValue = BatteryUtils.getBatteryTemperature(context)
            batteryCycles.intValue = BatteryUtils.getBatteryCycleCount(context)
            delay(2000L)
        }
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔋 Battery", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))
            GeneralProgressBar(batteryPercentage.intValue.toLong(), 100L)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Battery Level", "${batteryPercentage.intValue} %", getBatteryLevelColor(batteryPercentage.intValue.toLong()))
            GeneralStatRow("Health", batteryHealth.value, getStatusColor(batteryHealth.value))
            GeneralStatRow("Status", batteryStatus.value, getStatusColor(batteryStatus.value))
            GeneralStatRow("Cycle Count", if (batteryCycles.intValue >= 0) "${batteryCycles.intValue}" else "N/A")
            GeneralStatRow("Temperature", "${batteryTemperature.floatValue} °C", getTemperatureColor(batteryTemperature.floatValue))
        }
    }
}

@Composable
fun GeneralStatRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, textAlign = TextAlign.Start)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = valueColor, textAlign = TextAlign.End)
    }
}

@Composable
fun GeneralProgressBar(level: Long, total: Long, type: Int = 0) {
    LinearProgressIndicator(
        progress = { level.toFloat() / total },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(MaterialTheme.shapes.medium),
        color = if (type == 0) getBatteryLevelColor(level) else getMemoryLevelColor(level)
    )
}
