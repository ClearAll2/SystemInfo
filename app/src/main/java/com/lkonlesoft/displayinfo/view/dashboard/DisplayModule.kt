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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.DisplayUtils

@Composable
fun DisplayDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val resources = context.resources

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.display), icon = R.drawable.outline_smartphone_24)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow(stringResource(R.string.display_pixels), "${DisplayUtils.getHeightPx(resources)}" + " • " +  "${DisplayUtils.getWidthPx(resources)}")
            GeneralStatRow(stringResource(R.string.smallest_dp), "${DisplayUtils.getSmallestDp(resources)}")
            GeneralStatRow(stringResource(R.string.xdpi), "${DisplayUtils.getXDpi(resources)}")
            GeneralStatRow(stringResource(R.string.ydpi), "${DisplayUtils.getYDpi(resources)}")
            GeneralStatRow(stringResource(R.string.width_dp), "${DisplayUtils.getWidthDp(resources)}")
            GeneralStatRow(stringResource(R.string.height_dp), "${DisplayUtils.getHeightDp(resources)}")
        }
    }
}