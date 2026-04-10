package top.xjunz.tasker.task.ocr

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * OCR 识别引擎统一接口
 * 支持本地（ML Kit）和云端（可插拔）两种实现
 */
interface OcrProvider {

    /**
     * 引擎名称（用于 UI 展示和日志）
     */
    val name: String

    /**
     * 当前引擎是否可用
     */
    fun isAvailable(): Boolean

    /**
     * 对 Bitmap 进行 OCR 文字识别
     * @param bitmap 输入图片
     * @param region 识别区域（null 为全图）
     * @return OcrResult 或 null（识别失败）
     */
    suspend fun recognize(bitmap: Bitmap, region: Rect? = null): OcrResult?

    /**
     * 释放资源
     */
    fun release()
}
