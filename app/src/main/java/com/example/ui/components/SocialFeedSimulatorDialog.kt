package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SlicePiece
import com.example.model.SocialSimulatorPlatform

@Composable
fun SocialFeedSimulatorDialog(
    pieces: List<SlicePiece>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlatform by remember { mutableStateOf(SocialSimulatorPlatform.WECHAT_MOMENTS) }
    var authorName by remember { mutableStateOf("花艺美学研习社") }
    var postContent by remember { mutableStateOf("今日花艺手作 🌸 每一格拼图都有独特的绽放光芒～") }

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
                // Top Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📱 社媒发圈实景仿真器",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "发图前 100% 确认无缝对齐与缩略图裁切",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Platform Selector Tabs
                TabRow(
                    selectedTabIndex = selectedPlatform.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    SocialSimulatorPlatform.values().forEach { platform ->
                        Tab(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            text = {
                                Text(
                                    text = platform.platformName.substringBefore("实景"),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedPlatform == platform) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulation Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when (selectedPlatform) {
                                SocialSimulatorPlatform.WECHAT_MOMENTS -> Color(0xFFF7F7F7)
                                SocialSimulatorPlatform.RED_CAROUSEL -> Color(0xFFFFFFFF)
                                SocialSimulatorPlatform.WEIBO_FEED -> Color(0xFFF2F2F2)
                                SocialSimulatorPlatform.INSTAGRAM_GRID -> Color(0xFF000000)
                            }
                        )
                        .padding(12.dp)
                ) {
                    when (selectedPlatform) {
                        SocialSimulatorPlatform.WECHAT_MOMENTS -> {
                            WeChatMomentsView(authorName, postContent, pieces)
                        }
                        SocialSimulatorPlatform.RED_CAROUSEL -> {
                            RedCarouselView(authorName, postContent, pieces)
                        }
                        SocialSimulatorPlatform.WEIBO_FEED -> {
                            WeiboFeedView(authorName, postContent, pieces)
                        }
                        SocialSimulatorPlatform.INSTAGRAM_GRID -> {
                            InstagramGridView(pieces)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("完成预览")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeChatMomentsView(author: String, content: String, pieces: List<SlicePiece>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF576B95)),
                contentAlignment = Alignment.Center
            ) {
                Text(author.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = author,
                    color = Color(0xFF576B95),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    color = Color(0xFF1E293B),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Moments Grid: 4-grid is 2x2, 9-grid is 3x3
                val gridCols = if (pieces.size == 4) 2 else 3
                val gridGap = 4.dp
                val validPieces = pieces.filter { !it.isDecorative }

                Column(
                    verticalArrangement = Arrangement.spacedBy(gridGap),
                    modifier = Modifier.fillMaxWidth(if (gridCols == 2) 0.72f else 0.98f)
                ) {
                    val rows = validPieces.chunked(gridCols)
                    for (row in rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(gridGap),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (piece in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    if (piece.bitmap != null) {
                                        Image(
                                            bitmap = piece.bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            // Fill empty slots in row if uneven
                            if (row.size < gridCols) {
                                for (i in 0 until (gridCols - row.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("10分钟前 · 朋友圈", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.MoreHoriz, null, tint = Color(0xFF576B95), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RedCarouselView(author: String, content: String, pieces: List<SlicePiece>) {
    val validPieces = pieces.filter { !it.isDecorative }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Horizontal Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            validPieces.forEachIndexed { idx, piece ->
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    if (piece.bitmap != null) {
                        Image(
                            bitmap = piece.bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Page indicator pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${idx + 1}/${validPieces.size}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "✨ $content",
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "#多图拼图 #无缝连贯滑动画报 #生活碎片",
            color = Color(0xFF1D4ED8),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun WeiboFeedView(author: String, content: String, pieces: List<SlicePiece>) {
    WeChatMomentsView(author, content, pieces)
}

@Composable
private fun InstagramGridView(pieces: List<SlicePiece>) {
    val validPieces = pieces.filter { !it.isDecorative }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("INSTAGRAM FEED GRID", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${validPieces.size} POSTS", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(validPieces) { piece ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color(0xFF1E293B))
                ) {
                    if (piece.bitmap != null) {
                        Image(
                            bitmap = piece.bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
