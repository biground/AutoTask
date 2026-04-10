package top.xjunz.tasker.task.ocr

import android.graphics.Rect

/**
 * OCR 识别结果
 */
data class OcrResult(
    val fullText: String,
    val blocks: List<TextBlock>,
    val processingTimeMs: Long = 0
)

/**
 * 单个文本块
 */
data class TextBlock(
    val text: String,
    val boundingBox: Rect,
    val confidence: Float
)

/**
 * OCR 文字匹配模式
 */
enum class OcrTextMatchMode {
    CONTAINS,  // 包含匹配
    REGEX,     // 正则匹配
    EXACT      // 精确匹配
}
