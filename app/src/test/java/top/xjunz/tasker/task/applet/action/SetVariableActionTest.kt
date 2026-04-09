package top.xjunz.tasker.task.applet.action

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.engine.variable.VariableType

/**
 * SetVariableAction 单元测试
 * - 变量名验证
 * - 类型转换
 */
class SetVariableActionTest {

    // ========== isValidVariableName ==========

    @Test
    fun `合法变量名 - 简单名称`() {
        assertTrue(SetVariableAction.isValidVariableName("counter"))
    }

    @Test
    fun `合法变量名 - 含下划线`() {
        assertTrue(SetVariableAction.isValidVariableName("my_var"))
    }

    @Test
    fun `合法变量名 - 单下划线开头`() {
        assertTrue(SetVariableAction.isValidVariableName("_temp"))
    }

    @Test
    fun `合法变量名 - 仅以双下划线开头但不以双下划线结尾`() {
        assertTrue(SetVariableAction.isValidVariableName("__prefix"))
    }

    @Test
    fun `非法变量名 - 空字符串`() {
        assertFalse(SetVariableAction.isValidVariableName(""))
    }

    @Test
    fun `非法变量名 - 纯空格`() {
        assertFalse(SetVariableAction.isValidVariableName("   "))
    }

    @Test
    fun `非法变量名 - 超过 128 字符`() {
        assertFalse(SetVariableAction.isValidVariableName("a".repeat(129)))
    }

    @Test
    fun `非法变量名 - 恰好 128 字符合法`() {
        assertTrue(SetVariableAction.isValidVariableName("a".repeat(128)))
    }

    @Test
    fun `非法变量名 - 系统保留名 __magic__`() {
        assertFalse(SetVariableAction.isValidVariableName("__magic__"))
    }

    @Test
    fun `非法变量名 - 系统保留名 ____`() {
        assertFalse(SetVariableAction.isValidVariableName("____"))
    }

    @Test
    fun `非法变量名 - 含模板标记 dollar brace`() {
        assertFalse(SetVariableAction.isValidVariableName("val\${x}"))
    }

    @Test
    fun `非法变量名 - 含右花括号`() {
        assertFalse(SetVariableAction.isValidVariableName("val}"))
    }

    // ========== coerceValue — BOOLEAN ==========

    @Test
    fun `coerceBoolean - Boolean true`() {
        assertEquals(true, SetVariableAction.coerceValue(true, VariableType.BOOLEAN))
    }

    @Test
    fun `coerceBoolean - Boolean false`() {
        assertEquals(false, SetVariableAction.coerceValue(false, VariableType.BOOLEAN))
    }

    @Test
    fun `coerceBoolean - String true`() {
        assertEquals(true, SetVariableAction.coerceValue("true", VariableType.BOOLEAN))
    }

    @Test
    fun `coerceBoolean - String false`() {
        assertEquals(false, SetVariableAction.coerceValue("false", VariableType.BOOLEAN))
    }

    @Test
    fun `coerceBoolean - 非法字符串返回 null`() {
        assertNull(SetVariableAction.coerceValue("yes", VariableType.BOOLEAN))
    }

    @Test
    fun `coerceBoolean - 数字返回 null`() {
        assertNull(SetVariableAction.coerceValue(1, VariableType.BOOLEAN))
    }

    // ========== coerceValue — INTEGER ==========

    @Test
    fun `coerceInteger - Long`() {
        assertEquals(42L, SetVariableAction.coerceValue(42L, VariableType.INTEGER))
    }

    @Test
    fun `coerceInteger - Int 升为 Long`() {
        assertEquals(42L, SetVariableAction.coerceValue(42, VariableType.INTEGER))
    }

    @Test
    fun `coerceInteger - String 数字`() {
        assertEquals(100L, SetVariableAction.coerceValue("100", VariableType.INTEGER))
    }

    @Test
    fun `coerceInteger - 非法字符串返回 null`() {
        assertNull(SetVariableAction.coerceValue("abc", VariableType.INTEGER))
    }

    @Test
    fun `coerceInteger - null 返回 null`() {
        assertNull(SetVariableAction.coerceValue(null, VariableType.INTEGER))
    }

    // ========== coerceValue — DECIMAL ==========

    @Test
    fun `coerceDecimal - Double`() {
        assertEquals(3.14, SetVariableAction.coerceValue(3.14, VariableType.DECIMAL))
    }

    @Test
    fun `coerceDecimal - Float 升为 Double`() {
        assertEquals(1.5.toFloat().toDouble(), SetVariableAction.coerceValue(1.5f, VariableType.DECIMAL))
    }

    @Test
    fun `coerceDecimal - Int 升为 Double`() {
        assertEquals(10.0, SetVariableAction.coerceValue(10, VariableType.DECIMAL))
    }

    @Test
    fun `coerceDecimal - String 数字`() {
        assertEquals(2.718, SetVariableAction.coerceValue("2.718", VariableType.DECIMAL))
    }

    @Test
    fun `coerceDecimal - 非法字符串返回 null`() {
        assertNull(SetVariableAction.coerceValue("not_a_number", VariableType.DECIMAL))
    }

    // ========== coerceValue — STRING ==========

    @Test
    fun `coerceString - 字符串原样返回`() {
        assertEquals("hello", SetVariableAction.coerceValue("hello", VariableType.STRING))
    }

    @Test
    fun `coerceString - 数字转为字符串`() {
        assertEquals("42", SetVariableAction.coerceValue(42, VariableType.STRING))
    }

    @Test
    fun `coerceString - Boolean 转为字符串`() {
        assertEquals("true", SetVariableAction.coerceValue(true, VariableType.STRING))
    }

    @Test
    fun `coerceString - null 返回 null`() {
        assertNull(SetVariableAction.coerceValue(null, VariableType.STRING))
    }

    // ========== 魔法变量名拒绝 ==========

    @Test
    fun `拒绝魔法变量名 __init__`() {
        assertFalse(SetVariableAction.isValidVariableName("__init__"))
    }

    @Test
    fun `拒绝魔法变量名 __system__`() {
        assertFalse(SetVariableAction.isValidVariableName("__system__"))
    }

    @Test
    fun `拒绝魔法变量名 __`() {
        // 以 __ 开头且以 __ 结尾
        assertFalse(SetVariableAction.isValidVariableName("__"))
    }
}
