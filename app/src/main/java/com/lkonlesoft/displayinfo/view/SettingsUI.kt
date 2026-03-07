package com.lkonlesoft.displayinfo.view

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.AppTheme
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    longPressCopy: Boolean,
    typographyType: Int,
    copyTitle: Boolean,
    showNotice: Boolean,
    appColor: Int,
    isDynamicColors: Boolean,
    settings: SettingsViewModel,
    onAboutClick: () -> Unit,
    paddingValues: PaddingValues
) {
    var showLangDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var currentLang by remember { mutableStateOf("") }
    val localeOptions = remember {
        mapOf(
            "default" to R.string.system_default,
            "vi" to R.string.vi,
            "ru" to R.string.ru,
            "zh" to R.string.zh,
            "ja" to R.string.ja,
            "ko" to R.string.ko,
            "en" to R.string.en,
            "fr" to R.string.fr,
            "nl" to R.string.nl,
            "de" to R.string.de,
            "it" to R.string.it,
            "pt" to R.string.pt,
            "es" to R.string.es
        )
    }
    LaunchedEffect(Unit) {
        currentLang = getCurrentLanguage()
    }
    AnimatedVisibility(
        visible = showLangDialog,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        LanguageSelectionPopup(
            modifier = Modifier.fillMaxWidth(),
            localeOptions = localeOptions,
            currentLang = currentLang,
            onDismiss = {
                showLangDialog = !showLangDialog
            },
            onClick = { newLang ->
                if (currentLang != newLang) {
                    currentLang = newLang
                    if (newLang == "default")
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    else
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                currentLang
                            )
                        )
                }
            }
        )
    }
    AnimatedVisibility(
        visible = showFontDialog,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 100)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220, delayMillis = 100)
        ),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        FontSelectionPopup(
            modifier = Modifier.fillMaxWidth(),
            currentFont = typographyType,
            onDismiss = {
                showFontDialog = !showFontDialog
            },
            onClick = { newFont ->
                settings.setTypographyType(newFont)
            }
        )
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.language))
                IndividualLine(
                    title = stringResource(
                        localeOptions.entries.firstOrNull { it.key == currentLang }?.value
                            ?: R.string.system_default
                    ),
                    info = currentLang,
                    onClick = {
                        showLangDialog = !showLangDialog
                    },
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.appearance))
                ThemeSelector(
                    selectedTheme = appColor,
                    onThemeSelected = {
                        settings.setAppColor(it)
                    },
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                )
                IndividualLine(
                    title = stringResource(R.string.font),
                    info = if (typographyType == 1) stringResource(R.string.system_default)
                        else stringResource(R.string.gg_sf_font),
                    onClick = {
                        showFontDialog = !showFontDialog
                    }
                )
                CommonSwitchOption(
                    text = R.string.material_you,
                    subText = R.string.material_you_details,
                    extra = "",
                    checked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isDynamicColors else false,
                    onClick = {
                        settings.setUseDynamicColors(!isDynamicColors)
                    },
                    onSwitch = {
                        settings.setUseDynamicColors(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp,
                    isLast = false,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    clickable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                )
                CommonSwitchOption(
                    text = R.string.show_warning_notice,
                    subText = R.string.show_warning_notice_details,
                    extra = "",
                    checked = showNotice,
                    onClick = {
                        settings.setShowNotice(!showNotice)
                    },
                    onSwitch = {
                        settings.setShowNotice(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    isLast = true
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.general))
                CommonSwitchOption(
                    text = R.string.long_press_to_copy,
                    subText = R.string.long_press_to_copy_details,
                    extra = "",
                    checked = longPressCopy,
                    onClick = {
                        settings.setLongPressCopy(!longPressCopy)
                    },
                    onSwitch = {
                        settings.setLongPressCopy(it)
                    },
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp,
                    isLast = false
                )
                CommonSwitchOption(
                    text = R.string.copy_title,
                    subText = R.string.copy_title_details,
                    extra = "",
                    checked = copyTitle,
                    onClick = {
                        settings.setCopyTitle(!copyTitle)
                    },
                    onSwitch = {
                        settings.setCopyTitle(it)
                    },
                    topStart = 5.dp,
                    topEnd = 5.dp,
                    enabled = longPressCopy,
                    clickable = longPressCopy,
                    isLast = true
                )
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.about))
                IndividualLine(
                    title = stringResource(R.string.app_info),
                    info = stringResource(id = R.string.app_ver),
                    onClick = onAboutClick,
                    isLast = true,
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            }
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit,
    topStart: Dp = 20.dp,
    topEnd: Dp = 20.dp,
    bottomStart: Dp = 20.dp,
    bottomEnd: Dp = 20.dp,
    paddingValues: Dp = 20.dp,
    isLast: Boolean = false
) {
    val themeOptions = remember {
        mutableListOf(
            AppTheme.System,
            AppTheme.Light,
            AppTheme.Dark
        ).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                remove(AppTheme.System)
            }
        }
    }
    Column {
        Column(
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
                .padding(horizontal = paddingValues)
        ) {
            Text(
                text = stringResource(R.string.app_color),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .padding(vertical = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { theme ->
                    FilterChip(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(theme.icon),
                                contentDescription = null
                            )
                        },
                        selected = selectedTheme == theme.value,
                        onClick = { onThemeSelected(theme.value) },
                        label = { Text(stringResource(theme.title), fontSize = 14.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
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
fun LanguageSelectionPopup(
    modifier: Modifier,
    currentLang: String,
    localeOptions: Map<String, Int>,
    onClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val height = LocalWindowInfo.current.containerDpSize.height
    var selectLang by remember { mutableStateOf(currentLang) }
    val firstIndex = localeOptions.entries.toList().indexOfFirst { it.key == currentLang } -1
    val state =
        rememberLazyListState(initialFirstVisibleItemIndex = if (firstIndex >= 0) firstIndex else 0)
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 10.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.language),
                    fontSize = 25.sp,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp)
                )
                LazyColumn(
                    state = state, modifier = Modifier
                        .fillMaxWidth()
                        .height(height.times(0.4f))
                ) {
                    items(localeOptions.entries.toList()) { item ->
                        PopupSelectionLine(
                            name = buildString {
                                append(stringResource(item.value))
                                append("\n")
                                append("(${item.key})")
                            },
                            onSelected = selectLang == item.key,
                            onItemClick = {
                                selectLang = item.key
                            }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .padding(bottom = 10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    Button(
                        onClick = {
                            onClick(selectLang)
                            onDismiss()
                        },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.OK),
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FontSelectionPopup(
    modifier: Modifier,
    currentFont: Int,
    onClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectFont by remember { mutableIntStateOf(currentFont) }
    val listFont = remember {
        listOf(
            R.string.gg_sf_font,
            R.string.system_default
        )
    }
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 10.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.font),
                    fontSize = 25.sp,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp)
                )
                listFont.forEachIndexed { index, i ->
                    PopupSelectionLine(
                        name = stringResource(i),
                        onSelected = selectFont == index,
                        onItemClick = {
                            selectFont = index
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .padding(bottom = 10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    Button(
                        onClick = {
                            onClick(selectFont)
                            onDismiss()
                        },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.OK),
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}