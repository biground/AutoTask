/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.task.ocr.OcrResult
import top.xjunz.tasker.task.ocr.OcrTextMatchMode

/**
 * OcrTextConstraint 单元测试
 *
 * 覆盖三种匹配模式（CONTAINS / EXACT / REGEX）、取反、OCR 返回 null、非法正则等场景。
 */
class OcrTextConstraintTest {

    /** 构造一个 OcrTextConstraint 并注入 values 和 recognizer */
    private fun create(
        matchMode: OcrTextMatchMode,
        matchText: String,
        ocrFullText: String?,
        inverted: Boolean = false
    ): OcrTextConstraint {
        val recognizer: suspend () -> OcrResult? = {
            ocrFullText?.let { OcrResult(it, emptyList(), 10) }
        }
        return OcrTextConstraint(matchMode, recognizer).also {
            it.values = mapOf(0 to matchText)
            if (inverted) it.isInverted = true
        }
    }

    // ========== CONTAINS ==========

    @Test
    fun `CONTAINS 匹配成功`() = runTest {
        val constraint = create(OcrTextMatchMode.CONTAINS, "你好", "你好世界")
        val result = constraint.apply(mockRuntime())
        assertTrue("fullText 包含 '你好'，应成功", result.isSuccessful)
    }

    @Test
    fun `CONTAINS 匹配失败`() = runTest {
        val constraint = create(OcrTextMatchMode.CONTAINS, "再见", "你好世界")
        val result = constraint.apply(mockRuntime())
        assertFalse("fullText 不包含 '再见'，应失败", result.isSuccessful)
    }

    // ========== EXACT ==========

    @Test
    fun `EXACT 匹配成功`() = runTest {
        val constraint = create(OcrTextMatchMode.EXACT, "你好世界", "你好世界")
        val result = constraint.apply(mockRuntime())
        assertTrue("fullText 完全等于匹配文本，应成功", result.isSuccessful)
    }

    @Test
    fun `EXACT 匹配失败`() = runTest {
        val constraint = create(OcrTextMatchMode.EXACT, "你好", "你好世界")
        val result = constraint.apply(mockRuntime())
        assertFalse("fullText 不完全等于匹配文本，应失败", result.isSuccessful)
    }

    // ========== REGEX ==========

    @Test
    fun `REGEX 匹配成功`() = runTest {
        val constraint = create(OcrTextMatchMode.REGEX, "\\d{3,}", "订单号12345已发货")
        val result = constraint.apply(mockRuntime())
        assertTrue("fullText 含 3+ 位数字，正则应匹配", result.isSuccessful)
    }

    @Test
    fun `REGEX 匹配失败`() = runTest {
        val constraint = create(OcrTextMatchMode.REGEX, "^\\d+$", "订单号12345已发货")
        val result = constraint.apply(mockRuntime())
        assertFalse("fullText 非纯数字，正则应不匹配", result.isSuccessful)
    }

    // ========== isInverted ==========

    @Test
    fun `isInverted 取反 CONTAINS 成功变失败`() = runTest {
        val constraint = create(OcrTextMatchMode.CONTAINS, "你好", "你好世界", inverted = true)
        val result = constraint.apply(mockRuntime())
        assertFalse("取反后，原本成功应变失败", result.isSuccessful)
    }

    @Test
    fun `isInverted 取反 CONTAINS 失败变成功`() = runTest {
        val constraint = create(OcrTextMatchMode.CONTAINS, "再见", "你好世界", inverted = true)
        val result = constraint.apply(mockRuntime())
        assertTrue("取反后，原本失败应变成功", result.isSuccessful)
    }

    // ========== OCR 返回 null ==========

    @Test
    fun `OCR 返回 null 时返回 FAILURE`() = runTest {
        val constraint = create(OcrTextMatchMode.CONTAINS, "测试", null)
        val result = constraint.apply(mockRuntime())
        assertFalse("OCR 返回 null 应返回失败", result.isSuccessful)
    }

    // ========== 非法正则 ==========

    @Test
    fun `非法正则返回 FAILURE`() = runTest {
        val constraint = create(OcrTextMatchMode.REGEX, "[invalid(", "任意文本")
        val result = constraint.apply(mockRuntime())
        assertFalse("非法正则应安全返回失败", result.isSuccessful)
    }

    // ========== values[0] 缺失 ==========

    @Test
    fun `values 缺失匹配文本返回 FAILURE`() = runTest {
        val recognizer: suspend () -> OcrResult? = { OcrResult("text", emptyList()) }
        val constraint = OcrTextConstraint(OcrTextMatchMode.CONTAINS, recognizer).also {
            it.values = emptyMap()
        }
        val result = constraint.apply(mockRuntime())
        assertFalse("values[0] 缺失应返回失败", result.isSuccessful)
    }

    // ========== ReDoS 防护 ==========

    @Test
    fun `isUnsafeRegex 检测嵌套量词`() {
        assertTrue("(a+)+ 应被识别为危险", OcrTextConstraint.isUnsafeRegex("(a+)+"))
        assertTrue("(a*)+ 应被识别为危险", OcrTextConstraint.isUnsafeRegex("(a*)+"))
        assertTrue("(.+)+ 应被识别为危险", OcrTextConstraint.isUnsafeRegex("(.+)+"))
        assertTrue("(a+)* 应被识别为危险", OcrTextConstraint.isUnsafeRegex("(a+)*"))
        assertTrue("(a+)? 应被识别为危险", OcrTextConstraint.isUnsafeRegex("(a+)?"))
    }

    @Test
    fun `isUnsafeRegex 放行安全正则`() {
        assertFalse("\\d{3,} 是安全的", OcrTextConstraint.isUnsafeRegex("\\d{3,}"))
        assertFalse("^\\d+$ 是安全的", OcrTextConstraint.isUnsafeRegex("^\\d+$"))
        assertFalse("[a-z]+ 是安全的", OcrTextConstraint.isUnsafeRegex("[a-z]+"))
        assertFalse("hello 是安全的", OcrTextConstraint.isUnsafeRegex("hello"))
    }

    @Test
    fun `REGEX 拒绝嵌套量词模式`() = runTest {
        val constraint = create(OcrTextMatchMode.REGEX, "(a+)+", "aaaaaaaaaaaaaaaaaa!")
        val result = constraint.apply(mockRuntime())
        assertFalse("嵌套量词正则应直接被拒绝", result.isSuccessful)
    }

    @Test
    fun `REGEX 安全正则正常匹配`() = runTest {
        val constraint = create(OcrTextMatchMode.REGEX, "\\d+", "订单12345")
        val result = constraint.apply(mockRuntime())
        assertTrue("安全正则应正常匹配", result.isSuccessful)
    }

    // ---- 辅助 ----

    /** 创建一个最简的 TaskRuntime 实例（apply 中未使用 runtime 的字段） */
    private fun mockRuntime(): top.xjunz.tasker.engine.runtime.TaskRuntime {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, top.xjunz.tasker.engine.runtime.TaskRuntime::class.java)
            as top.xjunz.tasker.engine.runtime.TaskRuntime
    }
}
