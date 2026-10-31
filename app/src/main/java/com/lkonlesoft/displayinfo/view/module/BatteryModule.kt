package com.lkonlesoft.displayinfo.view.module


import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.service.ChargingTileService
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import com.lkonlesoft.displayinfo.view.components.BigPercentageValue
import com.lkonlesoft.displayinfo.view.components.CardDashboard
import com.lkonlesoft.displayinfo.view.components.GeneralProgressBar
import com.lkonlesoft.displayinfo.view.components.GeneralStatRow
import com.lkonlesoft.displayinfo.view.components.GeneralWarning
import com.lkonlesoft.displayinfo.view.components.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.components.HeaderLine
import com.lkonlesoft.displayinfo.view.components.IndividualLine
import com.lkonlesoft.displayinfo.view.components.staggeredHeader
import com.lkonlesoft.displayinfo.widget.BatteryWidgetReceiver
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun BatteryDashboard(intervalMillis: Long = 2000L,onClick: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val batteryUtils = remember { BatteryUtils(context) }
    val infoList = remember(refreshKey) { batteryUtils.getDashboardData() }
    val batteryLevel = remember(infoList) { infoList.first()  }
    val batteryDetails = remember(infoList) { infoList.filter { it != infoList.first() } }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis.milliseconds)
            refreshKey++
        }
    }

    CardDashboard(
        onClick = onClick
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
fun BatteryScreen(
    longPressCopy: Boolean,
    copyTitle: Boolean,
    showNotice: Boolean,
    paddingValues: PaddingValues,
    onNavigateToChargingScreen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val batteryUtils = remember { BatteryUtils(context) }
    val generalData = remember(refreshKey) { batteryUtils.getGeneralData() }
    val batteryLevel = remember(generalData) { generalData.first() }
    val batteryDetails = remember(generalData) { generalData.filter { it != generalData.first() } }
    val powerData = remember(refreshKey) { batteryUtils.getPowerData() }
    val chargeStatus = remember(powerData) { powerData.first() }
    val powerDetails = remember(powerData) { powerData.filter { it != powerData.first() } }
    val isCharging = chargeStatus.value.toString() == stringResource(R.string.charging)
    val infiniteTransition = rememberInfiniteTransition(label = "ChargingEffect")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ChargingPulseAlpha"
    )
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    LaunchedEffect(Unit) {
        while (true){
            delay(1.seconds)
            refreshKey++
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility (isCharging,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind{
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryContainerColor.copy(alpha = pulseAlpha * 0.5f),
                                    primaryContainerColor
                                )
                            )
                        )
                    }
            )
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(320.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection))
                .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
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
                    HeaderLine(title = stringResource(R.string.general))
                    batteryDetails.forEachIndexed { index, it ->
                        val isFirst = index == 0
                        val isLast = index == batteryDetails.lastIndex
                        IndividualLine(
                            title = stringResource(it.name),
                            info = "${it.value}${it.extra}",
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isFirst = isFirst,
                            isLast = isLast
                        )
                    }
                }
            }
            item {
                Column {
                    HeaderLine(title = chargeStatus.value.toString())
                    powerDetails.forEachIndexed { index, it ->
                        val isFirst = index == 0
                        val isLast = index == powerDetails.lastIndex
                        IndividualLine(
                            title = stringResource(it.name),
                            info = "${it.value}${it.extra}",
                            canLongPress = longPressCopy,
                            copyTitle = copyTitle,
                            isFirst = isFirst,
                            isLast = isLast
                        )
                    }
                }
            }
            item {
                GeneralWarning(
                    modifier = Modifier.animateContentSize(tween(250)),
                    title = R.string.charging_screen,
                    text = R.string.charging_screen_details,
                    icon = R.drawable.mobile_charge_24px,
                    extra = {
                        Column(
                            modifier = Modifier.animateContentSize(tween(250)),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimatedVisibility(
                                visible = isCharging,
                                enter = fadeIn() + scaleIn(tween(250)),
                                exit = fadeOut() + scaleOut(tween(250))
                            ) {
                                Button(
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {
                                        onNavigateToChargingScreen?.invoke()
                                    }
                                ) {
                                    Text(stringResource(R.string.charging_screen))
                                    Icon(
                                        modifier = Modifier.padding(start = 8.dp),
                                        imageVector = ImageVector.vectorResource(R.drawable.open_in_new_24px),
                                        contentDescription = null
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.add_quick_settings_tile_details),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                val statusBarService =
                                    context.getSystemService(StatusBarManager::class.java)
                                val componentName =
                                    ComponentName(context, ChargingTileService::class.java)
                                val title = resources.getString(R.string.charging_screen)
                                val icon =
                                    Icon.createWithResource(context, R.drawable.mobile_charge_24px)
                                FilledTonalButton(
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {
                                        statusBarService.requestAddTileService(
                                            componentName,
                                            title,
                                            icon,
                                            ContextCompat.getMainExecutor(context)
                                        ) { result ->
                                            val message = when (result) {
                                                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> resources.getString(
                                                    R.string.tile_added
                                                )

                                                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> resources.getString(
                                                    R.string.tile_already_added
                                                )

                                                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> resources.getString(
                                                    R.string.cancel
                                                )

                                                else -> resources.getString(R.string.unknown)
                                            }
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.add_quick_settings_tile))
                                }
                            }
                        }
                    }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                item {
                    GeneralWarning(
                        title = R.string.cycle_count,
                        text = R.string.battery_notice_2,
                        icon = R.drawable.outline_info_24,
                        extra = {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val widgetProvider =
                                ComponentName(context, BatteryWidgetReceiver::class.java)
                            FilledTonalButton(
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
            if (showNotice) {
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
}
