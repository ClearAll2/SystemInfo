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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.SystemUtils

@Composable
fun SystemDashboard(onBack: () -> Unit, onClick: () -> Unit) {
    BackHandler {
        onBack()
    }
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "System",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Model", SystemUtils.getModel())
            GeneralStatRow("Product", SystemUtils.getProduct())
            GeneralStatRow("Device", SystemUtils.getDevice())
            GeneralStatRow(
                "Manufacturer",
                SystemUtils.getManufacturer()
            )
            GeneralStatRow("Up time", SystemUtils.getUptime())
        }
    }
}