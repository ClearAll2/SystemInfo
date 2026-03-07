package com.lkonlesoft.displayinfo.view

import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.copyTextToClipboard
import com.lkonlesoft.displayinfo.helper.getBatteryLevelColor
import com.lkonlesoft.displayinfo.helper.getMemoryLevelColor
import com.lkonlesoft.displayinfo.helper.toBitmap

@Composable
fun BigTitle(title: String, icon: Int, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .padding(horizontal = 7.5.dp, vertical = 7.5.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.padding(10.dp))
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = title,
                modifier = Modifier
                    .padding(10.dp)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = title,
                fontSize = 25.sp,
                modifier = Modifier.padding(10.dp)
            )

        }
        Spacer(modifier = Modifier.padding(10.dp))
    }
}


@Composable
fun IndividualLine(
    title: String,
    info: String,
    icon: Drawable? = null,
    onClick: () -> Unit = { },
    canLongPress: Boolean = true,
    copyTitle: Boolean = true,
    topStart: Dp = 5.dp,
    topEnd: Dp = 5.dp,
    bottomStart: Dp = 5.dp,
    bottomEnd: Dp = 5.dp,
    isLast: Boolean = false,
    dividerColor: Color = MaterialTheme.colorScheme.background,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
){
    val context = LocalContext.current
    val resource = LocalResources.current
    val isNotExpandable by remember { mutableStateOf(info.length < 120) }
    var expanded by rememberSaveable { mutableStateOf(isNotExpandable) }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .combinedClickable(
                    onClick = {
                        if (isNotExpandable)
                            onClick()
                        else
                            expanded = !expanded
                    },
                    onLongClick = {
                        if (canLongPress) {
                            if (copyTitle) {
                                context.copyTextToClipboard(buildString {
                                    append(title)
                                    append("\n")
                                    append(info)
                                })
                            } else {
                                context.copyTextToClipboard(info)
                            }
                            Toast.makeText(
                                context,
                                resource.getString(R.string.copied_to_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (icon != null) {
                Image(
                    painter = BitmapPainter(icon.toBitmap().asImageBitmap()),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).padding(end = 20.dp)
                )
            }
            Column {
                Text(
                    text = if (icon == null) title.split(" ")
                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                    else title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
                AnimatedContent(
                    targetState = expanded,
                    transitionSpec = {
                        expandVertically(expandFrom = Alignment.Top) + fadeIn() togetherWith shrinkVertically(
                            shrinkTowards = Alignment.Top
                        ) + fadeOut()
                    }
                ) {
                    if (it) {
                        Text(
                            text = info,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    } else {
                        Text(
                            text = info.take(60) + "...",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = dividerColor
            )
        }
    }
}

@Composable
fun HeaderLine(tittle: String, horizontalPadding: Dp = 10.dp, verticalPadding: Dp = 10.dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = tittle,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

fun getCurrentLanguage(): String {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    return if (!currentLocales.isEmpty) {
        currentLocales[0]?.language
            ?: "default"
    }
    else {
        "default"
    }
}

@Composable
fun PopupSelectionLine(name: String, onSelected: Boolean, onItemClick: () -> Unit) {
    // Animate scale with keyframes for a bouncy effect
    val scale by animateFloatAsState(
        targetValue = 1.0f, // Always return to 1.0f
        animationSpec = if (onSelected) {
            keyframes {
                durationMillis = 400 // Faster animation (400ms total)
                1.0f at 0 // Start at normal scale
                1.3f at 150 // Peak scale (bouncy overshoot)
                0.9f at 300 // Slight undershoot for bounce
                1.0f at 400 // Settle back to normal
            }
        } else {
            keyframes {
                durationMillis = 400 // Fast animation (400ms)
                1.0f at 0 // Start at normal scale
                0.8f at 150 // Scale down for unselection
                1.4f at 300 // Slight overshoot for bounce
                1.0f at 400 // Settle back to normal
            }
        }
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (onSelected) 16.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = 0.3f,
            stiffness = 300f
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .scale(scale)
            .background(if (!onSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            .clickable {
                onItemClick()
            }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name, modifier = Modifier
                .padding(vertical = 20.dp)
        )
        AnimatedVisibility(
            visible = onSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.baseline_check_circle_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }

    }
}

@Composable
fun CommonSwitchOption(
    text: Int,
    subText: Int,
    extra: Any,
    clickable: Boolean = true,
    enabled: Boolean = true,
    separator: Boolean = false,
    checked: Boolean,
    horizontalPadding: Dp = 20.dp,
    topStart: Dp = 20.dp,
    topEnd: Dp = 20.dp,
    bottomStart: Dp = 20.dp,
    bottomEnd: Dp = 20.dp,
    isLast: Boolean = false,
    onClick: () -> Unit,
    onSwitch: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(
                        topStart = topStart,
                        topEnd = topEnd,
                        bottomStart = bottomStart,
                        bottomEnd = bottomEnd
                    )
                )
                .clickable(enabled = clickable) {
                    onClick()
                }
                .height(IntrinsicSize.Min)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = stringResource(id = text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 5.dp),
                        color = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                    )
                    if (subText != -1)
                        Text(
                            text = stringResource(id = subText, extra),
                            fontSize = 14.sp,
                            color = if (enabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                        )
                }
            }
            if (separator) {
                VerticalDivider(
                    color = Color.Gray,
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                )
            }
            Switch(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(start = 15.dp),
                enabled = enabled,
                checked = checked,
                onCheckedChange = onSwitch,
                thumbContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (checked) R.drawable.baseline_check_24 else R.drawable.baseline_close_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            )
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun GeneralWarning(
    canClick: Boolean = false,
    title: Int,
    text: Int,
    icon: Int = R.drawable.outline_comment_24,
    onClick: () -> Unit = {},
    extra: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(.5f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .clickable(canClick) { onClick() }
            .padding(vertical = 10.dp, horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(text = stringResource(title))
        }

        Text(
            text = stringResource(text),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
        extra()
    }
}

@Composable
fun ConfirmActionPopup(
    content: @Composable () -> Unit = { },
    mainText: String = stringResource(id = R.string.are_you_sure),
    subText: String = stringResource(id = R.string.n_a),
    confirmText: String = stringResource(id = R.string.yes),
    cancelText: String = stringResource(id = R.string.no),
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 10.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp)
                )
                content()
                Text(text = subText, modifier = Modifier.padding(horizontal = 30.dp))
                Spacer(modifier = Modifier.padding(15.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(5.dp)) {
                        Text(text = cancelText, modifier = Modifier.padding(5.dp))
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    TextButton(onClick = onClick, modifier = Modifier.padding(5.dp)) {
                        Text(text = confirmText, modifier = Modifier.padding(5.dp))
                    }
                }
            }
        }
    }
}

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

fun LazyStaggeredGridScope.staggeredHeader(
    content: @Composable LazyStaggeredGridItemScope.() -> Unit
) {
    item(
        span = StaggeredGridItemSpan.FullLine, // Use this to span the full width
        content = content
    )
}

@Composable
fun HeaderForDashboard(title: String, icon: Int) {
    Row (modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = ImageVector.vectorResource(icon),
            contentDescription = title,
            modifier = Modifier.size(48.dp).padding(end = 10.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GeneralStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
        Text(text = value, fontSize = 16.sp, textAlign = TextAlign.End)
    }
}

@Composable
fun GeneralProgressBar(level: Long, total: Long, type: Int = 0, height: Dp = 10.dp, horizontalPadding: Dp = 0.dp, verticalPadding: Dp = 0.dp) {
    LinearProgressIndicator(
        progress = { level.toFloat().div(total) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .height(height)
            .clip(MaterialTheme.shapes.small),
        color = if (type == 0) getBatteryLevelColor(level) else getMemoryLevelColor(((level.toDouble() / total.toDouble()) * 100).toLong())
    )
}