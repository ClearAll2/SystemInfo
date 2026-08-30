package com.lkonlesoft.displayinfo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.BatteryUtils

class BatteryWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(110.dp, 110.dp)
        private val MEDIUM_SQUARE = DpSize(150.dp, 150.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
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

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            BatteryInfoContent(context)
        }
    }

    @Composable
    fun BatteryInfoContent(context: Context) {
        val size = LocalSize.current
        val cycleCount = BatteryUtils(context).getBatteryCycleCount()
        val titleFontSize = when {
            size.height >= 110.dp && size.height < 150.dp -> 16.sp
            size.height >= 150.dp && size.height < 250.dp -> 18.sp
            else -> 20.sp
        }
        val countFontSize = when {
            size.height >= 110.dp && size.height < 150.dp -> 40.sp
            size.height >= 150.dp && size.height < 250.dp -> 48.sp
            else -> 52.sp
        }
        val intent = Intent(Intent.ACTION_VIEW, "si://info/battery".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(28.dp)
                .padding(8.dp)
                .background(GlanceTheme.colors.widgetBackground)
                .clickable(onClick = actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = context.getString(R.string.cycle_count),
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = GlanceModifier.size(8.dp))
            Text(text = if (cycleCount == -1) context.getString(R.string.n_a) else cycleCount.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = countFontSize,
                    fontWeight = FontWeight.Bold)
                )
        }
    }
}
