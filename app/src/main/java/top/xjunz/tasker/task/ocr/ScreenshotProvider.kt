/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.ocr

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * 统一截图接口，封装不同运行模式下的截图实现
 */
interface ScreenshotProvider {
    /**
     * 截取屏幕截图
     * @param crop 裁剪区域（null 为全屏）
     * @return Bitmap 或 null（截图失败）
     */
    suspend fun takeScreenshot(crop: Rect? = null): Bitmap?
}
