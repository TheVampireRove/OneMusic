package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.OneUISquircleSmall
import com.example.ui.theme.glassmorphism
import kotlin.math.roundToInt

/**
 * Movable widget item representing a player button on the 2D infinite canvas.
 */
data class MovableWidgetItem(
    val id: String,
    val name: String,
    val worldPosition: Offset,
    val content: @Composable (isEditMode: Boolean, isBeingDragged: Boolean) -> Unit
)

/**
 * State engine for the Infinite Canvas & Viewport.
 * Manages:
 * - Global viewport panOffset across infinite 2D space.
 * - Viewport scale / zoom.
 * - Magnetic grid snapping calculations.
 * - Edit / Customize mode vs. Locked Playback mode.
 */
@Stable
class InfiniteCanvasState(
    initialPan: Offset = Offset.Zero,
    initialZoom: Float = 1.0f,
    initialGridSnap: Boolean = true,
    val gridSizePx: Float = 84f // ~28-32dp
) {
    var panOffset by mutableStateOf(initialPan)
    var zoomScale by mutableFloatStateOf(initialZoom)
    var isGridSnapEnabled by mutableStateOf(initialGridSnap)
    var isEditMode by mutableStateOf(false)
    var activeDraggedItemId by mutableStateOf<String?>(null)

    fun panBy(delta: Offset) {
        panOffset += delta
    }

    fun recenter() {
        panOffset = Offset.Zero
    }

    fun snapToGrid(position: Offset): Offset {
        if (!isGridSnapEnabled || gridSizePx <= 0f) return position
        val snappedX = (position.x / gridSizePx).roundToInt() * gridSizePx
        val snappedY = (position.y / gridSizePx).roundToInt() * gridSizePx
        return Offset(snappedX, snappedY)
    }
}

@Composable
fun rememberInfiniteCanvasState(
    initialPan: Offset = Offset.Zero,
    initialZoom: Float = 1.0f,
    initialGridSnap: Boolean = true,
    gridSizeDp: Dp = 28.dp
): InfiniteCanvasState {
    val density = LocalDensity.current
    val gridSizePx = with(density) { gridSizeDp.toPx() }
    return remember(initialPan, initialZoom, initialGridSnap) {
        InfiniteCanvasState(
            initialPan = initialPan,
            initialZoom = initialZoom,
            initialGridSnap = initialGridSnap,
            gridSizePx = gridSizePx
        )
    }
}

/**
 * High-performance Infinite Canvas & Movable Button Layout for Tabletop / Flex mode.
 * 
 * Features:
 * 1. Looping infinite coordinate dot-matrix & grid lines with modulo calculation.
 * 2. Unrestricted 2D canvas panning in both locked playback and edit mode.
 * 3. Independent repositioning of each playback control on the grid.
 * 4. Magnetic grid snapping (round(pos / gridSize) * gridSize) on drag release.
 * 5. Unlock / Edit Grid toggle with glowing selection rings and drag handles.
 * 6. Recenter / Reset View button to restore the viewport and default cluster.
 * 7. Hardware-accelerated 60/120fps graphicsLayer translations.
 */
