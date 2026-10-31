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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.lkonlesoft.displayinfo.utils.NetworkTrafficSnapshot
import com.lkonlesoft.displayinfo.utils.NetworkUtils
import com.lkonlesoft.displayinfo.view.components.LiveChart
import com.lkonlesoft.displayinfo.view.components.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NetworkMonitorScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var snapshot by remember { mutableStateOf<NetworkTrafficSnapshot?>(null) }
    var refreshInterval by remember { mutableLongStateOf(1000L) }

    val downloadHistory = remember { mutableStateOf(listOf<Float>()) }
    val uploadHistory = remember { mutableStateOf(listOf<Float>()) }

    LaunchedEffect(refreshInterval) {
        val networkUtils = NetworkUtils(context)
        while (true) {
            withContext(Dispatchers.IO) {
                val currentSnapshot = networkUtils.getTrafficSnapshot()
                withContext(Dispatchers.Main) {
                    snapshot = currentSnapshot

                    val dlKbps = currentSnapshot.downloadSpeedBps / 1024f
                    val ulKbps = currentSnapshot.uploadSpeedBps / 1024f

                    val currentDlList = downloadHistory.value.toMutableList()
                    val currentUlList = uploadHistory.value.toMutableList()

                    currentDlList.add(dlKbps)
                    currentUlList.add(ulKbps)

                    val maxHistoryPoints = 50
                    if (currentDlList.size > maxHistoryPoints) currentDlList.removeAt(0)
                    if (currentUlList.size > maxHistoryPoints) currentUlList.removeAt(0)

                    // Normalize independently for download and upload charts so each scales to full height nicely
                    val maxDl = maxOf(10f, currentDlList.maxOrNull() ?: 10f)
                    val maxUl = maxOf(10f, currentUlList.maxOrNull() ?: 10f)

                    downloadHistory.value = currentDlList.map { (it / maxDl * 100f).coerceIn(0f, 100f) }
                    uploadHistory.value = currentUlList.map { (it / maxUl * 100f).coerceIn(0f, 100f) }
                }
            }
            delay(refreshInterval.milliseconds)
        }
    }

    val networkUtils = remember { NetworkUtils(context) }

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
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalItemSpacing = 16.dp
    ) {
        staggeredHeader {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Card(
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = refreshInterval == 500L,
                            onClick = { refreshInterval = 500L },
                            label = { Text("0.5s") }
                        )
                        FilterChip(
                            selected = refreshInterval == 1000L,
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
        // Download Speed Card
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
                                imageVector = ImageVector.vectorResource(R.drawable.downloading_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.download_speed),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = networkUtils.formatSpeed(snapshot?.downloadSpeedBps ?: 0L),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${stringResource(R.string.total_downloaded)}: ${networkUtils.formatBytes(snapshot?.totalRxBytes ?: 0L)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LiveChart(
                        dataPoints = downloadHistory.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        lineColor = MaterialTheme.colorScheme.primary,
                        fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // Upload Speed Card
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
                                imageVector = ImageVector.vectorResource(R.drawable.arrow_upload_progress_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.upload_speed),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = networkUtils.formatSpeed(snapshot?.uploadSpeedBps ?: 0L),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${stringResource(R.string.total_uploaded)}: ${networkUtils.formatBytes(snapshot?.totalTxBytes ?: 0L)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

      
                  LiveChart(
                        dataPoints = uploadHistory.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        lineColor = MaterialTheme.colorScheme.secondary,
                        fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}
