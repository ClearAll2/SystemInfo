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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.AndroidUtils

@Composable
fun AndroidDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.android), icon = R.drawable.outline_android_24)
            Spacer(modifier = Modifier.height(8.dp))

            GeneralStatRow(stringResource(R.string.android_version), AndroidUtils.getAndroidVersion())
            GeneralStatRow(stringResource(R.string.api_level), AndroidUtils.getApiLevel().toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                GeneralStatRow(stringResource(R.string.security_patch), AndroidUtils.getSecurityPatch())
            }
            GeneralStatRow(stringResource(R.string.sdk), AndroidUtils.getSdkName())
            GeneralStatRow(stringResource(R.string.google_play_service), AndroidUtils.getGmsVersion(context))
        }
    }
}