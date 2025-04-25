package com.lkonlesoft.displayinfo.view.dashboard

import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.NetworkUtils
import kotlinx.coroutines.delay

@Composable
fun NetworkDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    // Auto-refresh every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            refreshKey++ // Triggers recomposition
        }
    }
    var networkInfo by remember(refreshKey) { mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) NetworkUtils.getNetInfo(context) else null) }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Network", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Interface", networkInfo?.interfaces.toString())
            GeneralStatRow("IP Addresses", networkInfo?.ip.toString())
            GeneralStatRow("DNS", networkInfo?.dnsServer?.replace("/", "").toString())

        }
    }
}