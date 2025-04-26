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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.SystemUtils

@Composable
fun SystemDashboard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.system), icon = R.drawable.outline_settings_24)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow(stringResource(R.string.model), SystemUtils.getModel())
            GeneralStatRow(stringResource(R.string.product), SystemUtils.getProduct())
            GeneralStatRow(stringResource(R.string.device), SystemUtils.getDevice())
            GeneralStatRow(stringResource(R.string.manufacturer), SystemUtils.getManufacturer())
            GeneralStatRow(stringResource(R.string.up_time), SystemUtils.getUptime())
        }
    }
}