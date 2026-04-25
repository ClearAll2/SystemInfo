package com.lkonlesoft.displayinfo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.`object`.AboutItem

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    val uriHandler = LocalUriHandler.current
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
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.app_info))
                appInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    IndividualLine(
                        title = stringResource(id = item.title),
                        info = stringResource(id = item.text),
                        onClick = {
                            uriHandler.openUri(url)
                        },
                        isLast = appInfoItems.last() == item,
                        topStart = if (appInfoItems.first() == item) 20.dp else 5.dp,
                        topEnd = if (appInfoItems.first() == item) 20.dp else 5.dp,
                        bottomStart = if (appInfoItems.last() == item) 20.dp else 5.dp,
                        bottomEnd = if (appInfoItems.last() == item) 20.dp else 5.dp
                    )
                }
            }
        }
        item {
            Column {
                HeaderLine(tittle = stringResource(R.string.legal_info))
                legalInfoItems.forEach { item ->
                    val url = stringResource(id = item.url)
                    IndividualLine(
                        title = stringResource(id = item.title),
                        info = stringResource(id = item.text),
                        onClick = {
                            uriHandler.openUri(url)
                        },
                        isLast = legalInfoItems.last() == item,
                        topStart = if (legalInfoItems.first() == item) 20.dp else 5.dp,
                        topEnd = if (legalInfoItems.first() == item) 20.dp else 5.dp,
                        bottomStart = if (legalInfoItems.last() == item) 20.dp else 5.dp,
                        bottomEnd = if (legalInfoItems.last() == item) 20.dp else 5.dp
                    )
                }
            }
        }
    }
}