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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.utils.AndroidUtils

@Composable
fun AndroidDashboard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Android", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            GeneralStatRow("Android Version", AndroidUtils.getAndroidVersion())
            GeneralStatRow("API Level", AndroidUtils.getApiLevel().toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                GeneralStatRow("Security Patch", AndroidUtils.getSecurityPatch())
            }
            GeneralStatRow("SDK Name", AndroidUtils.getSdkName())
        }
    }
}