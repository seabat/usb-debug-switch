package dev.seabat.android.usbdebugswitch.compose.tutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.HorizontalPagerIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialContent(
    modifier: Modifier = Modifier,
    bitmaps: List<Bitmap>,
    onClose: () -> Unit
) {
    val descriptions = arrayListOf(
        stringResource(id = R.string.tutorial_description_1),
        stringResource(id = R.string.tutorial_description_2),
        stringResource(id = R.string.tutorial_description_3),
        stringResource(id = R.string.tutorial_description_4),
        stringResource(id = R.string.tutorial_description_5),
        stringResource(id = R.string.tutorial_description_6),
        stringResource(id = R.string.tutorial_description_7),
        stringResource(id = R.string.tutorial_description_8),
        stringResource(id = R.string.tutorial_description_9),
        stringResource(id = R.string.tutorial_description_10),
        stringResource(id = R.string.tutorial_description_11),
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pagerState = rememberPagerState {
                bitmaps.size
            }
            HorizontalPager(
                beyondBoundsPageCount = bitmaps.size,
                state = pagerState,
                contentPadding = PaddingValues(
//                horizontal = 32.dp,
                    vertical = 8.dp
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bitmap = bitmaps[it].asImageBitmap()
                    Image(
                        bitmap = bitmap,
                        "assetsImage",
                        modifier = Modifier.height(300.dp)
                    )
                    Text(
                        text = descriptions[it],
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }
            }
            HorizontalPagerIndicator(
                pageCount = bitmaps.size,
                currentPage = pagerState.currentPage,
                targetPage = pagerState.targetPage,
                currentPageOffsetFraction = pagerState.currentPageOffsetFraction
            )

            // 閉じる
            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .clickable { onClose() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_close_40),
                    contentDescription = "assetsImage",
                )
                Text(
                    text = stringResource(id = R.string.tutorial_close),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun TutorialContentPreview() {
    val context = LocalContext.current
    TutorialContent(
        bitmaps = arrayListOf(
            BitmapFactory.decodeStream(
                context.assets.open("screenshot_app_off.png")
            )
        )
    ) {}
}
