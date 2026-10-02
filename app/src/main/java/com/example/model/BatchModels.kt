package com.example.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Status of each item in the batch queue
 */
enum class BatchItemStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

/**
 * Represents a single image item in a batch operation
 */
data class BatchImageItem(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val title: String = "图片",
    val status: BatchItemStatus = BatchItemStatus.PENDING,
    val slicePieces: List<SlicePiece> = emptyList()
)

/**
 * Complete batch result group
 */
data class BatchProcessResult(
    val totalImages: Int,
    val totalPieces: Int,
    val items: List<BatchImageItem>,
    val timestamp: Long = System.currentTimeMillis()
)
