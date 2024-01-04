package dev.seabat.android.usbdebugswitch.compose.tutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.compose.HorizontalPagerIndicator
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TutorialScreen(
    tutorialLoadedFlow: StateFlow<Boolean>,
    bitmapsFlow: StateFlow<MutableList<Bitmap>>
) {
    val tutorialLoaded by tutorialLoadedFlow.collectAsState()
    val bitmaps by bitmapsFlow.collectAsState()

    if (tutorialLoaded) {
        TutorialContent(bitmaps)
    } else {
        LoadingComponent()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialContent(bitmaps: List<Bitmap>) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pagerState = rememberPagerState()
        HorizontalPager(
            pageCount = bitmaps.size,
            state = pagerState,
            contentPadding = PaddingValues(
//                horizontal = 32.dp,
                vertical = 16.dp
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bitmap = bitmaps[it].asImageBitmap()
                Image(
                    bitmap = bitmap,
                    "assetsImage",
                    modifier = Modifier.fillMaxHeight(0.7f)
                )
                Text(
                    text = "xxxxxxxxXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.8f)
                )
            }
        }
        HorizontalPagerIndicator(
            pageCount = bitmaps.size,
            currentPage = pagerState.currentPage,
            targetPage = pagerState.targetPage,
            currentPageOffsetFraction = pagerState.currentPageOffsetFraction
        )
    }
}

@Preview
@Composable
fun TutorialScreenPreview() {
    val context = LocalContext.current
    TutorialScreen(
        MutableStateFlow(false),
        MutableStateFlow(
            arrayListOf(
                BitmapFactory.decodeStream(
                    context.assets.open("screenshot_app_off.png")
                )
            )
        )
    )
}