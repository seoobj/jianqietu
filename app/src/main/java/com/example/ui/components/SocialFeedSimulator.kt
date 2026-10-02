package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SlicePiece

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocialFeedSimulatorView(
    pieces: List<SlicePiece>,
    modifier: Modifier = Modifier
) {
    var simulatorTab by remember { mutableIntStateOf(0) } // 0: 微信朋友圈实景, 1: 小红书轮播
    var postContent by remember { mutableStateOf("九宫格大图打卡✨ 连起来看太有视觉冲击力了，分享给各位摄影与生活爱好者！📸 #九宫格切图 #美图分享") }
    var nickname by remember { mutableStateOf("摄影生活家") }
    var inspectingIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Platform Mode Selector Tab
        TabRow(
            selectedTabIndex = simulatorTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = simulatorTab == 0,
                onClick = { simulatorTab = 0 },
                text = { Text("💬 微信朋友圈 1:1 实机模拟", fontSize = 13.sp, fontWeight = if (simulatorTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = simulatorTab == 1,
                onClick = { simulatorTab = 1 },
                text = { Text("📕 小红书多图轮播模拟", fontSize = 13.sp, fontWeight = if (simulatorTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        // Copy Text & Tips Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 点击任意切片可全屏模拟查看，确认发圈无缝对齐效果",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Post Content", postContent))
                        Toast.makeText(context, "发帖文案已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("复制文案", fontSize = 11.sp)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (simulatorTab == 0) Color(0xFFF3F3F3) else Color(0xFFF8F9FA))
        ) {
            if (simulatorTab == 0) {
                // === 微信朋友圈 1:1 实机模拟 ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    // WeChat Post Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF07C160),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📸", fontSize = 20.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                // Nickname & Tag
                                Text(
                                    text = nickname,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF576B95) // WeChat Blue link color
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Post Content
                                Text(
                                    text = postContent,
                                    fontSize = 14.sp,
                                    color = Color(0xFF181818),
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 9-Grid / 4-Grid Media Layout
                                val gridCols = if (pieces.size == 4) 2 else if (pieces.size == 1) 1 else 3
                                val gridWidth = if (pieces.size == 4) 200.dp else 260.dp

                                Box(modifier = Modifier.width(gridWidth)) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(gridCols),
                                        horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.5.dp),
                                        modifier = Modifier.fillMaxWidth().height(
                                            if (pieces.size <= 3) 88.dp else if (pieces.size <= 6) 176.dp else 260.dp
                                        ),
                                        userScrollEnabled = false
                                    ) {
                                        itemsIndexed(pieces) { index, piece ->
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .clickable { inspectingIndex = index }
                                            ) {
                                                if (piece.bitmap != null) {
                                                    Image(
                                                        bitmap = piece.bitmap.asImageBitmap(),
                                                        contentDescription = "第 ${index + 1} 张",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                                // Mini Order pill
                                                Surface(
                                                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Time and WeChat interaction bubble
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "刚刚 · 朋友圈实机模拟",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8C8C8C)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF7F7F7),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                tint = Color(0xFF576B95),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ModeComment,
                                                contentDescription = null,
                                                tint = Color(0xFF576B95),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // === 小红书 3:4 轮播画廊滑动模拟 ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .weight(1f)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Red Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFF2442),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("RED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("小红书画廊", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFF2442).copy(alpha = 0.1f),
                                    modifier = Modifier.clickable {}
                                ) {
                                    Text(
                                        text = "+ 关注",
                                        color = Color(0xFFFF2442),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Carousel Swipe Area (Horizontal Pager)
                            val pagerState = rememberPagerState(pageCount = { pieces.size })
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF0F172A))
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    val piece = pieces.getOrNull(page)
                                    if (piece?.bitmap != null) {
                                        Image(
                                            bitmap = piece.bitmap.asImageBitmap(),
                                            contentDescription = "切片 ${page + 1}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Page Indicator Pill (e.g. 1/9)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / ${pieces.size}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Red Bottom Interaction Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FavoriteBorder, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("3.2k", fontSize = 11.sp, color = Color(0xFF666666))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("1.8k", fontSize = 11.sp, color = Color(0xFF666666))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ModeComment, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("642", fontSize = 11.sp, color = Color(0xFF666666))
                                    }
                                }
                                Icon(Icons.Default.Share, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen WeChat Photo Viewer Dialog
    if (inspectingIndex != null) {
        val curIdx = inspectingIndex!!
        val piece = pieces.getOrNull(curIdx)
        Dialog(
            onDismissRequest = { inspectingIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Top Bar with 1/9 indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { inspectingIndex = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                    Text(
                        text = "第 ${curIdx + 1} / ${pieces.size} 张",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { inspectingIndex = null }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                    }
                }

                // Center Image
                if (piece?.bitmap != null) {
                    Image(
                        bitmap = piece.bitmap.asImageBitmap(),
                        contentDescription = "第 ${curIdx + 1} 张大图",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .align(Alignment.Center)
                    )
                }

                // Bottom Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { inspectingIndex = (curIdx - 1 + pieces.size) % pieces.size },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626))
                    ) {
                        Text("上一张", color = Color.White)
                    }
                    Button(
                        onClick = { inspectingIndex = (curIdx + 1) % pieces.size },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160))
                    ) {
                        Text("下一张", color = Color.White)
                    }
                }
            }
        }
    }
}
