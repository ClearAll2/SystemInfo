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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.DisplayUtils
import kotlinx.coroutines.delay

@Composable
fun DisplayDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val resources = context.resources
    var refreshKey by remember { mutableIntStateOf(0) }
    val isValid by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) }
    var refreshRate by remember(refreshKey) {
        mutableIntStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) DisplayUtils.getDisplayRefreshRate(context) else -1)
    }

    LaunchedEffect(Unit) {
        while (isValid) {
            delay(1000L)
            refreshKey++ // Triggers recomposition
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
            HeaderForDashboard(title = stringResource(R.string.display), icon = R.drawable.outline_smartphone_24)
            Spacer(modifier = Modifier.height(12.dp))

            GeneralStatRow(stringResource(R.string.display_pixels), "${DisplayUtils.getHeightPx(resources)}" + " • " +  "${DisplayUtils.getWidthPx(resources)}")
            GeneralStatRow(stringResource(R.string.smallest_dp), "${DisplayUtils.getSmallestDp(resources)}")
            GeneralStatRow(stringResource(R.string.xdpi), "${DisplayUtils.getXDpi(resources)}")
            GeneralStatRow(stringResource(R.string.ydpi), "${DisplayUtils.getYDpi(resources)}")
            GeneralStatRow(stringResource(R.string.width_dp), "${DisplayUtils.getWidthDp(resources)}")
            GeneralStatRow(stringResource(R.string.height_dp), "${DisplayUtils.getHeightDp(resources)}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && refreshRate != -1)
                GeneralStatRow(stringResource(R.string.refresh_rate), "${DisplayUtils.getDisplayRefreshRate(context)} Hz")
        }
    }
}