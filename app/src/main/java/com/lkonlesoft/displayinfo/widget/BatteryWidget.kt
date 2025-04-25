package com.lkonlesoft.displayinfo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.lkonlesoft.displayinfo.utils.BatteryUtils

class BatteryWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 50.dp)
        private val MEDIUM_SQUARE = DpSize(200.dp, 100.dp)
        private val BIG_SQUARE = DpSize(300.dp, 200.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SQUARE,
            MEDIUM_SQUARE,
            BIG_SQUARE
        )
    )
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            BatteryInfoContent(context)
        }
    }

    @Composable
    fun BatteryInfoContent(context: Context) {
        val size = LocalSize.current
        val cycleCount = BatteryUtils.getBatteryCycleCount(context)
        val titleFontSize = when {
            size.height >= 50.dp && size.height < 100.dp -> 14.sp
            size.height >= 100.dp && size.height < 150.dp -> 16.sp
            else -> 18.sp
        }
        val countFontSize = when {
            size.height < 100.dp -> 20.sp
            size.height < 150.dp -> 24.sp
            else -> 26.sp
        }
        val intent = Intent(Intent.ACTION_VIEW, "si://info/battery".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔋 Cycle Count", modifier = GlanceModifier.padding(vertical = 10.dp, horizontal = 5.dp),
                style = TextStyle(
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Medium)
            )
            Text(text = if (cycleCount == -1) "N/A" else cycleCount.toString(),
                modifier = GlanceModifier.padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(fontSize = countFontSize, fontWeight = FontWeight.Bold)
                )
        }
    }
}