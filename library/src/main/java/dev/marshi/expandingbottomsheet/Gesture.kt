package dev.marshi.expandingbottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class State {
    Closed,
    Open
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Gesture() {
    val state = rememberSwipeableState(initialValue = State.Closed)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .swipeable(
                state = state,
                anchors = mapOf(0f to State.Closed, 300f to State.Open),
                orientation = Orientation.Vertical,
            )
            .background(color = Color.Blue)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(0, state.offset.value.roundToInt()) }
                .size(40.dp)
                .background(Color.White)
        )
    }
}

@Preview(name = "preview")
@Composable
fun GesturePreview() {
    Gesture()
}
