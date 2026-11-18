package com.lkonlesoft.displayinfo.view.test

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.copyTextToClipboard
import com.lkonlesoft.displayinfo.helper.hasPermission
import com.lkonlesoft.displayinfo.view.components.ConfirmActionPopup
import com.lkonlesoft.displayinfo.view.components.staggeredHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class GpsLogEvent(
    val time: String,
    val text: String,
    val isFix: Boolean,
)

data class GnssConstellationInfo(
    val type: Int,
    val name: String,
    val bands: List<String>,
    val usedInFixCount: Int,
    val totalCount: Int,
)

private class MutableConstellationData(val type: Int) {
    var usedInFixCount: Int = 0
    var totalCount: Int = 0
    val bands: MutableSet<String> = mutableSetOf()
}

@Composable
fun GpsTestScreen(
    paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberLazyStaggeredGridState()
    val hasGpsHardware = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var permissionGranted by remember {
        mutableStateOf(
            context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }
    var isGpsEnabled by remember {
        mutableStateOf(
            try {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (_: Exception) {
                false
            }
        )
    }
    var showWarningPopup by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var satellitesInView by remember { mutableIntStateOf(0) }
    var satellitesUsedInFix by remember { mutableIntStateOf(0) }
    var constellationsList by remember { mutableStateOf<List<GnssConstellationInfo>>(emptyList()) }
    val logs = remember { mutableStateListOf<GpsLogEvent>() }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        permissionGranted = granted
        val time = timeFormat.format(Date())
        if (granted) {
            Toast.makeText(context, resources.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            logs.add(0, GpsLogEvent(time = time, text = resources.getString(R.string.permission_granted), isFix = true))
        } else {
            Toast.makeText(context, resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            logs.add(0, GpsLogEvent(time = time, text = resources.getString(R.string.permission_denied), isFix = false))
            showWarningPopup = true
        }
    }
    val startLocationActivity = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionGranted = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        isGpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }
    val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    DisposableEffect(permissionGranted, isGpsEnabled) {
        if (!permissionGranted || !isGpsEnabled) return@DisposableEffect onDispose {}

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
                val time = timeFormat.format(Date(location.time))
                val logText = String.format(
                    Locale.US,
                    "%.5f, %.5f (±%.1fm)",
                    location.latitude,
                    location.longitude,
                    location.accuracy
                )
                if (logs.size >= 50) logs.removeAt(logs.size - 1)
                logs.add(0, GpsLogEvent(time = time, text = logText, isFix = true))
            }

            override fun onProviderEnabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    isGpsEnabled = true
                }
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    isGpsEnabled = false
                }
            }
        }
        val gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val count = status.satelliteCount
                var usedCount = 0
                val constellationMap = mutableMapOf<Int, MutableConstellationData>()

                for (i in 0 until count) {
                    val type = status.getConstellationType(i)
                    val used = status.usedInFix(i)
                    if (used) usedCount++

                    val data = constellationMap.getOrPut(type) { MutableConstellationData(type) }
                    data.totalCount++
                    if (used) data.usedInFixCount++

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && status.hasCarrierFrequencyHz(i)) {
                        val freqHz = status.getCarrierFrequencyHz(i)
                        val band = getCarrierBandName(type, freqHz)
                        if (band != null) {
                            data.bands.add(band)
                        }
                    }
                }
                satellitesInView = count
                satellitesUsedInFix = usedCount

                constellationsList = constellationMap.values.map { data ->
                    GnssConstellationInfo(
                        type = data.type,
                        name = getConstellationDisplayName(data.type),
                        bands = sortBands(data.bands),
                        usedInFixCount = data.usedInFixCount,
                        totalCount = data.totalCount,
                    )
                }.sortedBy { it.type }
            }
        }
        try {
            locationManager.registerGnssStatusCallback(gnssCallback, null)
        } catch (_: SecurityException) {}

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    locationListener
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    0f,
                    locationListener
                )
            }
        } catch (_: SecurityException) {}

        onDispose {
            try {
                locationManager.removeUpdates(locationListener)
                locationManager.unregisterGnssStatusCallback(gnssCallback)
            } catch (_: Exception) {}
        }
    }
    AnimatedVisibility(visible = showWarningPopup,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        ConfirmActionPopup(
            content = {},
            mainText = stringResource(id = R.string.permission_denied),
            subText = stringResource(id = R.string.permission_denied_details),
            confirmText = stringResource(id = R.string.settings),
            cancelText = stringResource(id = R.string.cancel),
            onDismiss = {
                showWarningPopup = false
            },
            onClick = {
                showWarningPopup = false
                startLocationActivity.launch(appSettingsIntent)
            }
        )
    }
    if (!hasGpsHardware) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text(
                text = stringResource(id = R.string.gps_not_available),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyVerticalStaggeredGrid(
            state = state,
            columns = StaggeredGridCells.Adaptive(300.dp),
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
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            staggeredHeader {
                val hasFix = currentLocation != null
                val animatedHeaderColor by animateColorAsState(
                    targetValue = when {
                        !permissionGranted || !isGpsEnabled -> MaterialTheme.colorScheme.errorContainer
                        hasFix -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    animationSpec = tween(500),
                    label = "headerColor"
                )

                val animatedIconColor by animateColorAsState(
                    targetValue = when {
                        !permissionGranted || !isGpsEnabled -> MaterialTheme.colorScheme.error
                        hasFix -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    animationSpec = tween(500),
                    label = "iconColor"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = CircleShape,
                        color = animatedHeaderColor,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.location_on),
                                contentDescription = stringResource(id = R.string.gps_test_title),
                                tint = animatedIconColor,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when {
                            !permissionGranted -> stringResource(id = R.string.gps_grant_permission)
                            !isGpsEnabled -> stringResource(id = R.string.gps_disabled)
                            hasFix -> stringResource(id = R.string.gps_has_fix)
                            else -> stringResource(id = R.string.gps_searching)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentLocation != null) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.6f°,  %.6f°",
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!permissionGranted) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = stringResource(id = R.string.gps_grant_permission))
                            }
                        } else if (!isGpsEnabled) {
                            Button(
                                onClick = {
                                    try {
                                        startLocationActivity.launch(locationSettingsIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, resources.getString(R.string.failure), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = stringResource(id = R.string.gps_enable_location))
                            }
                        }

                        if (currentLocation != null) {
                            OutlinedButton(
                                onClick = {
                                    val loc = currentLocation
                                    if (loc != null) {
                                        val textToCopy = "${loc.latitude}, ${loc.longitude}"
                                        context.copyTextToClipboard(textToCopy, "GPS Location")
                                        Toast.makeText(
                                            context,
                                            resources.getString(R.string.coordinates_copied),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = stringResource(id = R.string.copy_coordinates))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item {
                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.latitude) + " & " + stringResource(id = R.string.longitude),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.latitude),
                            value = currentLocation?.let { String.format(Locale.US, "%.7f°", it.latitude) } ?: "-"
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.longitude),
                            value = currentLocation?.let { String.format(Locale.US, "%.7f°", it.longitude) } ?: "-"
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.accuracy),
                            value = currentLocation?.let { String.format(Locale.US, "± %.2f m", it.accuracy) } ?: "-"
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.altitude) + " & " + stringResource(id = R.string.speed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.altitude),
                            value = currentLocation?.let { String.format(Locale.US, "%.2f m", it.altitude) } ?: "-"
                        )

                        val speedKmh = currentLocation?.let { it.speed * 3.6f }
                        GpsMetricRow(
                            label = stringResource(id = R.string.speed),
                            value = currentLocation?.let { String.format(Locale.US, "%.2f km/h (%.2f m/s)", speedKmh, it.speed) } ?: "-"
                        )

                        val bearingDegrees = currentLocation?.bearing
                        val directionStr = bearingDegrees?.let { getCardinalDirection(it) } ?: ""
                        GpsMetricRow(
                            label = stringResource(id = R.string.bearing),
                            value = currentLocation?.let { String.format(Locale.US, "%.1f° %s", it.bearing, directionStr) } ?: "-"
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.positioning_constellations),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val isDualFrequency = constellationsList.any { info ->
                            info.bands.any { band -> band == "L5" || band == "E5a" || band == "B2a" }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDualFrequency) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isDualFrequency) {
                                    stringResource(id = R.string.dual_frequency_gnss)
                                } else {
                                    stringResource(id = R.string.single_frequency_gnss)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDualFrequency) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        if (constellationsList.isNotEmpty()) {
                            constellationsList.forEach { info ->
                                val bandStr = if (info.bands.isNotEmpty()) {
                                    " (${info.bands.joinToString("+")})"
                                } else {
                                    ""
                                }
                                GpsMetricRow(
                                    label = "${info.name}$bandStr",
                                    value = "${info.usedInFixCount} / ${info.totalCount}"
                                )
                            }
                        } else {
                            GpsMetricRow(
                                label = stringResource(id = R.string.satellites_used_in_fix),
                                value = if (satellitesInView > 0 || satellitesUsedInFix > 0) {
                                    "$satellitesUsedInFix / $satellitesInView"
                                } else {
                                    "-"
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.provider) + " & " + stringResource(id = R.string.satellites),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.provider),
                            value = currentLocation?.provider?.uppercase(Locale.US) ?: "-"
                        )

                        GpsMetricRow(
                            label = stringResource(id = R.string.satellites_used_in_fix),
                            value = if (satellitesInView > 0 || satellitesUsedInFix > 0) {
                                "$satellitesUsedInFix / $satellitesInView"
                            } else {
                                "-"
                            }
                        )
                    }
                }
            }

            if (logs.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.event_log),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            logs.take(10).forEach { (time, text, isFix) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isFix) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GpsMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getCardinalDirection(bearing: Float): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    val index = ((bearing % 360) / 45.0).roundToInt()
    return directions[index]
}

