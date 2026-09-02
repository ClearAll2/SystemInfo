package com.lkonlesoft.displayinfo.view.module


import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import com.lkonlesoft.displayinfo.view.BigPercentageValue
import com.lkonlesoft.displayinfo.view.GeneralProgressBar
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import com.lkonlesoft.displayinfo.widget.BatteryWidgetReceiver
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BatteryDashboard(intervalMillis: Long = 2000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList = remember(refreshKey) { BatteryUtils(context).getDashboardData() }
    val batteryLevel = remember(infoList) { infoList.first()  }
    val batteryDetails = remember(infoList) { infoList.filter { it != infoList.first() } }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis.milliseconds)
            refreshKey++
        }
    }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.battery),
                icon = R.drawable.battery_android_4_24px
            )
            batteryLevel.let {
                BigPercentageValue(value = it.value.toString(), fontSize = 36.sp)
                Spacer(modifier = Modifier.height(12.dp))
                GeneralProgressBar((it.value as? Number)?.toLong() ?: 0L, 100L)
            }
            Spacer(modifier = Modifier.height(12.dp))
            batteryDetails.forEach {
                GeneralStatRow(
                    stringResource(it.name),
                    it.value.toString() + it.extra
                )
            }
        }
    }
}

@Composable
fun BatteryScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList = remember(refreshKey) { BatteryUtils(context).getAllData() }
    val batteryLevel = remember(infoList) { infoList.first() }
    val batteryDetails = remember(infoList) { infoList.filter { it != infoList.first() } }
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L.milliseconds)
            refreshKey++
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = paddingValues.calculateTopPadding())
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
        ),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                batteryLevel.let { level ->
                    BigPercentageValue(value = level.value.toString())
                    GeneralProgressBar(
                        level = (level.value as? Number)?.toLong() ?: 0L,
                        total = 100L,
                        type = 0,
                        height = 32.dp,
                        verticalPadding = 15.dp
                    )
                }
                batteryDetails.forEachIndexed { index, it ->
                    val isFirst = index == 0
                    val isLast = index == batteryDetails.lastIndex
                    IndividualLine(
                        title = stringResource(it.name),
                        info = "${it.value}${it.extra}",
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = isLast,
                        topStart = if (isFirst) 20.dp else 5.dp,
                        topEnd = if (isFirst) 20.dp else 5.dp,
                        bottomStart = if (isLast) 20.dp else 5.dp,
                        bottomEnd = if (isLast) 20.dp else 5.dp
                    )
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            item {
                GeneralWarning(
                    title = R.string.cycle_count,
                    text = R.string.battery_notice_2,
                    icon = R.drawable.outline_info_24,
                    extra = {
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val widgetProvider = ComponentName(context, BatteryWidgetReceiver::class.java)
                        Button(
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.padding(bottom = 10.dp),
                            onClick = {
                                appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
                            }
                        ) {
                            Text(stringResource(R.string.add_battery_widget))
                        }
                    }
                )
            }
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.battery_notice_title,
                    text = R.string.battery_notice
                )
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}
