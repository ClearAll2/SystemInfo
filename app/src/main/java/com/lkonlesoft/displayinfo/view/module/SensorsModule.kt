package com.lkonlesoft.displayinfo.view.module

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.SensorUtils
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader

@Composable
fun SensorsDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val sensorCount = remember { SensorUtils(context).getSensorCount() }

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
                title = stringResource(R.string.sensors),
                icon = R.drawable.sensors_24px
            )
            Spacer(modifier = Modifier.height(12.dp))
            GeneralStatRow(
                stringResource(R.string.sensor_count),
                sensorCount.toString()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SensorsScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val sensorUtils = remember { SensorUtils(context) }
    val sensorList = remember { sensorUtils.getSensorList() }

    AnimatedContent(
        targetState = sensorList.isEmpty(),
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { empty ->
        if (empty) {
            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape())
                        .background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = MaterialShapes.Cookie12Sided.toShape()
                        )
                ) {
                    Text(
                        text = stringResource(R.string.no_sensors_found),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        else {
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
                items(sensorList) { sensor ->
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        val details = remember { sensorUtils.getSensorDetails(sensor) }
                        details.forEachIndexed { index, it ->
                            val isFirst = index == 0
                            val isLast = index == details.lastIndex
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
                staggeredHeader {
                    Spacer(modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}
