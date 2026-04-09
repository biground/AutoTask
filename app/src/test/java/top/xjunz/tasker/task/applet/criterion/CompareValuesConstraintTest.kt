/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CompareValuesConstraint 单元测试
 */
class CompareValuesConstraintTest {

    // ========== EQUAL ==========

    @Test
    fun testEqualIntegers() {
        assertTrue(CompareValuesConstraint.compare(42L, 42L, CompareOp.EQUAL))
    }

    @Test
    fun testEqualIntegersFalse() {
        assertFalse(CompareValuesConstraint.compare(42L, 99L, CompareOp.EQUAL))
    }

    @Test
    fun testEqualStrings() {
        assertTrue(CompareValuesConstraint.compare("hello", "hello", CompareOp.EQUAL))
    }

    // ========== NOT_EQUAL ==========

    @Test
    fun testNotEqual() {
        assertTrue(CompareValuesConstraint.compare(1L, 2L, CompareOp.NOT_EQUAL))
    }

    @Test
    fun testNotEqualSameValue() {
        assertFalse(CompareValuesConstraint.compare(1L, 1L, CompareOp.NOT_EQUAL))
    }

    // ========== GREATER_THAN ==========

    @Test
    fun testGreaterThan() {
        assertTrue(CompareValuesConstraint.compare(10L, 5L, CompareOp.GREATER_THAN))
    }

    @Test
    fun testGreaterThanEqual() {
        assertFalse(CompareValuesConstraint.compare(5L, 5L, CompareOp.GREATER_THAN))
    }

    @Test
    fun testGreaterThanLess() {
        assertFalse(CompareValuesConstraint.compare(3L, 5L, CompareOp.GREATER_THAN))
    }

    // ========== LESS_THAN ==========

    @Test
    fun testLessThan() {
        assertTrue(CompareValuesConstraint.compare(3L, 5L, CompareOp.LESS_THAN))
    }

    @Test
    fun testLessThanEqual() {
        assertFalse(CompareValuesConstraint.compare(5L, 5L, CompareOp.LESS_THAN))
    }

    @Test
    fun testLessThanGreater() {
        assertFalse(CompareValuesConstraint.compare(10L, 5L, CompareOp.LESS_THAN))
    }

    // ========== GREATER_EQUAL ==========

    @Test
    fun testGreaterEqual() {
        assertTrue(CompareValuesConstraint.compare(10L, 5L, CompareOp.GREATER_EQUAL))
    }

    @Test
    fun testGreaterEqualSame() {
        assertTrue(CompareValuesConstraint.compare(5L, 5L, CompareOp.GREATER_EQUAL))
    }

    @Test
    fun testGreaterEqualLess() {
        assertFalse(CompareValuesConstraint.compare(3L, 5L, CompareOp.GREATER_EQUAL))
    }

    // ========== LESS_EQUAL ==========

    @Test
    fun testLessEqual() {
        assertTrue(CompareValuesConstraint.compare(3L, 5L, CompareOp.LESS_EQUAL))
    }

    @Test
    fun testLessEqualSame() {
        assertTrue(CompareValuesConstraint.compare(5L, 5L, CompareOp.LESS_EQUAL))
    }

    @Test
    fun testLessEqualGreater() {
        assertFalse(CompareValuesConstraint.compare(10L, 5L, CompareOp.LESS_EQUAL))
    }

    // ========== CONTAINS ==========

    @Test
    fun testStringContains() {
        assertTrue(CompareValuesConstraint.compare("hello world", "world", CompareOp.CONTAINS))
    }

    @Test
    fun testStringContainsFalse() {
        assertFalse(CompareValuesConstraint.compare("hello world", "xyz", CompareOp.CONTAINS))
    }

    @Test
    fun testStringContainsNonString() {
        // 非字符串自动 toString 再比较
        assertTrue(CompareValuesConstraint.compare("value=42", "42", CompareOp.CONTAINS))
    }

    // ========== MATCHES_REGEX ==========

    @Test
    fun testRegexMatch() {
        assertTrue(CompareValuesConstraint.compare("abc123", "\\d+", CompareOp.MATCHES_REGEX))
    }

    @Test
    fun testRegexMatchFalse() {
        assertFalse(CompareValuesConstraint.compare("abcdef", "^\\d+$", CompareOp.MATCHES_REGEX))
    }

    @Test
    fun testRegexMatchFullPattern() {
        assertTrue(CompareValuesConstraint.compare("2024-01-15", "^\\d{4}-\\d{2}-\\d{2}$", CompareOp.MATCHES_REGEX))
    }

    // ========== 跨类型比较 ==========

    @Test
    fun testCrossTypeComparison() {
        // Integer(42L) vs Decimal(42.0) — 数值相等
        assertTrue(CompareValuesConstraint.compare(42L, 42.0, CompareOp.EQUAL))
    }

    @Test
    fun testCrossTypeGreaterThan() {
        // Integer(10L) > Decimal(9.5)
        assertTrue(CompareValuesConstraint.compare(10L, 9.5, CompareOp.GREATER_THAN))
    }

    @Test
    fun testCrossTypeLessThan() {
        // Decimal(3.14) < Integer(4L)
        assertTrue(CompareValuesConstraint.compare(3.14, 4L, CompareOp.LESS_THAN))
    }

    // ========== 边界：无效正则 ==========

    @Test
    fun testInvalidRegexReturnsFalse() {
        assertFalse(CompareValuesConstraint.compare("test", "[invalid", CompareOp.MATCHES_REGEX))
    }

    // ========== 边界：null 值 ==========

    @Test
    fun testNullLeftEqual() {
        assertFalse(CompareValuesConstraint.compare(null, "value", CompareOp.EQUAL))
    }

    @Test
    fun testBothNullEqual() {
        assertTrue(CompareValuesConstraint.compare(null, null, CompareOp.EQUAL))
    }

    @Test
    fun testNullNotEqual() {
        assertTrue(CompareValuesConstraint.compare(null, "value", CompareOp.NOT_EQUAL))
    }
}
