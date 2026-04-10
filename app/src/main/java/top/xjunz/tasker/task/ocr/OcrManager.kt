package top.xjunz.tasker.task.ocr

import android.graphics.Rect

/**
 * OCR 管线编排器（单例）
 *
 * 负责：截图 → OCR 识别 → 结果缓存
 * 支持引擎切换和 lastResult TTL 缓存
 */
object OcrManager {

    private var provider: OcrProvider = MlKitOcrProvider()
    private var screenshotProvider: ScreenshotProvider? = null

    @Volatile
    private var lastResult: OcrResult? = null
    @Volatile
    private var lastResultTimestamp: Long = 0

    private const val RESULT_TTL_MS = 2000L

    /**
     * 初始化截图提供者
     */
    fun init(screenshotProvider: ScreenshotProvider) {
        this.screenshotProvider = screenshotProvider
    }

    /**
     * 切换 OCR 引擎（自动释放旧引擎）
     */
    fun setProvider(provider: OcrProvider) {
        this.provider.release()
        this.provider = provider
    }

    /**
     * 获取当前 OCR 引擎
     */
    fun getProvider(): OcrProvider = provider

    /**
     * 执行截图 → OCR 识别管线
     * @param region 识别区域（null 为全屏）
     * @return 识别结果，失败返回 null
     */
    suspend fun recognizeScreen(region: Rect? = null): OcrResult? {
        val sp = screenshotProvider ?: return null
        val bitmap = sp.takeScreenshot(region) ?: return null
        return try {
            val result = provider.recognize(bitmap, null)
            if (result != null) {
                lastResult = result
                lastResultTimestamp = System.currentTimeMillis()
            }
            result
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * 获取上次识别结果（带 TTL 检查）
     * @return 2000ms 内的缓存结果，过期返回 null
     */
    fun getLastResult(): OcrResult? {
        if (System.currentTimeMillis() - lastResultTimestamp > RESULT_TTL_MS) {
            lastResult = null
            return null
        }
        return lastResult
    }

    /**
     * 释放资源
     */
    fun release() {
        provider.release()
        lastResult = null
        lastResultTimestamp = 0
        screenshotProvider = null
    }
}
