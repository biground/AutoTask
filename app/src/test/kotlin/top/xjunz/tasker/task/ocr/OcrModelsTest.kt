package top.xjunz.tasker.task.ocr

import android.graphics.Rect
import org.junit.Assert.*
import org.junit.Test

/**
 * OcrModels 单元测试
 */
class OcrModelsTest {

    // --- OcrResult ---

    @Test
    fun `OcrResult equals and hashCode`() {
        val blocks = listOf(TextBlock("你好", Rect(0, 0, 100, 50), 0.95f))
        val a = OcrResult("你好", blocks, 120)
        val b = OcrResult("你好", blocks, 120)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `OcrResult copy 修改 fullText 不影响原对象`() {
        val original = OcrResult("原始文本", emptyList(), 50)
        val copied = original.copy(fullText = "新文本")
        assertEquals("原始文本", original.fullText)
        assertEquals("新文本", copied.fullText)
        assertNotEquals(original, copied)
    }

    @Test
    fun `OcrResult 空 blocks 列表`() {
        val result = OcrResult("", emptyList())
        assertTrue(result.blocks.isEmpty())
        assertEquals(0L, result.processingTimeMs)
    }

    // --- TextBlock ---

    @Test
    fun `TextBlock data class 行为`() {
        val rect = Rect(10, 20, 200, 80)
        val block = TextBlock("测试", rect, 0.88f)
        assertEquals("测试", block.text)
        assertSame(rect, block.boundingBox)
        assertEquals(0.88f, block.confidence, 0.001f)

        val copy = block.copy(confidence = 0.99f)
        assertEquals(0.99f, copy.confidence, 0.001f)
        assertEquals(0.88f, block.confidence, 0.001f)
    }

    // --- OcrTextMatchMode ---

    @Test
    fun `OcrTextMatchMode 枚举值完整`() {
        val values = OcrTextMatchMode.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(OcrTextMatchMode.CONTAINS))
        assertTrue(values.contains(OcrTextMatchMode.REGEX))
        assertTrue(values.contains(OcrTextMatchMode.EXACT))
    }

    @Test
    fun `OcrTextMatchMode valueOf 正确`() {
        assertEquals(OcrTextMatchMode.CONTAINS, OcrTextMatchMode.valueOf("CONTAINS"))
        assertEquals(OcrTextMatchMode.REGEX, OcrTextMatchMode.valueOf("REGEX"))
        assertEquals(OcrTextMatchMode.EXACT, OcrTextMatchMode.valueOf("EXACT"))
    }
}
