package top.xjunz.tasker.engine.variable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class VariableTest {

    @Test
    fun `VariableType 包含 4 种基本类型`() {
        val types = VariableType.values()
        assertEquals(4, types.size)
        assertTrue(types.contains(VariableType.BOOLEAN))
        assertTrue(types.contains(VariableType.INTEGER))
        assertTrue(types.contains(VariableType.DECIMAL))
        assertTrue(types.contains(VariableType.STRING))
    }

    @Test
    fun `VariableScope 包含 2 种作用域`() {
        val scopes = VariableScope.values()
        assertEquals(2, scopes.size)
        assertTrue(scopes.contains(VariableScope.GLOBAL))
        assertTrue(scopes.contains(VariableScope.LOCAL))
    }

    @Test
    fun `Variable 创建包含全部 6 个字段`() {
        val v = Variable(
            id = "uuid-1",
            name = "counter",
            type = VariableType.INTEGER,
            value = 42,
            scope = VariableScope.GLOBAL,
            persisted = true
        )
        assertEquals("uuid-1", v.id)
        assertEquals("counter", v.name)
        assertEquals(VariableType.INTEGER, v.type)
        assertEquals(42, v.value)
        assertEquals(VariableScope.GLOBAL, v.scope)
        assertTrue(v.persisted)
    }

    @Test
    fun `Variable 默认值正确`() {
        val v = Variable(id = "uuid-2", name = "flag", type = VariableType.BOOLEAN)
        assertNull(v.value)
        assertEquals(VariableScope.LOCAL, v.scope)
        assertFalse(v.persisted)
    }

    @Test
    fun `Variable copy 行为正确`() {
        val original = Variable(id = "uuid-3", name = "text", type = VariableType.STRING, value = "hello")
        val copied = original.copy(value = "world", scope = VariableScope.GLOBAL)
        assertEquals("uuid-3", copied.id)
        assertEquals("text", copied.name)
        assertEquals(VariableType.STRING, copied.type)
        assertEquals("world", copied.value)
        assertEquals(VariableScope.GLOBAL, copied.scope)
        assertFalse(copied.persisted)
    }

    @Test
    fun `Variable equals 和 hashCode 行为正确`() {
        val v1 = Variable(id = "uuid-4", name = "x", type = VariableType.DECIMAL, value = 3.14)
        val v2 = Variable(id = "uuid-4", name = "x", type = VariableType.DECIMAL, value = 3.14)
        val v3 = Variable(id = "uuid-5", name = "x", type = VariableType.DECIMAL, value = 3.14)
        assertEquals(v1, v2)
        assertEquals(v1.hashCode(), v2.hashCode())
        assertNotEquals(v1, v3)
    }
}