@Composable
fun TabletopInfiniteCanvas(
    state: InfiniteCanvasState,
    items: List<MovableWidgetItem>,
    onItemMoved: (id: String, newWorldPosition: Offset) -> Unit,
    onResetAll: () -> Unit,
    onSavePositions: () -> Unit,
    accentColor: Color,
    canvasBackgroundColor: Color,
    modifier: Modifier = Modifier,
    extraContent: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_glow")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(canvasBackgroundColor)
            .testTag("tabletop_infinite_canvas")
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()
        val centerAnchor = Offset(canvasWidthPx / 2f, canvasHeightPx / 2f)

        // 1. Dynamic Infinite Grid & Dot Matrix Background (ONLY visible when entering Customize Grid mode)
        if (state.isEditMode) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            state.panBy(dragAmount)
                        }
                    }
            ) {
                val step = state.gridSizePx * state.zoomScale
                if (step > 4f) {
                    val panX = state.panOffset.x
                    val panY = state.panOffset.y

                    // Modulo calculation ensures seamless infinite looping in both axes
                    val startX = (panX % step).let { if (it > 0) it - step else it }
                    val startY = (panY % step).let { if (it > 0) it - step else it }

                    val gridLineColor = accentColor.copy(alpha = 0.16f)
                    val dotColor = accentColor.copy(alpha = 0.40f)

                    // Render subtle coordinate grid lines
                    var x = startX
                    while (x < size.width + step) {
                        drawLine(
                            color = gridLineColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.2f
                        )
                        x += step
                    }

                    var y = startY
                    while (y < size.height + step) {
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.2f
                        )
                        y += step
                    }

                    // Render coordinate intersection dots
                    x = startX
                    while (x < size.width + step) {
                        y = startY
                        while (y < size.height + step) {
                            drawCircle(
                                color = dotColor,
                                radius = 2.4f,
                                center = Offset(x, y)
                            )
                            y += step
                        }
                        x += step
                    }

                    // Origin crosshair marker (centered around 0, 0 in world space)
                    val originScreenX = centerAnchor.x + state.panOffset.x
                    val originScreenY = centerAnchor.y + state.panOffset.y
                    if (originScreenX in -40f..(size.width + 40f) && originScreenY in -40f..(size.height + 40f)) {
                        val crosshairColor = accentColor.copy(alpha = 0.5f)
                        drawLine(
                            color = crosshairColor,
                            start = Offset(originScreenX - 16f, originScreenY),
                            end = Offset(originScreenX + 16f, originScreenY),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = crosshairColor,
                            start = Offset(originScreenX, originScreenY - 16f),
                            end = Offset(originScreenX, originScreenY + 16f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        } else {
            // When locked / in playback mode: clean background with no grid lines, supporting 2D panning of the canvas view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            state.panBy(dragAmount)
                        }
                    }
            )
        }

        // 2. Render Movable Widget Items at Screen Coordinates
        items.forEach { item ->
            val isBeingDragged = state.activeDraggedItemId == item.id

            // Screen coordinate mapping formula:
            // screenPosition = centerAnchor + (item.worldPosition * zoomScale) + panOffset
            val screenX = centerAnchor.x + (item.worldPosition.x * state.zoomScale) + state.panOffset.x
            val screenY = centerAnchor.y + (item.worldPosition.y * state.zoomScale) + state.panOffset.y

            val dragScale by animateFloatAsState(
                targetValue = if (isBeingDragged) 1.12f else 1.0f,
                animationSpec = spring(stiffness = 500f),
                label = "drag_scale_${item.id}"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = (item.worldPosition.x * state.zoomScale) + state.panOffset.x
                        translationY = (item.worldPosition.y * state.zoomScale) + state.panOffset.y
                        scaleX = dragScale * state.zoomScale
                        scaleY = dragScale * state.zoomScale
                    }
                    .then(
                        if (state.isEditMode) {
                            Modifier
                                .pointerInput(item.id, state.isGridSnapEnabled) {
                                    detectDragGestures(
                                        onDragStart = {
                                            state.activeDraggedItemId = item.id
                                        },
                                        onDragEnd = {
                                            val finalPos = if (state.isGridSnapEnabled) {
                                                state.snapToGrid(item.worldPosition)
                                            } else {
                                                item.worldPosition
                                            }
                                            onItemMoved(item.id, finalPos)
                                            state.activeDraggedItemId = null
                                            onSavePositions()
                                        },
                                        onDragCancel = {
                                            state.activeDraggedItemId = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val unscaledDelta = Offset(
                                                dragAmount.x / state.zoomScale,
                                                dragAmount.y / state.zoomScale
                                            )
                                            val newPos = item.worldPosition + unscaledDelta
                                            onItemMoved(item.id, newPos)
                                        }
                                    )
                                }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Edit Mode Visual Ring / Glowing Drag Indicator (Safe non-negative styling)
                if (state.isEditMode) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                width = if (isBeingDragged) 2.dp else 1.2.dp,
                                color = if (isBeingDragged) accentColor else accentColor.copy(alpha = pulseGlowAlpha),
                                shape = CircleShape
                            )
                    )
                }

                // Actual Composable Content (Play/Pause, Skips, Repeat, Shuffle, Queue, Volume)
                item.content(state.isEditMode, isBeingDragged)
            }
        }

        // 3. Optional Overlay Extra Content (e.g. Scrubber Bar, Dolby Ribbon)
        if (extraContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                extraContent()
            }
        }

        // 4. Infinite Canvas HUD Toolbar (Recenter, Snap, Scale, Lock/Unlock)
        TabletopCanvasToolbar(
            state = state,
            accentColor = accentColor,
            onRecenter = {
                state.recenter()
                state.zoomScale = 1.0f
            },
            onResetAll = {
                state.recenter()
                state.zoomScale = 1.0f
                onResetAll()
            },
            onSave = {
                state.isEditMode = false
                onSavePositions()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/**
 * Ergonomic floating HUD toolbar for the Infinite Canvas.
 */
@Composable
private fun TabletopCanvasToolbar(
    state: InfiniteCanvasState,
    accentColor: Color,
    onRecenter: () -> Unit,
    onResetAll: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(
            visible = state.isEditMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(
                        shape = OneUISquircleMedium,
                        backgroundColor = Color(0xD017192C),
                        borderWidth = 1.dp,
                        elevation = 8.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header row: Status + Action Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Infinite Canvas Editor",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Magnetic Grid Snap Toggle
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(if (state.isGridSnapEnabled) accentColor.copy(alpha = 0.28f) else Color(0x22FFFFFF))
                                .border(
                                    0.8.dp,
                                    if (state.isGridSnapEnabled) accentColor else Color(0x44FFFFFF),
                                    OneUIPillShape
                                )
                                .clickable { state.isGridSnapEnabled = !state.isGridSnapEnabled }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("canvas_snap_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Grid Snap",
                                    tint = if (state.isGridSnapEnabled) accentColor else Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isGridSnapEnabled) "Snap: ON" else "Snap: OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }

                        // Recenter View
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(Color(0x22FFFFFF))
                                .clickable { onRecenter() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("canvas_recenter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = "Recenter",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Center",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }

                        // Reset All Positions
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(Color(0x22FFFFFF))
                                .clickable { onResetAll() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("canvas_reset_all_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Positions",
                                    tint = Color(0xEEFFFFFF),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }

                        // Done / Save
                        Box(
                            modifier = Modifier
                                .clip(OneUIPillShape)
                                .background(accentColor)
                                .clickable { onSave() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("canvas_done_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color(0xFF0F101E),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Done",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF0F101E)
                                )
                            }
                        }
                    }
                }

                // Button Scale Control with Stepper & Presets
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    state.zoomScale = (state.zoomScale - 0.25f).coerceIn(0.70f, 5.00f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Scale: ${(state.zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xCCFFFFFF),
                            modifier = Modifier
                                .clickable { state.zoomScale = 1.0f }
                                .width(74.dp)
                        )

                        Slider(
                            value = state.zoomScale.coerceIn(0.70f, 5.00f),
                            onValueChange = { state.zoomScale = it },
                            valueRange = 0.70f..5.00f,
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color(0x35FFFFFF)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    state.zoomScale = (state.zoomScale + 0.25f).coerceIn(0.70f, 5.00f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Quick Scale Presets (up to 500%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            1.0f to "100%",
                            2.0f to "200%",
                            3.0f to "300%",
                            4.0f to "400%",
                            5.0f to "500%"
                        ).forEach { (presetScale, label) ->
                            val isSelected = kotlin.math.abs(state.zoomScale - presetScale) < 0.1f
                            Box(
                                modifier = Modifier
                                    .clip(OneUIPillShape)
                                    .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                    .border(
                                        0.6.dp,
                                        if (isSelected) accentColor else Color(0x30FFFFFF),
                                        OneUIPillShape
                                    )
                                    .clickable { state.zoomScale = presetScale }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.White else Color(0xBBFFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }


    }
}
