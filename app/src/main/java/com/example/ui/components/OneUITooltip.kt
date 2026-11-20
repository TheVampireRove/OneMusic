package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MotionSpecs
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.bouncableCombinedClick
import com.example.ui.theme.glassmorphism
import kotlinx.coroutines.launch

val LocalOneUITooltipHandler = compositionLocalOf<(String) -> Unit> {
    { _ -> }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUITooltipButton(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape? = null,
    consumeClicks: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    enableBounce: Boolean = interactionSource == null,
    pressedScale: Float = 0.94f,
    content: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val globalTooltipHandler = LocalOneUITooltipHandler.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Box(
                modifier = Modifier
                    .glassmorphism(
                        shape = OneUIPillShape,
                        backgroundColor = Color(0xEE1E2038),
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        ),
                        color = Color.White
                    )
                }
            }
        },
        state = tooltipState,
        modifier = modifier
    ) {
        if (consumeClicks) {
            val triggerLongPress: () -> Unit = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    tooltipState.show()
                }
            }
            val clickModifier = if (enableBounce) {
                Modifier.bouncableCombinedClick(
                    enabled = enabled,
                    pressedScale = pressedScale,
                    interactionSource = interactionSource,
                    useDefaultIndication = false,
                    onClick = onClick,
                    onLongClick = triggerLongPress
                )
            } else {
                Modifier.oneUILongPress(
                    name = name,
                    onClick = onClick,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    onLongClick = triggerLongPress
                )
            }
            Box(
                modifier = Modifier
                    .then(if (shape != null) Modifier.clip(shape) else Modifier)
                    .background(Color.Transparent)
                    .then(clickModifier)
            ) {
                content()
            }
        } else {
            Box(
                modifier = Modifier
                    .then(if (shape != null) Modifier.clip(shape) else Modifier)
                    .background(Color.Transparent)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.oneUILongPress(
    name: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onLongClick: (() -> Unit)? = null
): Modifier = this.combinedClickable(
    enabled = enabled,
    interactionSource = interactionSource,
    indication = null,
    onClick = onClick,
    onLongClick = onLongClick
)

@Composable
fun OneUITooltipBanner(
    text: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn(animationSpec = MotionSpecs.snappySpring()) + scaleIn(
            initialScale = 0.85f,
            animationSpec = MotionSpecs.snappySpring()
        ),
        exit = fadeOut(animationSpec = MotionSpecs.snappySpring()) + scaleOut(
            targetScale = 0.85f,
            animationSpec = MotionSpecs.snappySpring()
        ),
        modifier = modifier
    ) {
        if (text != null) {
            Box(
                modifier = Modifier
                    .glassmorphism(
                        shape = OneUIPillShape,
                        backgroundColor = Color(0xF0181A30),
                        borderWidth = 1.2.dp,
                        elevation = 16.dp
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
