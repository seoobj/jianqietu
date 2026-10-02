package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.LineStyle
import com.example.model.MagazinePosition
import com.example.model.MagazineTemplate
import com.example.model.MagazineTypographyConfig
import com.example.model.PosterBackgroundConfig
import com.example.model.PosterBgStyle
import com.example.model.PosterTexture
import com.example.model.TileShadowStyle

@Composable
fun MagazineTypographyDialog(
    currentConfig: MagazineTypographyConfig,
    currentLineStyle: LineStyle,
    currentShadowStyle: TileShadowStyle,
    currentPosterBg: PosterBackgroundConfig,
    onSaveConfig: (
        magConfig: MagazineTypographyConfig,
        lineStyle: LineStyle,
        shadowStyle: TileShadowStyle,
        posterBg: PosterBackgroundConfig
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(currentConfig.isEnabled) }
    var selectedTemplate by remember { mutableStateOf(currentConfig.template) }
    var mainTitle by remember { mutableStateOf(currentConfig.mainTitle) }
    var subTitle by remember { mutableStateOf(currentConfig.subTitle) }
    var dateText by remember { mutableStateOf(currentConfig.dateText) }
    var locationText by remember { mutableStateOf(currentConfig.locationText) }
    var selectedPosition by remember { mutableStateOf(currentConfig.position) }

    var selectedLineStyle by remember { mutableStateOf(currentLineStyle) }
    var selectedShadowStyle by remember { mutableStateOf(currentShadowStyle) }
    var selectedBgStyle by remember { mutableStateOf(currentPosterBg.style) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatPaint, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "海报质感与杂志排印设置",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section 1: Line Style (切线风格)
                    Text("1. 切割线条质感", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LineStyle.values()) { style ->
                            val isSelected = selectedLineStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLineStyle = style },
                                label = { Text(style.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    // Section 2: 3D Tile Shadows (立体阴影)
                    Text("2. 3D 卡片立体悬浮阴影", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TileShadowStyle.values()) { shadow ->
                            val isSelected = selectedShadowStyle == shadow
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedShadowStyle = shadow },
                                label = { Text(shadow.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }

                    // Section 3: Poster Background (画框底板)
                    Text("3. 海报外框底板风格", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PosterBgStyle.values()) { bg ->
                            val isSelected = selectedBgStyle == bg
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBgStyle = bg },
                                label = { Text(bg.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    // Section 4: Magazine Typography Stamp (杂志封面排印)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TextFields, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("4. 开启杂志风排印与日期印章", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                            }

                            if (isEnabled) {
                                Text("选择排印预设：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(MagazineTemplate.values()) { tmpl ->
                                        val isSel = selectedTemplate == tmpl
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedTemplate = tmpl
                                                mainTitle = tmpl.defaultMain
                                                subTitle = tmpl.defaultSub
                                                locationText = tmpl.defaultTag
                                            },
                                            label = { Text(tmpl.templateName, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = mainTitle,
                                    onValueChange = { mainTitle = it },
                                    label = { Text("主标题 (Main Title)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = subTitle,
                                    onValueChange = { subTitle = it },
                                    label = { Text("副标题 / 期刊志 (Sub Title)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = dateText,
                                        onValueChange = { dateText = it },
                                        label = { Text("日期印章") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = locationText,
                                        onValueChange = { locationText = it },
                                        label = { Text("地点/工坊") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text("印章排版位置：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(MagazinePosition.values()) { pos ->
                                        FilterChip(
                                            selected = selectedPosition == pos,
                                            onClick = { selectedPosition = pos },
                                            label = { Text(pos.label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            val newMag = currentConfig.copy(
                                isEnabled = isEnabled,
                                template = selectedTemplate,
                                mainTitle = mainTitle,
                                subTitle = subTitle,
                                dateText = dateText,
                                locationText = locationText,
                                position = selectedPosition
                            )
                            val newPosterBg = currentPosterBg.copy(
                                style = selectedBgStyle
                            )
                            onSaveConfig(newMag, selectedLineStyle, selectedShadowStyle, newPosterBg)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("应用海报样式")
                    }
                }
            }
        }
    }
}
