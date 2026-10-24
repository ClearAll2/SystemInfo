package com.lkonlesoft.displayinfo.view.monitors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.CpuSnapshot
import com.lkonlesoft.displayinfo.utils.SocUtils
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.LiveChart
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CpuMonitorScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    var cpuSnapshot by remember { mutableStateOf<CpuSnapshot?>(null) }
    var refreshInterval by remember { mutableLongStateOf(1000L) }

    val overallHistory = remember { mutableStateOf(listOf<Float>()) }
    val coreHistories = remember { mutableStateMapOf<Int, MutableList<Float>>() }

    LaunchedEffect(refreshInterval) {
        val socUtils = SocUtils(context)
        while (true) {
            withContext(Dispatchers.IO) {
                val snapshot = socUtils.getCpuSnapshot()
                withContext(Dispatchers.Main) {
                    cpuSnapshot = snapshot
                    // Update overall history
                    val currentOverall = overallHistory.value.toMutableList()
                    currentOverall.add(snapshot.overallUsagePercent)
                    if (currentOverall.size > 40) currentOverall.removeAt(0)
                    overallHistory.value = currentOverall

                    // Update per-core history
                    snapshot.coreStates.forEach { core ->
                        val history = coreHistories.getOrPut(core.coreIndex) { mutableListOf() }
                        history.add(core.usagePercent)
                        if (history.size > 40) history.removeAt(0)
                    }
                }
            }
            delay(refreshInterval.milliseconds)
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection)
            )
            .padding(horizontal = 20.dp)
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        staggeredHeader {
            HeaderLine(title = stringResource(R.string.realtime_chart))
        }
        // Top Header Overview Card
        item {
            Card(
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.developer_board_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.overall_cpu_usage),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${cpuSnapshot?.overallUsagePercent?.toInt() ?: 0}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = buildString {
                            append(stringResource(R.string.active_cores))
                            append(": ")
                            append("${cpuSnapshot?.activeCoreCount ?: 0} / ${cpuSnapshot?.totalCoreCount ?: 0}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LiveChart(
                        dataPoints = overallHistory.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls / Interval Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected =  refreshInterval == 500L,
                            onClick = { refreshInterval = 500L },
                            label = { Text("0.5s") }
                        )
                        FilterChip(
                            selected =refreshInterval == 1000L,
                            onClick = { refreshInterval = 1000L },
                            label = { Text("1s") }
                        )
                        FilterChip(
                            selected = refreshInterval == 2000L,
                            onClick = { refreshInterval = 2000L },
                            label = { Text("2s") }
                        )
                    }
                }
            }
        }

        // Per-Core Header
        staggeredHeader {
            HeaderLine(title = stringResource(R.string.cpu_cores_usage))
        }

        // Per-Core Cards
        cpuSnapshot?.coreStates?.let { cores ->
            items(cores) { core ->
                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.core, core.coreIndex + 1),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (core.isOffline) stringResource(R.string.offline) else "${core.usagePercent.toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (core.isOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { if (core.isOffline) 0f else (core.usagePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.frequency)}: ${if (core.isOffline) "0" else core.currentFreqMhz.toString()} MHz",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stringResource(R.string.governor)}: ${core.governor}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        LiveChart(
                            dataPoints = coreHistories[core.coreIndex] ?: emptyList(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            lineColor = MaterialTheme.colorScheme.secondary,
                            fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}
