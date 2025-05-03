package com.lkonlesoft.displayinfo.view.dashboard


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import com.lkonlesoft.displayinfo.helper.getBatteryLevelColor
import com.lkonlesoft.displayinfo.helper.getMemoryLevelColor
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import kotlinx.coroutines.delay

@Composable
fun BatteryDashboard(intervalMillis: Long = 2000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList by remember(refreshKey) { mutableStateOf<List<DeviceInfo>>(BatteryUtils(context).getDashboardData()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++
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
            HeaderForDashboard(title = stringResource(R.string.battery), icon = R.drawable.outline_battery_4_bar_24)
            Spacer(modifier = Modifier.height(12.dp))
            GeneralProgressBar((infoList[0].value as Number).toLong(), 100L)
            Spacer(modifier = Modifier.height(12.dp))
            infoList.forEach {
                GeneralStatRow(stringResource(it.name),
                    it.value.toString() + it.extra.toString()
                )
            }
        }
    }
}

@Composable
fun HeaderForDashboard(title: String, icon: Int) {
    Row (modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = ImageVector.vectorResource(icon),
            contentDescription = title,
            modifier = Modifier.size(48.dp).padding(end = 10.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
        Text(value, fontSize = 16.sp, color = valueColor, textAlign = TextAlign.End)
    }
}

@Composable
fun GeneralProgressBar(level: Long, total: Long, type: Int = 0, horizontalPadding: Dp = 0.dp, verticalPadding: Dp = 0.dp) {
    LinearProgressIndicator(
        progress = { level.toFloat().div(total) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .height(10.dp)
            .clip(MaterialTheme.shapes.medium),
        color = if (type == 0) getBatteryLevelColor(level) else getMemoryLevelColor(((level.toDouble() / total.toDouble()) * 100).toLong())
    )
}