private fun getConstellationDisplayName(type: Int): String {
    return when (type) {
        GnssStatus.CONSTELLATION_GPS -> "GPS"
        GnssStatus.CONSTELLATION_BEIDOU -> "BDS"
        GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
        GnssStatus.CONSTELLATION_QZSS -> "QZSS"
        GnssStatus.CONSTELLATION_IRNSS -> "NavIC"
        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
        GnssStatus.CONSTELLATION_SBAS -> "SBAS"
        else -> "GNSS"
    }
}

private fun getCarrierBandName(constellationType: Int, carrierFrequencyHz: Float): String? {
    val freqMHz = carrierFrequencyHz / 1_000_000f
    return when (constellationType) {
        GnssStatus.CONSTELLATION_GPS -> when (freqMHz) {
            in 1559.0f..1591.0f -> "L1"
            in 1164.0f..1189.0f -> "L5"
            in 1215.0f..1240.0f -> "L2"
            else -> null
        }
        GnssStatus.CONSTELLATION_BEIDOU -> when (freqMHz) {
            in 1555.0f..1567.0f -> "B1I"
            in 1570.0f..1580.0f -> "B1c"
            in 1164.0f..1189.0f -> "B2a"
            in 1195.0f..1219.0f -> "B2b"
            in 1255.0f..1280.0f -> "B3I"
            else -> null
        }
        GnssStatus.CONSTELLATION_GALILEO -> when (freqMHz) {
            in 1559.0f..1591.0f -> "E1"
            in 1164.0f..1189.0f -> "E5a"
            in 1195.0f..1219.0f -> "E5b"
            in 1260.0f..1300.0f -> "E6"
            else -> null
        }
        GnssStatus.CONSTELLATION_QZSS -> when (freqMHz) {
            in 1559.0f..1591.0f -> "L1"
            in 1164.0f..1189.0f -> "L5"
            in 1215.0f..1240.0f -> "L2"
            in 1260.0f..1300.0f -> "L6"
            else -> null
        }
        GnssStatus.CONSTELLATION_IRNSS -> when (freqMHz) {
            in 1164.0f..1189.0f -> "L5"
            in 2480.0f..2500.0f -> "S"
            else -> null
        }
        GnssStatus.CONSTELLATION_GLONASS -> when (freqMHz) {
            in 1598.0f..1606.0f -> "L1"
            in 1242.0f..1249.0f -> "L2"
            in 1200.0f..1205.0f -> "L3"
            else -> null
        }
        GnssStatus.CONSTELLATION_SBAS -> when (freqMHz) {
            in 1559.0f..1591.0f -> "L1"
            in 1164.0f..1189.0f -> "L5"
            else -> null
        }
        else -> null
    }
}

private fun sortBands(bands: Set<String>): List<String> {
    val order = listOf("L1", "B1I", "B1c", "E1", "L5", "B2a", "E5a", "E5b", "B2b", "L2", "L6", "E6", "B3I", "L3", "S")
    return bands.sortedWith(Comparator { a, b ->
        val idxA = order.indexOf(a).let { if (it == -1) 999 else it }
        val idxB = order.indexOf(b).let { if (it == -1) 999 else it }
        idxA.compareTo(idxB)
    })
}
