package com.lkonlesoft.displayinfo.view.test.touch

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkonlesoft.displayinfo.R

@Composable
fun TouchTestHud(
    isPinned: Boolean,
    mode: TouchTestMode,
    density: GridDensity,
    isHapticsEnabled: Boolean,
    onModeChange: (TouchTestMode) -> Unit,
    onDensityChange: (GridDensity) -> Unit,
    onToggleHaptics: () -> Unit,
    onTogglePin: () -> Unit,
    onClear: () -> Unit,
    onExit: () -> Unit
) {
    val width = LocalWindowInfo.current.containerDpSize.width
    Card(
        modifier = Modifier
            .fillMaxWidth(if (width < 600.dp) 1f else .5f)
            .padding(bottom = 48.dp)
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Title, Pin Button, Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onExit,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(id = R.string.close))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Haptics Toggle
                    IconButton(
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        onClick = onToggleHaptics) {
                        Icon(
                            painter = painterResource(id = R.drawable.mobile_vibrate_24px),
                            contentDescription = stringResource(id = R.string.touch_test_haptics),
                            tint = if (isHapticsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Pin Toggle
                    IconButton(
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        onClick = onTogglePin
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPinned) R.drawable.keep_24px else R.drawable.keep_off_24px
                            ),
                            contentDescription = "Pin HUD",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear Button
                    Button(
                        onClick = onClear,
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(text = stringResource(id = R.string.touch_test_clear))
                    }
                }
            }

            // Mode Selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = mode == TouchTestMode.TRAILS_AND_INFO,
                    onClick = { onModeChange(TouchTestMode.TRAILS_AND_INFO) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(text = stringResource(id = R.string.touch_test_mode_trail), fontSize = 12.sp)
                }

                SegmentedButton(
                    selected = mode == TouchTestMode.GRID_COVERAGE,
                    onClick = { onModeChange(TouchTestMode.GRID_COVERAGE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(text = stringResource(id = R.string.touch_test_mode_grid), fontSize = 12.sp)
                }
            }

            // Grid Density Selector (Only visible in GRID_COVERAGE mode)
            if (mode == TouchTestMode.GRID_COVERAGE) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridDensity.entries.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = density == item,
                            onClick = { onDensityChange(item) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = GridDensity.entries.size)
                        ) {
                            Text(text = item.label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
