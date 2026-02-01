package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.DisplayUtils
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun DisplayDashboard(intervalMillis: Long = 1000L,onClick: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val infoList by remember(refreshKey) { mutableStateOf(DisplayUtils(context, resources).getDashboardData()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            refreshKey++
        }
    }

    OutlinedCard(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(title = stringResource(R.string.display), icon = R.drawable.outline_smartphone_24)
            Spacer(modifier = Modifier.height(12.dp))

            infoList.forEach {
                GeneralStatRow(label = stringResource(it.name), value = it.value.toString() + it.extra)
            }
        }
    }
}

@Composable
fun DisplayScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var widevineInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var clearKeyInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var infoList by remember(refreshKey) { mutableStateOf(DisplayUtils(context, resources).getAllData()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            widevineInfo = DisplayUtils(context, resources).getWidevineInfo()
            clearKeyInfo = DisplayUtils(context, resources).getClearKeyInfo()
        }
        while (true){
            delay(1000L)
            refreshKey++
        }
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
                HeaderLine(tittle = stringResource(R.string.display))
                infoList.forEach {
                    IndividualLine(tittle = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = infoList.last() == it,
                        topStart = if (infoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (infoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (infoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (infoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.widevine))
                val widevineList = widevineInfo.toList()
                widevineList.forEach {
                    IndividualLine(tittle = it.first.replaceFirstChar { c -> c.uppercase() },
                        info = it.second,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = widevineList.last() == it,
                        topStart = if (widevineList.first() == it) 20.dp else 5.dp,
                        topEnd = if (widevineList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (widevineList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (widevineList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.clearkey))
                val clearKeyList = clearKeyInfo.toList()
                clearKeyList.forEach {
                    IndividualLine(tittle = it.first.replaceFirstChar { c -> c.uppercase() },
                        info = it.second,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = clearKeyList.last() == it,
                        topStart = if (clearKeyList.first() == it) 20.dp else 5.dp,
                        topEnd = if (clearKeyList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (clearKeyList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (clearKeyList.last() == it) 20.dp else 5.dp
                    )
                }

            }
        }
        if (showNotice){
            item {
                GeneralWarning(
                    title = R.string.drm_notice_title,
                    text = R.string.drm_notice,
                )
            }
        }
    }
}