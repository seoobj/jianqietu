package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ImageFilterPreset
import com.example.model.SliceCustomConfig
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SliceMicroAdjustDialog(
    pieceIndex: Int,
    config: SliceCustomConfig,
    onConfigChange: (SliceCustomConfig) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var isBlank by remember(config) { mutableStateOf(config.isBlank) }
    var panX by remember(config) { mutableFloatStateOf(config.panOffsetX) }
    var panY by remember(config) { mutableFloatStateOf(config.panOffsetY) }
    var zoom by remember(config) { mutableFloatStateOf(config.zoomScale) }
    var rotation by remember(config) { mutableFloatStateOf(config.rotationAngle) }
    var flipH by remember(config) { mutableStateOf(config.flipH) }
    var filterOverride by remember(config) { mutableStateOf(config.filterOverride) }
    var customText by remember(config) { mutableStateOf(config.customText ?: "") }
    var blankColorHex by remember(config) { mutableStateOf(config.blankColorHex) }

    fun commit(
        newBlank: Boolean = isBlank,
        newPanX: Float = panX,
        newPanY: Float = panY,
        newZoom: Float = zoom,
        newRotation: Float = rotation,
        newFlipH: Boolean = flipH,
        newFilter: ImageFilterPreset? = filterOverride,
        newText: String = customText,
        newColor: Long = blankColorHex
    ) {
        onConfigChange(
            SliceCustomConfig(
                isBlank = newBlank,
                blankColorHex = newColor,
                panOffsetX = newPanX,
                panOffsetY = newPanY,
                zoomScale = newZoom,
                rotationAngle = newRotation,
                flipH = newFlipH,
                filterOverride = newFilter,
                customText = if (newText.isNotBlank()) newText.trim() else null
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$pieceIndex",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "第 $pieceIndex 格 · 单片精细微调",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "独立构图、留白、缩放与专属风格",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Hollow / Blank Tile Toggle (留白 / 镂空卡片)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isBlank) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isBlank) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CropFree,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "单格设为留白 / 镂空",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "中心或角落留白，打造朋友圈呼吸感排版",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isBlank,
                                onCheckedChange = {
                                    isBlank = it
                                    commit(newBlank = it)
                                }
                            )
                        }

                        if (isBlank) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "选择留白底色：",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val colors = listOf(
                                    0xFFFFFFFF to "纯白",
                                    0xFF000000 to "纯黑",
                                    0xFFF7F3E9 to "暖杏",
                                    0xFFE8F5E9 to "薄荷",
                                    0xFF0B1120 to "深蓝"
                                )
                                colors.forEach { (hex, label) ->
                                    val isSelected = blankColorHex == hex
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(hex),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp)
                                            .clickable {
                                                blankColorHex = hex
                                                commit(newColor = hex)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hex == 0xFFFFFFFF || hex == 0xFFF7F3E9 || hex == 0xFFE8F5E9) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Micro Alignment (平移 & 局部缩放)
                if (!isBlank) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "单片构图微调",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        panX = 0f
                                        panY = 0f
                                        zoom = 1.0f
                                        commit(newPanX = 0f, newPanY = 0f, newZoom = 1.0f)
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.CenterFocusStrong, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("居中复位", fontSize = 11.sp)
                                }
                            }

                            // Zoom Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("局部缩放", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.2f", zoom)}x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = zoom,
                                onValueChange = {
                                    zoom = it
                                    commit(newZoom = it)
                                },
                                valueRange = 0.8f..2.5f,
                                modifier = Modifier.height(28.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Horizontal Pan Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("水平微调 (X)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (panX > 0) "+${(panX * 100).toInt()}%" else "${(panX * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = panX,
                                onValueChange = {
                                    panX = it
                                    commit(newPanX = it)
                                },
                                valueRange = -0.35f..0.35f,
                                modifier = Modifier.height(28.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Vertical Pan Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("垂直微调 (Y)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (panY > 0) "+${(panY * 100).toInt()}%" else "${(panY * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = panY,
                                onValueChange = {
                                    panY = it
                                    commit(newPanY = it)
                                },
                                valueRange = -0.35f..0.35f,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Rotation & Flip per slice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val nextRot = (rotation + 90f) % 360f
                                rotation = nextRot
                                commit(newRotation = nextRot)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (rotation > 0) "旋转 ${rotation.toInt()}°" else "单片旋转", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val nextFlip = !flipH
                                flipH = nextFlip
                                commit(newFlipH = nextFlip)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = if (flipH) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Flip, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (flipH) "已镜像翻转" else "水平镜像", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Per-slice Filter Override
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ColorLens, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("单片独立滤镜（营造冷暖/黑白对比）", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = filterOverride == null,
                                    onClick = {
                                        filterOverride = null
                                        commit(newFilter = null)
                                    },
                                    label = { Text("跟随全局", fontSize = 11.sp) }
                                )
                                ImageFilterPreset.values().forEach { preset ->
                                    FilterChip(
                                        selected = filterOverride == preset,
                                        onClick = {
                                            filterOverride = preset
                                            commit(newFilter = preset)
                                        },
                                        label = { Text(preset.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Sticker / Custom Text Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TextFields, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("单片文字 / 表情贴纸", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customText,
                            onValueChange = {
                                customText = it
                                commit(newText = it)
                            },
                            placeholder = { Text("输入心愿/日期/表情 (如 ❤️, VIBE, 2026)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Quick Emoji / Tag chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("❤️", "✨", "DAILY", "2026", "VIBE", "PHOTO").forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clickable {
                                            customText = tag
                                            commit(newText = tag)
                                        }
                                        .padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重置单格", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                    ) {
                        Text("应用并完成", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
