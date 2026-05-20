package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.utils.CameraUtils
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.GeneralWarning
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader


@Composable
fun CameraDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val cameraCount = CameraUtils(context).getCameraCount()
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
                title = stringResource(R.string.camera),
                icon = R.drawable.camera_24px
            )
            Spacer(modifier = Modifier.height(8.dp))
            GeneralStatRow(
                label = stringResource(cameraCount.name),
                value = cameraCount.value.toString()
            )
        }
    }
}

@Composable
fun CameraInfoScreen(paddingValues: PaddingValues, longPressCopy: Boolean, copyTitle: Boolean, showNotice: Boolean) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var cameraInfoList by remember { mutableStateOf<List<List<DeviceInfo>>>(emptyList()) }
    LaunchedEffect(Unit) {
        cameraInfoList = CameraUtils(context).getAllData()
    }

    // Display the list of camera details.
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
        itemsIndexed(cameraInfoList) { index, cameraItemList ->
            Column {
                HeaderLine(tittle = "#${index+1}")
                cameraItemList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = cameraItemList.last() == it,
                        topStart = if (cameraItemList.first() == it) 20.dp else 5.dp,
                        topEnd = if (cameraItemList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (cameraItemList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (cameraItemList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        if (showNotice) {
            staggeredHeader {
                GeneralWarning(
                    title = R.string.camera_notice_title,
                    text = R.string.camera_notice
                )
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}