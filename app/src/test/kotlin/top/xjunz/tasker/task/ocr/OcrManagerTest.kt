package top.xjunz.tasker.task.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * OcrManager 单元测试
 *
 * 使用 Fake 实现替代真实的 ScreenshotProvider 和 OcrProvider，
 * 因为 JVM 测试环境下 Android SDK 返回默认值 (returnDefaultValues = true)
 */
class OcrManagerTest {

    companion object {
        /** 通过 Unsafe 创建 Bitmap 实例（JVM 测试中 Bitmap.createBitmap 返回 null） */
        private fun createFakeBitmap(): Bitmap {
            val unsafeClass = Class.forName("sun.misc.Unsafe")
            val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
            unsafeField.isAccessible = true
            val unsafe = unsafeField.get(null)
            val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
            return allocateInstance.invoke(unsafe, Bitmap::class.java) as Bitmap
        }
    }

    // ---- Fakes ----

    private class FakeScreenshotProvider(
        private var bitmapToReturn: Bitmap? = createFakeBitmap()
    ) : ScreenshotProvider {
        var takeScreenshotCalled = false
        override suspend fun takeScreenshot(crop: Rect?): Bitmap? {
            takeScreenshotCalled = true
            return bitmapToReturn
        }
    }

    private class FakeOcrProvider(
        private var resultToReturn: OcrResult? = OcrResult("fake", emptyList(), 10)
    ) : OcrProvider {
        override val name: String = "FakeOcr"
        var recognizeCalled = false
        var releaseCalled = false
        var lastBitmap: Bitmap? = null

        override fun isAvailable(): Boolean = true

        override suspend fun recognize(bitmap: Bitmap, region: Rect?): OcrResult? {
            recognizeCalled = true
            lastBitmap = bitmap
            return resultToReturn
        }

        override fun release() {
            releaseCalled = true
        }
    }

    // ---- 重置单例状态 ----

    @Before
    fun setUp() {
        OcrManager.release()
    }

    @After
    fun tearDown() {
        OcrManager.release()
    }

    // ---- 1. init() 设置 screenshotProvider ----

    @Test
    fun `init 设置 screenshotProvider 后 recognizeScreen 可执行`() = runTest {
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider()
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        val result = OcrManager.recognizeScreen()
        assertNotNull(result)
        assertTrue(sp.takeScreenshotCalled)
    }

    // ---- 2. recognizeScreen() 成功执行截图→识别管线 ----

    @Test
    fun `recognizeScreen 成功返回 OcrResult`() = runTest {
        val expectedResult = OcrResult("hello", emptyList(), 5)
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider(expectedResult)
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        val result = OcrManager.recognizeScreen()
        assertNotNull(result)
        assertEquals("hello", result!!.fullText)
        assertTrue(fakeOcr.recognizeCalled)
    }

    // ---- 3. recognizeScreen() 未 init 时返回 null ----

    @Test
    fun `recognizeScreen 未初始化 screenshotProvider 返回 null`() = runTest {
        val result = OcrManager.recognizeScreen()
        assertNull(result)
    }

    // ---- 4. recognizeScreen() 截图返回 null 时直接返回 null ----

    @Test
    fun `recognizeScreen 截图返回 null 时返回 null`() = runTest {
        val sp = FakeScreenshotProvider(bitmapToReturn = null)
        val fakeOcr = FakeOcrProvider()
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        val result = OcrManager.recognizeScreen()
        assertNull(result)
        // OCR 不应被调用
        assertTrue(!fakeOcr.recognizeCalled)
    }

    // ---- 5. recognizeScreen() 识别失败时返回 null ----

    @Test
    fun `recognizeScreen 识别失败返回 null`() = runTest {
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider(resultToReturn = null)
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        val result = OcrManager.recognizeScreen()
        assertNull(result)
        assertTrue(fakeOcr.recognizeCalled)
    }

    // ---- 6. getLastResult() TTL 内返回缓存 ----

    @Test
    fun `getLastResult TTL 内返回缓存结果`() = runTest {
        val expected = OcrResult("cached", emptyList(), 1)
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider(expected)
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        OcrManager.recognizeScreen()
        val cached = OcrManager.getLastResult()
        assertNotNull(cached)
        assertEquals("cached", cached!!.fullText)
    }

    // ---- 7. getLastResult() TTL 过期返回 null ----

    @Test
    fun `getLastResult TTL 过期返回 null`() = runTest {
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider()
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)

        OcrManager.recognizeScreen()
        // 利用反射修改 lastResultTimestamp 模拟过期
        val tsField = OcrManager::class.java.getDeclaredField("lastResultTimestamp")
        tsField.isAccessible = true
        tsField.setLong(OcrManager, System.currentTimeMillis() - 3000)

        val cached = OcrManager.getLastResult()
        assertNull(cached)
    }

    // ---- 8. setProvider() 切换引擎并 release 旧引擎 ----

    @Test
    fun `setProvider 切换引擎并释放旧引擎`() {
        val oldProvider = FakeOcrProvider()
        val newProvider = FakeOcrProvider()
        OcrManager.setProvider(oldProvider)
        OcrManager.setProvider(newProvider)

        assertTrue(oldProvider.releaseCalled)
        assertSame(newProvider, OcrManager.getProvider())
    }

    // ---- 9. release() 清理状态 ----

    @Test
    fun `release 清理状态`() = runTest {
        val sp = FakeScreenshotProvider()
        val fakeOcr = FakeOcrProvider()
        OcrManager.init(sp)
        OcrManager.setProvider(fakeOcr)
        OcrManager.recognizeScreen()

        OcrManager.release()
        assertTrue(fakeOcr.releaseCalled)
        assertNull(OcrManager.getLastResult())
    }
}
