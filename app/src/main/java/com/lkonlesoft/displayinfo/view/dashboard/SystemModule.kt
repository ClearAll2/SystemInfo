package com.lkonlesoft.displayinfo.view.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.SystemUtils

@Composable
fun SystemDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val infoList by remember { mutableStateOf(SystemUtils(context).getDashboardData()) }
    OutlinedCard(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.system), icon = R.drawable.outline_settings_24)
            Spacer(modifier = Modifier.height(12.dp))

            infoList.forEach {
                GeneralStatRow(label = stringResource(it.name), value = it.value.toString())
            }
        }
    }
}