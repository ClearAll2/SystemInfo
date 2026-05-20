package com.lkonlesoft.displayinfo.view.module

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.lkonlesoft.displayinfo.view.GeneralStatRow
import com.lkonlesoft.displayinfo.view.HeaderForDashboard
import com.lkonlesoft.displayinfo.view.HeaderLine
import com.lkonlesoft.displayinfo.view.IndividualLine
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.delay

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderForDashboard(
                title = stringResource(R.string.display),
                icon = R.drawable.mobile_text_24px
            )
            Spacer(modifier = Modifier.height(12.dp))

            infoList.forEach {
                GeneralStatRow(
                    label = stringResource(it.name),
                    value = it.value.toString() + it.extra
                )
            }
        }
    }
}

@Composable
fun DisplayScreen(longPressCopy: Boolean, copyTitle: Boolean, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var infoList by remember(refreshKey) { mutableStateOf(DisplayUtils(context, resources).getAllDisplayDetails()) }
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            refreshKey++
        }
    }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(320.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = paddingValues.calculateTopPadding())
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(infoList) { index, display ->
            Column {
                HeaderLine(tittle = stringResource(R.string.display) + " #${index+1}")
                display.forEach {
                    IndividualLine(title = stringResource(it.name),
                        info = it.value.toString() + it.extra,
                        canLongPress = longPressCopy,
                        copyTitle = copyTitle,
                        isLast = display.last() == it,
                        topStart = if (display.first() == it) 20.dp else 5.dp,
                        topEnd = if (display.first() == it) 20.dp else 5.dp,
                        bottomStart = if (display.last() == it) 20.dp else 5.dp,
                        bottomEnd = if (display.last() == it) 20.dp else 5.dp
                    )
                }
            }
        }

        staggeredHeader {
            Spacer(modifier = Modifier.padding(20.dp))
        }
    }
}