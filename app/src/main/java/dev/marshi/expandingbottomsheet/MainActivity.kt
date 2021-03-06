package dev.marshi.expandingbottomsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dev.marshi.expandingbottomsheet.ui.theme.ExpandingBottomSheetTheme
import kotlinx.coroutines.launch

private const val ExpandedSheetAlpha = 0.96f

private val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpandingBottomSheetTheme {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray), text = "aaiaiai"
                )
                ExpandingBottomSheetWrapper()
            }
        }
    }
}

@Preview
@Composable
fun ExpandingBottomSheetPreview() {
    ExpandingBottomSheetTheme {
        ExpandingBottomSheetWrapper()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExpandingBottomSheetWrapper() {
    val scroll = rememberLazyListState()
    val endColor = MaterialTheme.colors.primarySurface.copy(alpha = ExpandedSheetAlpha)
    val dynamicSurfaceColor = { openFraction: Float ->
        lerp(
            startColor = Color.Blue,
            endColor = endColor,
            startFraction = 0f,
            endFraction = 0.3f,
            fraction = openFraction
        )
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberSwipeableState(initialValue = SheetState.Closed)
    ExpandingBottomSheet(
        fabSize = 56.dp,
        semiOpenFabSize = 90.dp,
        dynamicSurfaceColor = dynamicSurfaceColor,
        sheetState = sheetState,
        fabContent = {
            IconButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    when (sheetState.currentValue) {
                        SheetState.Closed -> scope.launch { sheetState.animateTo(SheetState.SemiOpen) }
                        SheetState.SemiOpen -> scope.launch { sheetState.animateTo(SheetState.Open) }
                        SheetState.Open -> {}
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistPlay,
                    tint = MaterialTheme.colors.onPrimary,
                    contentDescription = "description"
                )
            }
        },
        bottomSheetContent = {
            LazyColumn(
                state = scroll,
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = false
                )
            ) {
                val lessons: List<String> = List(300) {
                    "aiueo_$it"
                }
                items(lessons) { str ->
                    Text("lesson")
                    Divider(startIndent = 128.dp)
                }
            }
        },
        appBar = { openFraction ->
            val appBarElevation by animateDpAsState(if (scroll.isScrolled) 4.dp else 0.dp)
            val appBarColor = if (appBarElevation > 0.dp) {
                dynamicSurfaceColor(openFraction)
            } else {
                Color.Transparent
            }
            TopAppBar(
                backgroundColor = appBarColor,
                elevation = appBarElevation
            ) {
                Text(
                    text = "course name",
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
                IconButton(
                    onClick = {
                        scope.launch { sheetState.animateTo(SheetState.Closed) }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null
                    )
                }
            }
        }
    )
}
