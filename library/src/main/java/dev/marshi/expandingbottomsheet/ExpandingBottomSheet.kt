package dev.marshi.expandingbottomsheet

import androidx.activity.compose.BackHandler
import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.launch

enum class SheetState {
    Closed,
    Open
}

private val FabSize = 56.dp
private const val ExpandedSheetAlpha = 0.96f

// https://github.com/android/compose-samples/blob/bd546b0a021554adac82bb0d2996fc3e76b552f2/Owl/app/src/main/java/com/example/owl/ui/course/CourseDetails.kt#L138-L166
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExpandingBottomSheet(
    dynamicSurfaceColor: (openFraction: Float) -> Color,
    fabContent: @Composable BoxScope.((SheetState) -> Unit) -> Unit,
    bottomSheetContent: @Composable ColumnScope.() -> Unit,
    appBar: @Composable ColumnScope.(openFraction: Float, (SheetState) -> Unit) -> Unit,
) {
    BoxWithConstraints {
        val sheetState = rememberSwipeableState(SheetState.Closed)
        val fabSize = with(LocalDensity.current) { FabSize.toPx() }
        val dragRange = constraints.maxHeight - fabSize
        val scope = rememberCoroutineScope()

        fun updateSheet(state: SheetState) {
            scope.launch {
                sheetState.animateTo(state)
            }
        }

        BackHandler(
            enabled = sheetState.currentValue == SheetState.Open,
            onBack = {
                scope.launch {
                    sheetState.animateTo(SheetState.Closed)
                }
            }
        )
        Box(
            Modifier.swipeable(
                state = sheetState,
                anchors = mapOf(
                    0f to SheetState.Closed,
                    -dragRange to SheetState.Open
                ),
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Vertical
            )
        ) {
            val openFraction = if (sheetState.offset.value.isNaN()) {
                0f
            } else {
                -sheetState.offset.value / dragRange
            }.coerceIn(0f, 1f)
            val surfaceColorVal = dynamicSurfaceColor(openFraction)
            BottomSheet(
                openFraction = openFraction,
                width = this@BoxWithConstraints.constraints.maxWidth.toFloat(),
                height = this@BoxWithConstraints.constraints.maxHeight.toFloat(),
                surfaceColor = surfaceColorVal,
                appBar = { appBar(openFraction, ::updateSheet) },
                fabContent = { fabContent(::updateSheet) },
                bottomSheetContent = bottomSheetContent,
            )
        }
    }
}


@Composable
private fun BottomSheet(
    openFraction: Float,
    width: Float,
    height: Float,
    surfaceColor: Color = MaterialTheme.colors.surface,
    appBar: @Composable ColumnScope.() -> Unit,
    fabContent: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable ColumnScope.() -> Unit,
) {
    // Use the fraction that the sheet is open to drive the transformation from FAB -> Sheet
    val fabSize = with(LocalDensity.current) { FabSize.toPx() }
    val fabSheetHeight = fabSize + LocalWindowInsets.current.systemBars.bottom
    val offsetX = lerp(width - fabSize, 0f, 0f, 0.15f, openFraction)
    val offsetY = lerp(height - fabSheetHeight, 0f, openFraction)
    val tlCorner = lerp(fabSize, 0f, 0f, 0.15f, openFraction)

    Surface(
        color = surfaceColor,
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.primarySurface),
        shape = RoundedCornerShape(topStart = tlCorner),
        modifier = Modifier.graphicsLayer {
            translationX = offsetX
            translationY = offsetY
        }
    ) {
        BottomSheet(
            openFraction,
            appBar = appBar,
            bottomSheetContent = bottomSheetContent,
            fabContent = fabContent
        )
    }
}

@Composable
private fun BottomSheet(
    openFraction: Float,
    appBar: @Composable ColumnScope.() -> Unit,
    bottomSheetContent: @Composable ColumnScope.() -> Unit,
    fabContent: @Composable BoxScope.() -> Unit,
) {

    Box(modifier = Modifier.fillMaxWidth()) {
        // When sheet open, show a list of the lessons
        val bottomSheetAlpha = lerp(0f, 1f, 0.2f, 0.8f, openFraction)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bottomSheetAlpha }
                .statusBarsPadding()
        ) {
            appBar()
            bottomSheetContent()
        }

        // When sheet closed, show the FAB
        val fabAlpha = lerp(1f, 0f, 0f, 0.15f, openFraction)
        Box(
            modifier = Modifier
                .size(FabSize)
                .padding(start = 16.dp, top = 8.dp) // visually center contents
                .graphicsLayer { alpha = fabAlpha }
        ) {
            fabContent()
        }
    }
}


private val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

/**
 * Linearly interpolate between two [Float]s when the [fraction] is in a given range.
 */
fun lerp(
    startValue: Float,
    endValue: Float,
    @FloatRange(from = 0.0, to = 1.0) startFraction: Float,
    @FloatRange(from = 0.0, to = 1.0) endFraction: Float,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float
): Float {
    if (fraction < startFraction) return startValue
    if (fraction > endFraction) return endValue

    return lerp(startValue, endValue, (fraction - startFraction) / (endFraction - startFraction))
}

fun lerp(
    startValue: Float,
    endValue: Float,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float
): Float {
    return startValue + fraction * (endValue - startValue)
}

fun lerp(
    startColor: Color,
    endColor: Color,
    @FloatRange(from = 0.0, to = 1.0) startFraction: Float,
    @FloatRange(from = 0.0, to = 1.0) endFraction: Float,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float
): Color {
    if (fraction < startFraction) return startColor
    if (fraction > endFraction) return endColor

    return lerp(
        startColor,
        endColor,
        (fraction - startFraction) / (endFraction - startFraction)
    )
}


@Preview(name = "preview")
@Composable
private fun CourseDetailsPreview() {
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
    ExpandingBottomSheet(
        dynamicSurfaceColor = dynamicSurfaceColor,
        fabContent = { updateSheet ->
            IconButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = { updateSheet(SheetState.Open) }
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
        appBar = { openFraction, updateSheet ->
            val appBarElevation by animateDpAsState(if (scroll.isScrolled) 4.dp else 0.dp)
            println("elevation $appBarElevation")
            val appBarColor =
                if (appBarElevation > 0.dp) dynamicSurfaceColor(openFraction) else Color.Transparent
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
                    onClick = { updateSheet(SheetState.Closed) },
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

