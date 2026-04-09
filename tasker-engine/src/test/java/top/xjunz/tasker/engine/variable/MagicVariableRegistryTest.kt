package top.xjunz.tasker.engine.variable

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MagicVariableRegistryTest {

    private lateinit var registry: MagicVariableRegistry

    // 用于测试的简单魔术变量提供者
    private class FakeTimeMagicVariable : MagicVariableProvider {
        override val name = "__current_time__"
        override val type = VariableType.INTEGER
        override fun getCurrentValue(): Any = 1234567890L
    }

    private class FakeDeviceMagicVariable : MagicVariableProvider {
        override val name = "__device_name__"
        override val type = VariableType.STRING
        override fun getCurrentValue(): Any = "TestDevice"
    }

    @Before
    fun setUp() {
        registry = MagicVariableRegistry()
    }

    @Test
    fun testRegisterAndResolve() {
        val provider = FakeTimeMagicVariable()
        registry.register(provider)

        val resolved = registry.resolve("__current_time__")
        assertNotNull(resolved)
        assertEquals("__current_time__", resolved!!.name)
        assertEquals(VariableType.INTEGER, resolved.type)
        assertEquals(1234567890L, resolved.value)
    }

    @Test
    fun testResolveNonExistent() {
        val resolved = registry.resolve("__nonexistent__")
        assertNull(resolved)
    }

    @Test
    fun testGetAllNames() {
        registry.register(FakeTimeMagicVariable())
        registry.register(FakeDeviceMagicVariable())

        val names = registry.getAllNames()
        assertEquals(2, names.size)
        assertTrue(names.contains("__current_time__"))
        assertTrue(names.contains("__device_name__"))
    }

    @Test
    fun testResolvedVariableProperties() {
        registry.register(FakeTimeMagicVariable())

        val resolved = registry.resolve("__current_time__")!!
        assertEquals(VariableScope.GLOBAL, resolved.scope)
        assertFalse(resolved.persisted)
        assertEquals("magic___current_time__", resolved.id)
    }

    @Test
    fun testGetProvider() {
        val provider = FakeTimeMagicVariable()
        registry.register(provider)

        assertNotNull(registry.get("__current_time__"))
        assertNull(registry.get("__nonexistent__"))
    }

    @Test
    fun testRegisterOverwritesExisting() {
        registry.register(FakeTimeMagicVariable())
        // 注册同名提供者应覆盖
        val newProvider = object : MagicVariableProvider {
            override val name = "__current_time__"
            override val type = VariableType.INTEGER
            override fun getCurrentValue(): Any = 9999L
        }
        registry.register(newProvider)

        val resolved = registry.resolve("__current_time__")!!
        assertEquals(9999L, resolved.value)
        assertEquals(1, registry.getAllNames().size)
    }
}
