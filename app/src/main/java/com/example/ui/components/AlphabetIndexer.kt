package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.glassmorphism
import kotlin.math.roundToInt

val AlphabetList = listOf('#') + ('A'..'Z').toList()

@Composable
fun AlphabetIndexer(
    accentColor: Color,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var columnHeight by remember { mutableFloatStateOf(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    fun selectLetterAtY(y: Float) {
        if (columnHeight <= 0f) return
        val clampedY = y.coerceIn(0f, columnHeight)
        touchY = clampedY
        val itemHeight = columnHeight / AlphabetList.size
        val index = (clampedY / itemHeight).toInt().coerceIn(0, AlphabetList.size - 1)
        val letter = AlphabetList[index]
        if (activeLetter != letter) {
            activeLetter = letter
            onLetterSelected(letter)
        } else {
            onLetterSelected(letter)
        }
    }

    val bubbleScale by animateFloatAsState(
        targetValue = if (activeLetter != null) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "alphabet_bubble_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(36.dp)
            .padding(end = 4.dp)
            .testTag("alphabet_quick_scroll_bar"),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .width(28.dp)
                .glassmorphism(
                    shape = OneUIPillShape,
                    backgroundColor = Color(0x351B1D36),
                    borderWidth = 0.8.dp
                )
                .padding(vertical = 6.dp, horizontal = 2.dp)
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        selectLetterAtY(down.position.y)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                            if (change == null || !change.pressed) {
                                activeLetter = null
                                break
                            }
                            change.consume()
                            selectLetterAtY(change.position.y)
                        }
                    }
                },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlphabetList.forEach { char ->
                val isSelected = activeLetter == char
                Text(
                    text = char.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isSelected) accentColor else Color(0xAAFFFFFF),
                    lineHeight = 12.sp
                )
            }
        }

        // Large non-clipping floating letter bubble displayed beside the user's finger while pressing & dragging
        activeLetter?.let { letter ->
            val bubbleSizePx = with(density) { 88.dp.toPx() }
            val horizontalGapPx = with(density) { 46.dp.roundToPx() }
            val maxYOffset = (columnHeight - bubbleSizePx).coerceAtLeast(0f)
            val verticalOffsetPx = (touchY - bubbleSizePx / 2f).coerceIn(0f, maxYOffset).roundToInt()

            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = -horizontalGapPx, y = verticalOffsetPx)
            ) {
                val bubbleShape = RoundedCornerShape(
                    topStart = 44.dp,
                    topEnd = 44.dp,
                    bottomStart = 44.dp,
                    bottomEnd = 12.dp
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            scaleX = bubbleScale
                            scaleY = bubbleScale
                        }
                        .shadow(16.dp, bubbleShape, clip = false)
                        .clip(bubbleShape)
                        .background(accentColor)
                        .border(2.dp, Color.White.copy(alpha = 0.85f), bubbleShape)
                        .testTag("alphabet_scroll_bubble"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 40.sp
                        ),
                        color = Color(0xFF0C0E1A)
                    )
                }
            }
        }
    }
}
