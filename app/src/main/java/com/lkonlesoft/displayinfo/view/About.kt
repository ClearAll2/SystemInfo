package com.lkonlesoft.displayinfo.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.AboutItem

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    val uriHandler = LocalUriHandler.current
    val layoutDirection = LocalLayoutDirection.current
    val appInfoItems = remember {
        listOf(
            AboutItem.AppVer,
            AboutItem.Rate,
            AboutItem.More,
            AboutItem.Contact,

            )
    }
    val legalInfoItems = remember {
        listOf(
            AboutItem.Privacy,
            AboutItem.Terms
        )
    }
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
                HeaderLine(title = stringResource(R.string.app_info))
                appInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    IndividualLine(
                        title = stringResource(id = item.title),
                        info = stringResource(id = item.text),
                        onClick = {
                            uriHandler.openUri(url)
                        },
                        isFirst = appInfoItems.first() == item,
                        isLast = appInfoItems.last() == item
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(title = stringResource(R.string.legal_info))
                legalInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    IndividualLine(
                        title = stringResource(id = item.title),
                        info = stringResource(id = item.text),
                        onClick = {
                            uriHandler.openUri(url)
                        },
                        isFirst = legalInfoItems.first() == item,
                        isLast = legalInfoItems.last() == item
                    )
                }
            }
        }
        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}