package com.example.ui.components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.model.LayoutType
import com.example.model.LineStyle
import com.example.model.SliceCustomConfig
import com.example.model.SplitLine
import com.example.model.TileShadowStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun SliceCanvasOverlay(
    cropRect: RectF, // 0f..1f
    layoutType: LayoutType,
    rows: Int,
    cols: Int,
    showIndices: Boolean,
    splitLines: List<SplitLine>,
    freeBoxes: List<RectF>,
    gapFraction: Float,
    gridLineColor: Color = Color(0xFFFFFFFF),
    gridLineWidthDp: Float = 3.0f,
    lineStyle: LineStyle = LineStyle.SOLID,
    shadowStyle: TileShadowStyle = TileShadowStyle.NONE,
    outerCornerRadiusDp: Float = 0f,
    isSnapGuideEnabled: Boolean = true,
    heroFocusIndex: Int? = null,
    isEyedropperActive: Boolean = false,
    perSliceConfigs: Map<Int, SliceCustomConfig> = emptyMap(),
    selectedPieceIndex: Int? = null,
    activeSplitLineId: String? = null,
    onCropRectChange: (RectF) -> Unit,
    onSplitLineMove: (lineId: String, newPos: Float) -> Unit,
    onSplitLineClick: ((String) -> Unit)? = null,
    onSplitLineDelete: ((String) -> Unit)? = null,
    onSliceClick: ((Int) -> Unit)? = null,
    onEyedropperPick: ((fracX: Float, fracY: Float) -> Unit)? = null,
    onDragStart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localActiveSplitLineId by remember { mutableStateOf<String?>(null) }
    var draggingHandle by remember { mutableStateOf<String?>(null) }
    var currentTouchPos by remember { mutableStateOf<Offset?>(null) }
    var eyedropperTouchPos by remember { mutableStateOf<Offset?>(null) }
    var activeSnapPos by remember { mutableStateOf<Float?>(null) }
    var isSnapVertical by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val currentActiveLineId = activeSplitLineId ?: localActiveSplitLineId
    val snapTargets = listOf(0.25f, 0.333f, 0.50f, 0.618f, 0.667f, 0.75f)

    val pointerModifier = if (isEyedropperActive) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                eyedropperTouchPos = down.position
                val fx = (down.position.x / w).coerceIn(0f, 1f)
                val fy = (down.position.y / h).coerceIn(0f, 1f)
                onEyedropperPick?.invoke(fx, fy)

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (!change.pressed) break
                    change.consume()
                    eyedropperTouchPos = change.position
                    val curFx = (change.position.x / w).coerceIn(0f, 1f)
                    val curFy = (change.position.y / h).coerceIn(0f, 1f)
                    onEyedropperPick?.invoke(curFx, curFy)
                }
            }
        }
    } else {
        Modifier
            .pointerInput(cropRect, layoutType, splitLines, rows, cols) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cropLeft = cropRect.left * w
                        val cropTop = cropRect.top * h
                        val cropW = (cropRect.right - cropRect.left) * w
                        val cropH = (cropRect.bottom - cropRect.top) * h

                        if (layoutType == LayoutType.FREE_SPLIT_LINES) {
                            // Check if tapped on or near any split line
                            val hitThreshold = 60f
                            for (line in splitLines) {
                                if (line.isVertical) {
                                    val lx = cropLeft + line.positionFraction * cropW
                                    if (abs(tapOffset.x - lx) < hitThreshold && tapOffset.y in (cropTop - 30)..(cropTop + cropH + 30)) {
                                        localActiveSplitLineId = line.id
                                        onSplitLineClick?.invoke(line.id)
                                        return@detectTapGestures
                                    }
                                } else {
                                    val ly = cropTop + line.positionFraction * cropH
                                    if (abs(tapOffset.y - ly) < hitThreshold && tapOffset.x in (cropLeft - 30)..(cropLeft + cropW + 30)) {
                                        localActiveSplitLineId = line.id
                                        onSplitLineClick?.invoke(line.id)
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }

                        // Check if tapped inside a grid slice
                        if (tapOffset.x in cropLeft..(cropLeft + cropW) && tapOffset.y in cropTop..(cropTop + cropH)) {
                            val r = when (layoutType) {
                                LayoutType.GRID_3x3 -> 3
                                LayoutType.GRID_2x2 -> 2
                                LayoutType.GRID_2x3 -> 2
                                LayoutType.GRID_3x2 -> 3
                                LayoutType.GRID_1x3 -> 1
                                LayoutType.GRID_3x1 -> 3
                                LayoutType.GRID_3x4 -> 3
                                LayoutType.GRID_CUSTOM -> rows.coerceIn(1, 10)
                                else -> layoutType.defaultRows.coerceAtLeast(1)
                            }
                            val c = when (layoutType) {
                                LayoutType.GRID_3x3 -> 3
                                LayoutType.GRID_2x2 -> 2
                                LayoutType.GRID_2x3 -> 3
                                LayoutType.GRID_3x2 -> 2
                                LayoutType.GRID_1x3 -> 3
                                LayoutType.GRID_3x1 -> 1
                                LayoutType.GRID_3x4 -> 4
                                LayoutType.GRID_CUSTOM -> cols.coerceIn(1, 10)
                                else -> layoutType.defaultCols.coerceAtLeast(1)
                            }

                            val colIdx = ((tapOffset.x - cropLeft) / (cropW / c)).toInt().coerceIn(0, c - 1)
                            val rowIdx = ((tapOffset.y - cropTop) / (cropH / r)).toInt().coerceIn(0, r - 1)
                            val pieceIdx = rowIdx * c + colIdx + 1
                            onSliceClick?.invoke(pieceIdx)
                        }
                    }
                )
            }
            .pointerInput(cropRect, layoutType, splitLines, isSnapGuideEnabled) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentTouchPos = offset
                        onDragStart()
                        activeSnapPos = null
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Check if hitting split lines in FREE_SPLIT_LINES mode with high sensitivity (60f hit radius)
                        if (layoutType == LayoutType.FREE_SPLIT_LINES) {
                            val cropLeft = cropRect.left * w
                            val cropTop = cropRect.top * h
                            val cropW = (cropRect.right - cropRect.left) * w
                            val cropH = (cropRect.bottom - cropRect.top) * h

                            val hitThreshold = 60f
                            for (line in splitLines) {
                                if (line.isVertical) {
                                    val lineX = cropLeft + line.positionFraction * cropW
                                    if (abs(offset.x - lineX) < hitThreshold &&
                                        offset.y in (cropTop - 40)..(cropTop + cropH + 40)
                                    ) {
                                        localActiveSplitLineId = line.id
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSplitLineClick?.invoke(line.id)
                                        return@detectDragGestures
                                    }
                                } else {
                                    val lineY = cropTop + line.positionFraction * cropH
                                    if (abs(offset.y - lineY) < hitThreshold &&
                                        offset.x in (cropLeft - 40)..(cropLeft + cropW + 40)
                                    ) {
                                        localActiveSplitLineId = line.id
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSplitLineClick?.invoke(line.id)
                                        return@detectDragGestures
                                    }
                                }
                            }
                        }

                        // Check crop rect corners/edges
                        val cl = cropRect.left * w
                        val ct = cropRect.top * h
                        val cr = cropRect.right * w
                        val cb = cropRect.bottom * h
                        val handleRadius = 48f

                        draggingHandle = when {
                            distance(offset.x, offset.y, cl, ct) < handleRadius -> "tl"
                            distance(offset.x, offset.y, cr, ct) < handleRadius -> "tr"
                            distance(offset.x, offset.y, cl, cb) < handleRadius -> "bl"
                            distance(offset.x, offset.y, cr, cb) < handleRadius -> "br"
                            abs(offset.x - cl) < handleRadius && offset.y in ct..cb -> "left"
                            abs(offset.x - cr) < handleRadius && offset.y in ct..cb -> "right"
                            abs(offset.y - ct) < handleRadius && offset.x in cl..cr -> "top"
                            abs(offset.y - cb) < handleRadius && offset.x in cl..cr -> "bottom"
                            offset.x in cl..cr && offset.y in ct..cb -> "body"
                            else -> null
                        }
                        if (draggingHandle != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentTouchPos = change.position
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        if (localActiveSplitLineId != null) {
                            val line = splitLines.find { it.id == localActiveSplitLineId } ?: return@detectDragGestures
                            val cropW = (cropRect.right - cropRect.left) * w
                            val cropH = (cropRect.bottom - cropRect.top) * h

                            if (line.isVertical) {
                                val delta = dragAmount.x / cropW
                                var rawPos = (line.positionFraction + delta).coerceIn(0.05f, 0.95f)
                                var snappedPos = rawPos
                                var snapFound: Float? = null

                                if (isSnapGuideEnabled) {
                                    for (st in snapTargets) {
                                        if (abs(rawPos - st) < 0.025f) {
                                            snappedPos = st
                                            snapFound = st
                                            break
                                        }
                                    }
                                }
                                if (snapFound != null && snapFound != activeSnapPos) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                activeSnapPos = snapFound
                                isSnapVertical = true
                                onSplitLineMove(line.id, snappedPos)
                            } else {
                                val delta = dragAmount.y / cropH
                                var rawPos = (line.positionFraction + delta).coerceIn(0.05f, 0.95f)
                                var snappedPos = rawPos
                                var snapFound: Float? = null

                                if (isSnapGuideEnabled) {
                                    for (st in snapTargets) {
                                        if (abs(rawPos - st) < 0.025f) {
                                            snappedPos = st
                                            snapFound = st
                                            break
                                        }
                                    }
                                }
                                if (snapFound != null && snapFound != activeSnapPos) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                activeSnapPos = snapFound
                                isSnapVertical = false
                                onSplitLineMove(line.id, snappedPos)
                            }
                            return@detectDragGestures
                        }

                        if (draggingHandle != null) {
                            val dx = dragAmount.x / w
                            val dy = dragAmount.y / h
                            var l = cropRect.left
                            var t = cropRect.top
                            var r = cropRect.right
                            var b = cropRect.bottom

                            when (draggingHandle) {
                                "tl" -> { l = (l + dx).coerceIn(0f, r - 0.15f); t = (t + dy).coerceIn(0f, b - 0.15f) }
                                "tr" -> { r = (r + dx).coerceIn(l + 0.15f, 1f); t = (t + dy).coerceIn(0f, b - 0.15f) }
                                "bl" -> { l = (l + dx).coerceIn(0f, r - 0.15f); b = (b + dy).coerceIn(t + 0.15f, 1f) }
                                "br" -> { r = (r + dx).coerceIn(l + 0.15f, 1f); b = (b + dy).coerceIn(t + 0.15f, 1f) }
                                "left" -> l = (l + dx).coerceIn(0f, r - 0.15f)
                                "right" -> r = (r + dx).coerceIn(l + 0.15f, 1f)
                                "top" -> t = (t + dy).coerceIn(0f, b - 0.15f)
                                "bottom" -> b = (b + dy).coerceIn(t + 0.15f, 1f)
                                "body" -> {
                                    val boxW = r - l
                                    val boxH = b - t
                                    val newL = (l + dx).coerceIn(0f, 1f - boxW)
                                    val newT = (t + dy).coerceIn(0f, 1f - boxH)
                                    l = newL
                                    r = newL + boxW
                                    t = newT
                                    b = newT + boxH
                                }
                            }
                            onCropRectChange(RectF(l, t, r, b))
                        }
                    },
                    onDragEnd = {
                        localActiveSplitLineId = null
                        draggingHandle = null
                        activeSnapPos = null
                        currentTouchPos = null
                    },
                    onDragCancel = {
                        localActiveSplitLineId = null
                        draggingHandle = null
                        activeSnapPos = null
                        currentTouchPos = null
                    }
                )
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(pointerModifier)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val leftPx = cropRect.left * canvasW
            val topPx = cropRect.top * canvasH
            val rightPx = cropRect.right * canvasW
            val bottomPx = cropRect.bottom * canvasH
            val widthPx = rightPx - leftPx
            val heightPx = bottomPx - topPx

            // 1. Draw darkened dim outside crop area
            val dimColor = Color.Black.copy(alpha = 0.55f)
            drawRect(dimColor, Offset(0f, 0f), Size(canvasW, topPx))
            drawRect(dimColor, Offset(0f, bottomPx), Size(canvasW, canvasH - bottomPx))
            drawRect(dimColor, Offset(0f, topPx), Size(leftPx, heightPx))
            drawRect(dimColor, Offset(rightPx, topPx), Size(canvasW - rightPx, heightPx))

            // 2. Draw Crop boundary
            val strokePx = (gridLineWidthDp * density).coerceIn(1f, 30f)
            val gridColor = gridLineColor
            val cornerRadPx = (outerCornerRadiusDp * density).coerceAtLeast(0f)

            if (cornerRadPx > 0.5f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(leftPx, topPx),
                    size = Size(widthPx, heightPx),
                    cornerRadius = CornerRadius(cornerRadPx, cornerRadPx),
                    style = Stroke(width = strokePx.coerceAtLeast(1.5f))
                )
            } else {
                drawRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(leftPx, topPx),
                    size = Size(widthPx, heightPx),
                    style = Stroke(width = strokePx.coerceAtLeast(1.5f))
                )
            }

            // 3. Draw Interior Slice Lines based on Layout and LineStyle
            val pathEffect = when (lineStyle) {
                LineStyle.SOLID -> null
                LineStyle.DASHED -> PathEffect.dashPathEffect(floatArrayOf(strokePx * 3f, strokePx * 2f), 0f)
                LineStyle.DOTTED -> PathEffect.dashPathEffect(floatArrayOf(strokePx, strokePx * 2f), 0f)
                LineStyle.FILM_SPROCKET -> PathEffect.dashPathEffect(floatArrayOf(strokePx * 4f, strokePx * 1.5f), 0f)
                LineStyle.POLAROID, LineStyle.NEON_GLOW -> null
            }

            val lineStroke = Stroke(
                width = if (lineStyle == LineStyle.POLAROID) strokePx * 2.5f else strokePx,
                pathEffect = pathEffect
            )

            when (layoutType) {
                LayoutType.CREATIVE_HERO_SPLIT -> {
                    val splitX = leftPx + widthPx * 0.62f
                    val splitY = topPx + heightPx * 0.5f
                    drawLine(gridColor, Offset(splitX, topPx), Offset(splitX, bottomPx), strokeWidth = lineStroke.width, pathEffect = lineStroke.pathEffect)
                    drawLine(gridColor, Offset(splitX, splitY), Offset(rightPx, splitY), strokeWidth = lineStroke.width, pathEffect = lineStroke.pathEffect)

                    if (showIndices) {
                        drawIndexCircle(1, Offset(leftPx + (splitX - leftPx) / 2, topPx + heightPx / 2), perSliceConfigs[1], selectedPieceIndex == 1)
                        drawIndexCircle(2, Offset(splitX + (rightPx - splitX) / 2, topPx + (splitY - topPx) / 2), perSliceConfigs[2], selectedPieceIndex == 2)
                        drawIndexCircle(3, Offset(splitX + (rightPx - splitX) / 2, splitY + (bottomPx - splitY) / 2), perSliceConfigs[3], selectedPieceIndex == 3)
                    }
                }

                LayoutType.CREATIVE_TOP_HERO -> {
                    val splitY = topPx + heightPx * 0.55f
                    drawLine(gridColor, Offset(leftPx, splitY), Offset(rightPx, splitY), strokeWidth = lineStroke.width, pathEffect = lineStroke.pathEffect)
                    val colW = widthPx / 3f
                    drawLine(gridColor, Offset(leftPx + colW, splitY), Offset(leftPx + colW, bottomPx), strokeWidth = lineStroke.width, pathEffect = lineStroke.pathEffect)
                    drawLine(gridColor, Offset(leftPx + colW * 2, splitY), Offset(leftPx + colW * 2, bottomPx), strokeWidth = lineStroke.width, pathEffect = lineStroke.pathEffect)

                    if (showIndices) {
                        drawIndexCircle(1, Offset(leftPx + widthPx / 2, topPx + (splitY - topPx) / 2), perSliceConfigs[1], selectedPieceIndex == 1)
                        drawIndexCircle(2, Offset(leftPx + colW * 0.5f, splitY + (bottomPx - splitY) / 2), perSliceConfigs[2], selectedPieceIndex == 2)
                        drawIndexCircle(3, Offset(leftPx + colW * 1.5f, splitY + (bottomPx - splitY) / 2), perSliceConfigs[3], selectedPieceIndex == 3)
                        drawIndexCircle(4, Offset(leftPx + colW * 2.5f, splitY + (bottomPx - splitY) / 2), perSliceConfigs[4], selectedPieceIndex == 4)
                    }
                }

                LayoutType.FREE_SPLIT_LINES -> {
                    for (line in splitLines) {
                        val isActive = line.id == currentActiveLineId
                        val activeLineColor = if (isActive) Color(0xFF38BDF8) else gridColor
                        val activeWidth = if (isActive) lineStroke.width * 1.6f else lineStroke.width

                        if (line.isVertical) {
                            val lineX = leftPx + line.positionFraction * widthPx
                            drawLine(activeLineColor, Offset(lineX, topPx), Offset(lineX, bottomPx), strokeWidth = activeWidth, pathEffect = lineStroke.pathEffect)

                            // Top & Bottom large draggable grab knobs
                            drawCircle(Color.White, radius = 13f, center = Offset(lineX, topPx + 16f))
                            drawCircle(Color(0xFF0284C7), radius = 10f, center = Offset(lineX, topPx + 16f))

                            drawCircle(Color.White, radius = 13f, center = Offset(lineX, bottomPx - 16f))
                            drawCircle(Color(0xFF0284C7), radius = 10f, center = Offset(lineX, bottomPx - 16f))

                            // Center Handle Pill with bidirectional arrow
                            val centerY = topPx + heightPx * 0.5f
                            drawRoundRect(
                                color = if (isActive) Color(0xFF0284C7) else Color(0xDD0F172A),
                                topLeft = Offset(lineX - 18f, centerY - 14f),
                                size = Size(36f, 28f),
                                cornerRadius = CornerRadius(14f, 14f)
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(lineX - 18f, centerY - 14f),
                                size = Size(36f, 28f),
                                cornerRadius = CornerRadius(14f, 14f),
                                style = Stroke(width = 1.5f)
                            )

                            // Draw text pill with percentage
                            val pctText = "${String.format(Locale.US, "%.1f", line.positionFraction * 100)}%"
                            drawContext.canvas.nativeCanvas.drawText(
                                pctText,
                                lineX,
                                topPx - 10f,
                                android.graphics.Paint().apply {
                                    color = if (isActive) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.WHITE
                                    textSize = 24f
                                    isFakeBoldText = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        } else {
                            val lineY = topPx + line.positionFraction * heightPx
                            drawLine(activeLineColor, Offset(leftPx, lineY), Offset(rightPx, lineY), strokeWidth = activeWidth, pathEffect = lineStroke.pathEffect)

                            // Left & Right draggable grab knobs
                            drawCircle(Color.White, radius = 13f, center = Offset(leftPx + 16f, lineY))
                            drawCircle(Color(0xFF0284C7), radius = 10f, center = Offset(leftPx + 16f, lineY))

                            drawCircle(Color.White, radius = 13f, center = Offset(rightPx - 16f, lineY))
                            drawCircle(Color(0xFF0284C7), radius = 10f, center = Offset(rightPx - 16f, lineY))

                            // Center Handle Pill
                            val centerX = leftPx + widthPx * 0.5f
                            drawRoundRect(
                                color = if (isActive) Color(0xFF0284C7) else Color(0xDD0F172A),
                                topLeft = Offset(centerX - 14f, lineY - 18f),
                                size = Size(28f, 36f),
                                cornerRadius = CornerRadius(14f, 14f)
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerX - 14f, lineY - 18f),
                                size = Size(28f, 36f),
                                cornerRadius = CornerRadius(14f, 14f),
                                style = Stroke(width = 1.5f)
                            )

                            val pctText = "${String.format(Locale.US, "%.1f", line.positionFraction * 100)}%"
                            drawContext.canvas.nativeCanvas.drawText(
                                pctText,
                                leftPx - 10f,
                                lineY + 8f,
                                android.graphics.Paint().apply {
                                    color = if (isActive) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.WHITE
                                    textSize = 24f
                                    isFakeBoldText = true
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                }
                            )
                        }
                    }
                }

                LayoutType.GRID_CUSTOM -> {
                    drawUniformGrid(leftPx, topPx, widthPx, heightPx, rows.coerceIn(1, 10), cols.coerceIn(1, 10), gridColor, lineStroke)
                    if (showIndices) {
                        drawGridIndices(leftPx, topPx, widthPx, heightPx, rows.coerceIn(1, 10), cols.coerceIn(1, 10), perSliceConfigs, selectedPieceIndex)
                    }
                }

                else -> {
                    val r = layoutType.defaultRows
                    val c = layoutType.defaultCols
                    drawUniformGrid(leftPx, topPx, widthPx, heightPx, r, c, gridColor, lineStroke)
                    if (showIndices) {
                        drawGridIndices(leftPx, topPx, widthPx, heightPx, r, c, perSliceConfigs, selectedPieceIndex)
                    }
                }
            }

            // 4. Draw Active Snap Magnetic Guideline if active
            if (activeSnapPos != null) {
                val snapGuidelinePaint = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
                val snapColor = Color(0xFF22C55E)

                if (isSnapVertical) {
                    val sx = leftPx + activeSnapPos!! * widthPx
                    drawLine(snapColor, Offset(sx, 0f), Offset(sx, canvasH), strokeWidth = snapGuidelinePaint.width, pathEffect = snapGuidelinePaint.pathEffect)
                    val label = when (activeSnapPos) {
                        0.5f -> "50% 中心对齐"
                        0.618f -> "0.618 黄金分割"
                        0.333f, 0.667f -> "三分线磁吸"
                        0.25f, 0.75f -> "四等分吸附"
                        else -> "磁吸对齐"
                    }
                    drawContext.canvas.nativeCanvas.drawText(label, sx + 8f, topPx + 30f, android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#22C55E")
                        textSize = 24f
                        isFakeBoldText = true
                    })
                } else {
                    val sy = topPx + activeSnapPos!! * heightPx
                    drawLine(snapColor, Offset(0f, sy), Offset(canvasW, sy), strokeWidth = snapGuidelinePaint.width, pathEffect = snapGuidelinePaint.pathEffect)
                    val label = when (activeSnapPos) {
                        0.5f -> "50% 中心对齐"
                        0.618f -> "0.618 黄金分割"
                        0.333f, 0.667f -> "三分线磁吸"
                        0.25f, 0.75f -> "四等分吸附"
                        else -> "磁吸对齐"
                    }
                    drawContext.canvas.nativeCanvas.drawText(label, leftPx + 10f, sy - 8f, android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#22C55E")
                        textSize = 24f
                        isFakeBoldText = true
                    })
                }
            }

            // 5. Draw Eyedropper Loupe if active
            if (isEyedropperActive && eyedropperTouchPos != null) {
                val pos = eyedropperTouchPos!!
                val loupeRadius = 40f
                val loupeCenter = if (pos.y > canvasH * 0.35f) {
                    Offset(pos.x, pos.y - loupeRadius * 1.6f)
                } else {
                    Offset(pos.x, pos.y + loupeRadius * 1.6f)
                }

                drawLine(color = Color.White, start = pos, end = loupeCenter, strokeWidth = 3f)
                drawCircle(color = Color.White, radius = 8f, center = pos, style = Stroke(width = 2.5f))
                drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = loupeRadius + 4f, center = loupeCenter)
                drawCircle(color = Color.White, radius = loupeRadius + 2f, center = loupeCenter)
                drawCircle(color = gridLineColor, radius = loupeRadius, center = loupeCenter)
            }

            // 6. Draw Precision Dragging Loupe (精准拖拽放大镜气泡) when moving corners or split lines
            if (!isEyedropperActive && currentTouchPos != null && (draggingHandle != null || localActiveSplitLineId != null)) {
                val tPos = currentTouchPos!!
                val loupeRadius = 46f
                val loupeCenter = if (tPos.y > loupeRadius * 2.2f) {
                    Offset(tPos.x, tPos.y - loupeRadius * 1.8f)
                } else {
                    Offset(tPos.x, tPos.y + loupeRadius * 1.8f)
                }

                // Connect line and ring
                drawLine(color = Color.White.copy(alpha = 0.8f), start = tPos, end = loupeCenter, strokeWidth = 2f)
                drawCircle(color = Color(0xFF38BDF8), radius = 5f, center = tPos)
                drawCircle(color = Color.Black.copy(alpha = 0.75f), radius = loupeRadius + 4f, center = loupeCenter)
                drawCircle(color = Color.White, radius = loupeRadius + 2f, center = loupeCenter, style = Stroke(width = 2.5f))
                drawCircle(color = Color(0xFF0F172A), radius = loupeRadius, center = loupeCenter)

                // Crosshair in Loupe
                val chLen = 14f
                drawLine(Color(0xFF38BDF8), Offset(loupeCenter.x - chLen, loupeCenter.y), Offset(loupeCenter.x + chLen, loupeCenter.y), strokeWidth = 2f)
                drawLine(Color(0xFF38BDF8), Offset(loupeCenter.x, loupeCenter.y - chLen), Offset(loupeCenter.x, loupeCenter.y + chLen), strokeWidth = 2f)

                val loupeLabel = when (draggingHandle) {
                    "tl" -> "左上角点"
                    "tr" -> "右上角点"
                    "bl" -> "左下角点"
                    "br" -> "右下角点"
                    "body" -> "选框平移"
                    else -> if (localActiveSplitLineId != null) "线条微调" else "精准对齐"
                }
                drawContext.canvas.nativeCanvas.drawText(
                    loupeLabel,
                    loupeCenter.x,
                    loupeCenter.y + loupeRadius + 22f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 20f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                )
            }
        }
    }
}

