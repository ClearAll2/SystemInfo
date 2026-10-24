package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lkonlesoft.displayinfo.`object`.NavigationItem
import com.lkonlesoft.displayinfo.view.BigTile

@Composable
fun MonitorsScreen(navController: NavHostController, currentRoute: String?, paddingValues: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    val width = LocalWindowInfo.current.containerDpSize.width
    val state = rememberLazyStaggeredGridState()
    val listTestScreen = remember {
        listOf(
            NavigationItem.CPUMonitor,
            NavigationItem.NetworkMonitor
        )
    }
    LazyVerticalStaggeredGrid(
        state = state,
        columns = if (width < 600.dp) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Adaptive(
            240.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection)
            )
            .padding(horizontal = 20.dp)
            .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(listTestScreen) { item ->
            val isSelected = currentRoute == item.route
            BigTile(title = stringResource(item.name), icon = item.icon)
            {
                if (!isSelected)
                    navController.navigate(item.route)
            }
        }
    }
}