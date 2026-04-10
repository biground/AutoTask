package top.xjunz.tasker.task.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * MlKitOcrProvider 单元测试
 *
 * 注意：TextRecognizer / Bitmap 等 Android SDK 类在 JVM 测试中返回默认值
 * （testOptions.unitTests.returnDefaultValues = true），
 * 因此只测试纯逻辑和接口 contract。
 */
class MlKitOcrProviderTest {

    private lateinit var provider: MlKitOcrProvider

    @Before
    fun setUp() {
        provider = MlKitOcrProvider()
    }

    // ---- 属性测试 ----

    @Test
    fun `name 返回 ML Kit (Offline)`() {
        assertEquals("ML Kit (Offline)", provider.name)
    }

    @Test
    fun `isAvailable 始终返回 true`() {
        assertTrue(provider.isAvailable())
    }

    // ---- OcrProvider 接口 contract 测试 ----

    @Test
    fun `provider 实现 OcrProvider 接口`() {
        assertTrue(provider is OcrProvider)
    }

    @Test
    fun `name 不为空`() {
        assertTrue(provider.name.isNotBlank())
    }

    // ---- release 安全性 ----

    @Test
    fun `release 不抛异常`() {
        // 由于 returnDefaultValues=true，recognizer.close() 不会崩溃
        provider.release()
    }

    @Test
    fun `多次 release 不崩溃`() {
        provider.release()
        provider.release()
    }

    // ---- OcrResult / TextBlock 数据类验证 ----

    @Test
    fun `OcrResult 数据类构造正确`() {
        val result = OcrResult(
            fullText = "测试文本",
            blocks = emptyList(),
            processingTimeMs = 42
        )
        assertEquals("测试文本", result.fullText)
        assertTrue(result.blocks.isEmpty())
        assertEquals(42, result.processingTimeMs)
    }
}