private fun DrawScope.drawUniformGrid(
    x: Float, y: Float, w: Float, h: Float,
    rows: Int, cols: Int,
    color: Color,
    stroke: Stroke
) {
    if (cols > 1) {
        val colW = w / cols
        for (c in 1 until cols) {
            val lineX = x + c * colW
            drawLine(color, Offset(lineX, y), Offset(lineX, y + h), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
        }
    }
    if (rows > 1) {
        val rowH = h / rows
        for (r in 1 until rows) {
            val lineY = y + r * rowH
            drawLine(color, Offset(x, lineY), Offset(x + w, lineY), strokeWidth = stroke.width, pathEffect = stroke.pathEffect)
        }
    }
}

private fun DrawScope.drawGridIndices(
    x: Float, y: Float, w: Float, h: Float,
    rows: Int, cols: Int,
    perSliceConfigs: Map<Int, SliceCustomConfig>,
    selectedPieceIndex: Int?
) {
    val colW = w / cols
    val rowH = h / rows
    var idx = 1
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cellLeft = x + c * colW
            val cellTop = y + r * rowH
            val cellCenter = Offset(cellLeft + colW / 2, cellTop + rowH / 2)
            val config = perSliceConfigs[idx]
            val isSelected = selectedPieceIndex == idx

            // If tile is blanked out, draw subtle pattern and hollow indicator
            if (config != null && config.isBlank) {
                drawRect(
                    color = Color(config.blankColorHex).copy(alpha = 0.85f),
                    topLeft = Offset(cellLeft + 2f, cellTop + 2f),
                    size = Size(colW - 4f, rowH - 4f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    config.customText ?: "留白",
                    cellCenter.x,
                    cellCenter.y + 6f,
                    android.graphics.Paint().apply {
                        color = if (config.blankColorHex == 0xFFFFFFFF) android.graphics.Color.GRAY else android.graphics.Color.WHITE
                        textSize = 22f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // If tile is selected, draw glowing focus ring
            if (isSelected) {
                drawRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(cellLeft + 1f, cellTop + 1f),
                    size = Size(colW - 2f, rowH - 2f),
                    style = Stroke(width = 3.5f)
                )
            }

            drawIndexCircle(idx, cellCenter, config, isSelected)
            idx++
        }
    }
}

private fun DrawScope.drawIndexCircle(
    index: Int,
    center: Offset,
    config: SliceCustomConfig? = null,
    isSelected: Boolean = false
) {
    val circleRadius = if (isSelected) 18f else 16f
    val bgColor = if (isSelected) Color(0xFF0284C7) else Color(0xCC000000)
    val ringColor = if (isSelected) Color(0xFF38BDF8) else Color.White

    drawCircle(
        color = bgColor,
        radius = circleRadius,
        center = center
    )
    drawCircle(
        color = ringColor,
        radius = circleRadius,
        center = center,
        style = Stroke(width = 2f)
    )

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = if (isSelected) 22f else 20f
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    val textY = center.y - ((paint.descent() + paint.ascent()) / 2)
    drawContext.canvas.nativeCanvas.drawText(index.toString(), center.x, textY, paint)
}

private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
