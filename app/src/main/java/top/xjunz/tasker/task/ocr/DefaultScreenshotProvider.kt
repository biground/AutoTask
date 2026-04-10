/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.ocr

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import top.xjunz.tasker.service.A11yAutomatorService
import top.xjunz.tasker.service.OperatingMode
import top.xjunz.tasker.service.uiAutomation
import kotlin.coroutines.resume

/**
 * 默认截图提供者，根据当前运行模式自动选择 Shizuku 或 A11y 截图路径
 */
class DefaultScreenshotProvider : ScreenshotProvider {

    companion object {
        /** 截图超时时间（毫秒） */
        private const val SCREENSHOT_TIMEOUT_MS = 5000L
    }

    override suspend fun takeScreenshot(crop: Rect?): Bitmap? {
        val raw = withTimeoutOrNull(SCREENSHOT_TIMEOUT_MS) {
            when (OperatingMode.CURRENT) {
                is OperatingMode.Privilege -> takeScreenshotViaShizuku()
                is OperatingMode.Accessibility -> takeScreenshotViaA11y()
            }
        } ?: return null
        return cropBitmap(raw, crop)
    }

    /**
     * Shizuku 模式：通过 UiAutomation.takeScreenshot() 同步截图
     */
    private suspend fun takeScreenshotViaShizuku(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            uiAutomation.takeScreenshot()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * A11y 模式：通过 AccessibilityService.takeScreenshot() 回调截图（Android 11+）
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun takeScreenshotViaA11y(): Bitmap? {
        val service = A11yAutomatorService.get() ?: return null
        return suspendCancellableCoroutine { cont ->
            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                Dispatchers.Main.asExecutor(),
                object : AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                        val bitmap = try {
                            result.hardwareBuffer.use { buffer ->
                                Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                            }
                        } catch (_: Exception) {
                            null
                        }
                        if (cont.isActive) cont.resume(bitmap)
                    }

                    override fun onFailure(errorCode: Int) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            )
        }
    }
}

/**
 * 安全裁剪 Bitmap，确保 crop 不超出 bitmap 边界
 * @return 裁剪后的 Bitmap，crop 为 null 时返回原图
 */
internal fun cropBitmap(source: Bitmap, crop: Rect?): Bitmap {
    if (crop == null) return source
    val safe = computeSafeCropRect(source.width, source.height,
        crop.left, crop.top, crop.right, crop.bottom) ?: return source
    return Bitmap.createBitmap(source, safe[0], safe[1], safe[2] - safe[0], safe[3] - safe[1])
}

/**
 * 计算安全的裁剪区域，确保不超出 (bitmapWidth x bitmapHeight) 边界
 * @return IntArray [left, top, right, bottom]，如果交集为空则返回 null
 */
internal fun computeSafeCropRect(
    bitmapWidth: Int, bitmapHeight: Int,
    cropLeft: Int, cropTop: Int, cropRight: Int, cropBottom: Int
): IntArray? {
    val left = maxOf(cropLeft, 0)
    val top = maxOf(cropTop, 0)
    val right = minOf(cropRight, bitmapWidth)
    val bottom = minOf(cropBottom, bitmapHeight)
    if (left >= right || top >= bottom) return null
    return intArrayOf(left, top, right, bottom)
}
