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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.DisplayUtils

@Composable
fun DisplayDashboard(onBack: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val resources = context.resources
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
            Text("📱 Display", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow("Resolution", "${DisplayUtils.getHeightPx(resources)}" + " • " +  "${DisplayUtils.getWidthPx(resources)}")
            GeneralStatRow("Smallest dp", "${DisplayUtils.getSmallestDp(resources)}")
            GeneralStatRow("X dpi", "${DisplayUtils.getXDpi(resources)}")
            GeneralStatRow("Y dpi", "${DisplayUtils.getYDpi(resources)}")
            GeneralStatRow("Width dp", "${DisplayUtils.getWidthDp(resources)}")
            GeneralStatRow("Height dp", "${DisplayUtils.getHeightDp(resources)}")
        }
    }
}