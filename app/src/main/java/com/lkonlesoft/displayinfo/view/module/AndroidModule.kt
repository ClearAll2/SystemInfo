package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.utils.AndroidUtils
import com.lkonlesoft.displayinfo.view.CardDashboard
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader

@Composable
fun AndroidDashboard(onClick: () -> Unit) {
    val context = LocalContext.current
    val listInfo = remember { AndroidUtils(context).getDashboardData() }
    CardDashboard(
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.android),
                icon = R.drawable.outline_android_24
            )
            Spacer(modifier = Modifier.height(8.dp))
            listInfo.forEach {
                GeneralStatRow(
                    label = stringResource(it.name),
                    value = it.value.toString() + it.extra
                )
            }
        }
    }
}

@Composable
fun AndroidScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val androidInfoList = remember {
        AndroidUtils(context).getAndroidInfo()
    }
    val extraInfoList = remember {
        AndroidUtils(context).getExtraInfo()
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection))
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(title = stringResource(R.string.general))
                androidInfoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = androidInfoList.last() == it,
                        topStart = if (androidInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (androidInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (androidInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (androidInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(title = stringResource(R.string.extra))
                extraInfoList.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString(),
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = extraInfoList.last() == it,
                        topStart = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        topEnd = if (extraInfoList.first() == it) 20.dp else 5.dp,
                        bottomStart = if (extraInfoList.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (extraInfoList.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}