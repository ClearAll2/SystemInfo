package com.lkonlesoft.displayinfo.widget

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.lkonlesoft.displayinfo.helper.getBatteryStatus

class BatteryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            MyContent(context)
        }
    }

    @Composable
    fun MyContent(context: Context) {
        val batteryStatus = getBatteryStatus(context)
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(vertical = 10.dp, horizontal = 10.dp).background(
                MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(text = "Cycle Count", modifier = GlanceModifier.padding(vertical = 10.dp, horizontal = 5.dp))
            Text(text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) batteryStatus?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0).toString()
            else "N/A",
                modifier = GlanceModifier.padding(vertical = 10.dp, horizontal = 5.dp),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
        }
    }
}