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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.MediaUtils
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MediaDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val mediaFeature = MediaUtils(context).getAudioFeatures()
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.media),
                icon = R.drawable.slideshow_24px
            )
            Spacer(modifier = Modifier.height(8.dp))
            mediaFeature.forEach {
                GeneralStatRow(
                    label = stringResource(it.name),
                    value = it.value.toString()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaScreen(longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var isLoading by remember { mutableStateOf(true) }
    val mediaFeature = MediaUtils(context).getAudioFeatures()
    val mediaCodecs = MediaUtils(context).getMediaCodecs()
    var widevineInfo by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var clearKeyInfo by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            widevineInfo = MediaUtils(context).getWidevineInfo()
            clearKeyInfo = MediaUtils(context).getClearKeyInfo()
            isLoading = false
        }
    }
    AnimatedContent(targetState = isLoading,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { loading ->
        if (loading) {
            Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer).fillMaxSize(), contentAlignment = Alignment.Center) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(100.dp)
                )
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
                item {
                    Column {
                        HeaderLine(tittle = stringResource(R.string.features))
                        mediaFeature.forEach {
                            IndividualLine(title = stringResource(it.name),
                                info = it.value.toString(),
                                canLongPress = longPressCopy,
                                copyTitle = copyTitle,
                                isLast = mediaFeature.last() == it,
                                topStart = if (mediaFeature.first() == it) 20.dp else 5.dp,
                                topEnd = if (mediaFeature.first() == it) 20.dp else 5.dp,
                                bottomStart = if (mediaFeature.last() == it) 20.dp else 5.dp,
                                bottomEnd = if (mediaFeature.last() == it) 20.dp else 5.dp
                            )
                        }
                    }
                }
                item {
                    Column {
                        HeaderLine(tittle = stringResource(R.string.codecs))
                        mediaCodecs.forEach {
                            IndividualLine(title = stringResource(it.name),
                                info = it.value.toString(),
                                canLongPress = longPressCopy,
                                copyTitle = copyTitle,
                                isLast = mediaCodecs.last() == it,
                                topStart = if (mediaCodecs.first() == it) 20.dp else 5.dp,
                                topEnd = if (mediaCodecs.first() == it) 20.dp else 5.dp,
                                bottomStart = if (mediaCodecs.last() == it) 20.dp else 5.dp,
                                bottomEnd = if (mediaCodecs.last() == it) 20.dp else 5.dp
                            )
                        }
                    }
                }
                item {
                    Column {
                        HeaderLine(tittle = stringResource(R.string.widevine))
                        val widevineList = widevineInfo.toList()
                        widevineList.forEach {
                            IndividualLine(title = stringResource(it.name),
                                info = it.value.toString(),
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
                            IndividualLine(title = stringResource(it.name),
                                info = it.value.toString(),
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
                staggeredHeader {
                    Spacer(modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}