package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.ocr.OcrResult

class OcrRecognizeActionTest {

    private lateinit var runtime: TaskRuntime

    @Before
    fun setUp() {
        // 通过 Unsafe.allocateInstance 绕过私有构造器（apply 中未使用 runtime 字段）
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        runtime = allocateInstance.invoke(unsafe, TaskRuntime::class.java) as TaskRuntime
    }

    @Test
    fun `识别成功时返回 fullText`() = runTest {
        val expected = "你好世界"
        val ocrResult = OcrResult(
            fullText = expected,
            blocks = emptyList(),
            processingTimeMs = 120
        )
        val action = OcrRecognizeAction(recognizer = { ocrResult })

        val result = action.apply(runtime)

        assertTrue(result.isSuccessful)
        assertEquals(expected, result.returned)
    }

    @Test
    fun `识别失败（null）时返回 EMPTY_FAILURE`() = runTest {
        val action = OcrRecognizeAction(recognizer = { null })

        val result = action.apply(runtime)

        assertFalse(result.isSuccessful)
        assertSame(AppletResult.EMPTY_FAILURE, result)
    }

    @Test
    fun `空文本的 OCR 结果返回成功`() = runTest {
        val ocrResult = OcrResult(
            fullText = "",
            blocks = emptyList(),
            processingTimeMs = 50
        )
        val action = OcrRecognizeAction(recognizer = { ocrResult })

        val result = action.apply(runtime)

        // AppletResult.succeeded("") 对空字符串仍返回成功
        assertTrue(result.isSuccessful)
        assertEquals("", result.returned)
    }
}
