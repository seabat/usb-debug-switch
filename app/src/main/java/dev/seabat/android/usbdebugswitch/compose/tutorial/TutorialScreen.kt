package dev.seabat.android.usbdebugswitch.compose.tutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.HorizontalPagerIndicator
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TutorialScreen(
    tutorialLoadedFlow: StateFlow<Boolean>,
    bitmapsFlow: StateFlow<MutableList<Bitmap>>,
    onClose: () -> Unit
) {
    val tutorialLoaded by tutorialLoadedFlow.collectAsState()
    val bitmaps by bitmapsFlow.collectAsState()

    if (tutorialLoaded) {
        TutorialContent(bitmaps, onClose)
    } else {
        LoadingComponent()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TutorialContent(
    bitmaps: List<Bitmap>,
    onClose: () -> Unit
) {
    val descriptions = arrayListOf(
        stringResource(id = R.string.tutorial_description_1),
        stringResource(id = R.string.tutorial_description_2),
        stringResource(id = R.string.tutorial_description_3),
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF2F0F4)),
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.tutorial_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(color = Color(0xFFF2F0F4))
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
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
                            modifier = Modifier.fillMaxHeight(0.6f)
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
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
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
    ) { /* Do nothing */ }
}