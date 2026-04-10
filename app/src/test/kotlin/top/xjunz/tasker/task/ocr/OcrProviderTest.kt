package top.xjunz.tasker.task.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OcrProviderTest {

    /** 可控的假实现 */
    private class FakeOcrProvider(
        override val name: String = "FakeOCR",
        private var available: Boolean = true,
        private var result: OcrResult? = null,
        private var released: Boolean = false
    ) : OcrProvider {

        var recognizeCalled = false
            private set
        var lastRegion: Rect? = null
            private set

        override fun isAvailable(): Boolean = available

        override suspend fun recognize(bitmap: Bitmap, region: Rect?): OcrResult? {
            recognizeCalled = true
            lastRegion = region
            return if (released) null else result
        }

        override fun release() {
            released = true
        }

        fun setAvailable(value: Boolean) { available = value }
        fun setResult(value: OcrResult?) { result = value }
    }

    private lateinit var provider: FakeOcrProvider

    @Before
    fun setUp() {
        provider = FakeOcrProvider()
    }

    // --- 接口方法签名验证 ---

    @Test
    fun `name 返回引擎名称`() {
        assertEquals("FakeOCR", provider.name)
    }

    @Test
    fun `isAvailable 返回当前可用状态`() {
        assertTrue(provider.isAvailable())
        provider.setAvailable(false)
        assertFalse(provider.isAvailable())
    }

    @Test
    fun `recognize 可正常调用并返回结果`() = runBlocking {
        val expected = OcrResult(
            fullText = "Hello",
            blocks = listOf(TextBlock("Hello", Rect(0, 0, 50, 20), 0.95f)),
            processingTimeMs = 42
        )
        provider.setResult(expected)

        val actual = provider.recognize(stubBitmap())
        assertTrue(provider.recognizeCalled)
        assertEquals(expected, actual)
    }

    @Test
    fun `recognize 支持指定识别区域`() = runBlocking {
        val expected = OcrResult("区域", emptyList(), 10)
        provider.setResult(expected)
        val region = Rect(10, 10, 50, 50)

        val result = provider.recognize(stubBitmap(), region)
        assertEquals(expected, result)
        // stub android.jar 的 Rect.equals 是引用比较，用 assertSame 验证参数透传
        assertSame(region, provider.lastRegion)
    }

    // --- 接口 contract 验证 ---

    @Test
    fun `isAvailable 为 false 时 recognize 仍可调用但返回 null`() = runBlocking {
        provider.setAvailable(false)
        provider.setResult(null)

        val result = provider.recognize(stubBitmap())
        assertTrue(provider.recognizeCalled)
        assertNull(result)
    }

    @Test
    fun `release 后 recognize 返回 null`() = runBlocking {
        provider.setResult(OcrResult("text", emptyList(), 5))

        provider.release()
        val result = provider.recognize(stubBitmap())
        assertNull(result)
    }

    @Test
    fun `release 可安全多次调用`() {
        provider.release()
        provider.release() // 不应抛异常
    }

    companion object {
        /**
         * 在 returnDefaultValues=true 的 JVM 测试中，
         * Bitmap 静态方法返回 null。通过 Java 反射绕过 Kotlin 空安全。
         */
        @JvmStatic
        private fun stubBitmap(): Bitmap =
            Bitmap::class.java.getDeclaredConstructor().let { ctor ->
                ctor.isAccessible = true
                ctor.newInstance()
            }
    }
}
