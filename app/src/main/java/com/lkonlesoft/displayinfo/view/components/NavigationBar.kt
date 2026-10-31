package com.lkonlesoft.displayinfo.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials


@Composable
fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    currentRoute: String?,
    onRouteSelected: (String) -> Unit
) {
    val navItems = remember {
        buildList {
            add(NavigationItem.Home)
            add(NavigationItem.Test)
            add(NavigationItem.Monitors)
        }
    }
    val selectedIndex by remember(currentRoute, navItems) {
        derivedStateOf {
            navItems.indexOfFirst { it.route == currentRoute }
        }
    }
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var activeHoverIndex by remember { mutableIntStateOf(selectedIndex) }
    val itemWidth = 90.dp
    val showLabels = LocalWindowInfo.current.containerDpSize.width >= 360.dp
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0)
            activeHoverIndex = selectedIndex
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(32.dp))
                .hazeBlur(
                    input = HazeInput.Backdrop(hazeState),
                    style = HazeMaterials.thin(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .pointerInput(navItems, selectedIndex, itemWidth) {
                        val totalItems = navItems.size
                        if (totalItems == 0) return@pointerInput

                        val itemWidthPx = itemWidth.toPx()

                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragOffsetPx = 0f
                                val startIndex = (offset.x / itemWidthPx)
                                    .toInt()
                                    .coerceIn(0, totalItems - 1)
                                activeHoverIndex = startIndex
                            },
                            onDragEnd = {
                                val basePx = selectedIndex * itemWidthPx
                                val finalPx = (basePx + dragOffsetPx).coerceIn(
                                    0f,
                                    (totalItems - 1) * itemWidthPx
                                )
                                val finalIndex = ((finalPx + itemWidthPx / 2f) / itemWidthPx)
                                    .toInt()
                                    .coerceIn(0, totalItems - 1)

                                isDragging = false
                                dragOffsetPx = 0f
                                if (finalIndex != selectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    onRouteSelected(navItems[finalIndex].route)
                                } else {
                                    activeHoverIndex = selectedIndex
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                dragOffsetPx = 0f
                                activeHoverIndex = selectedIndex
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetPx += dragAmount
                                val basePx = selectedIndex * itemWidthPx
                                val currentPx = (basePx + dragOffsetPx).coerceIn(
                                    0f,
                                    (totalItems - 1) * itemWidthPx
                                )
                                val hoverIndex = ((currentPx + itemWidthPx / 2f) / itemWidthPx)
                                    .toInt()
                                    .coerceIn(0, totalItems - 1)

                                if (hoverIndex != activeHoverIndex) {
                                    activeHoverIndex = hoverIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        )
                    }
            ) {
                val totalItems = navItems.size
                val density = LocalDensity.current
                val targetPillOffsetDp = if (isDragging) {
                    val baseDp = itemWidth * selectedIndex
                    val dragDp = with(density) { dragOffsetPx.toDp() }
                    (baseDp + dragDp).coerceIn(0.dp, itemWidth * (totalItems - 1))
                } else {
                    itemWidth * selectedIndex
                }
                val animatedPillOffset by animateDpAsState(
                    targetValue = targetPillOffsetDp,
                    animationSpec = if (isDragging) {
                        spring(stiffness = Spring.StiffnessHigh)
                    } else {
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    label = "pillOffset"
                )
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedPillOffset.roundToPx(), 0) }
                        .width(itemWidth)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
                Row(
                    modifier = Modifier.height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isHighlighted =
                            if (isDragging || selectedIndex == -1) index == activeHoverIndex else index == selectedIndex

                        val iconColor by animateColorAsState(
                            targetValue = if (isHighlighted) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(180),
                            label = "iconColor"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isHighlighted) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(180),
                            label = "textColor"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (isHighlighted) 1.1f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "iconScale"
                        )

                        Column(
                            modifier = Modifier
                                .width(itemWidth)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (index != selectedIndex) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onRouteSelected(item.route)
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = if (isHighlighted) item.selectedIcon else item.icon
                                ),
                                contentDescription = stringResource(id = item.name),
                                tint = iconColor,
                                modifier = Modifier.scale(iconScale)
                            )

                            if (showLabels) {
                                Text(
                                    text = stringResource(id = item.name),
                                    color = textColor,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.nonScaledSp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}