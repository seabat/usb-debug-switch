package dev.seabat.android.usbdebugswitch.compose.tutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    tutorialLoadedFlow: StateFlow<Boolean>,
    bitmapsFlow: StateFlow<MutableList<Bitmap>>
) {
    val tutorialLoaded by tutorialLoadedFlow.collectAsState()
    val bitmaps by bitmapsFlow.collectAsState()

    if (tutorialLoaded) {
        Column {
            val pagerState = rememberPagerState()
            HorizontalPager(
                pageCount = bitmaps.size,
                state = pagerState,
                pageSize = object : PageSize {
                    override fun Density.calculateMainAxisPageSize(
                        availableSpace: Int,
                        pageSpacing: Int
                    ): Int {
                        return ((availableSpace - 2 * pageSpacing) * 0.5f).toInt()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(200.dp)
            ) {
                val bitmap = bitmaps[it].asImageBitmap()
                Image(
                    bitmap = bitmap,
                    "assetsImage",
                )
            }
        }
    } else {
        LoadingComponent()
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