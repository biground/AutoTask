/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.ocr

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenshotProviderTest {

    // --- computeSafeCropRect 裁剪边界计算测试 ---

    @Test
    fun computeSafeCropRect_validCrop_returnsSameRect() {
        val result = computeSafeCropRect(100, 200, 10, 20, 60, 120)
        assertNotNull(result)
        assertArrayEquals(intArrayOf(10, 20, 60, 120), result)
    }

    @Test
    fun computeSafeCropRect_cropExceedsBounds_clampsToIntersection() {
        // crop 部分超出右下角 (50,50,200,200) → clamp 到 (50,50,100,100)
        val result = computeSafeCropRect(100, 100, 50, 50, 200, 200)
        assertNotNull(result)
        assertArrayEquals(intArrayOf(50, 50, 100, 100), result)
    }

    @Test
    fun computeSafeCropRect_cropCompletelyOutside_returnsNull() {
        val result = computeSafeCropRect(100, 100, 200, 200, 300, 300)
        assertNull(result)
    }

    @Test
    fun computeSafeCropRect_cropExceedsTopLeft_clampsToIntersection() {
        // crop 从负坐标开始 (-50,-50,30,40) → clamp 到 (0,0,30,40)
        val result = computeSafeCropRect(100, 100, -50, -50, 30, 40)
        assertNotNull(result)
        assertArrayEquals(intArrayOf(0, 0, 30, 40), result)
    }

    @Test
    fun computeSafeCropRect_emptyCrop_returnsNull() {
        // left == right → 宽为 0
        val result = computeSafeCropRect(100, 100, 50, 50, 50, 50)
        assertNull(result)
    }

    @Test
    fun computeSafeCropRect_fullSize_returnsSameDimensions() {
        val result = computeSafeCropRect(100, 200, 0, 0, 100, 200)
        assertNotNull(result)
        assertArrayEquals(intArrayOf(0, 0, 100, 200), result)
    }

    @Test
    fun computeSafeCropRect_zeroBitmapDimensions_returnsNull() {
        val result = computeSafeCropRect(0, 0, 0, 0, 50, 50)
        assertNull(result)
    }

    @Test
    fun computeSafeCropRect_allSidesExceed_clampsCorrectly() {
        // 所有方向都超出：(-10,-20,150,250) 在 100x200 → (0,0,100,200)
        val result = computeSafeCropRect(100, 200, -10, -20, 150, 250)
        assertNotNull(result)
        assertArrayEquals(intArrayOf(0, 0, 100, 200), result)
    }

    // --- 接口契约验证 ---

    @Test
    fun screenshotProvider_interface_hasTakeScreenshotMethod() {
        val methods = ScreenshotProvider::class.java.declaredMethods
        val takeScreenshot = methods.find { it.name == "takeScreenshot" }
        assertNotNull("ScreenshotProvider 应定义 takeScreenshot 方法", takeScreenshot)
    }
}
