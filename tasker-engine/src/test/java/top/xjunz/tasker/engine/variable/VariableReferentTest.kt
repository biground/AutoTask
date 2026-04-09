package top.xjunz.tasker.engine.variable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * VariableReferent 适配器测试：验证变量系统到 Referent 系统的桥接行为。
 */
internal class VariableReferentTest {

    private lateinit var runtime: TaskRuntime

    @Before
    fun setUp() {
        // 通过反射创建 TaskRuntime（私有构造器），VariableReferent 不依赖 runtime 状态
        val ctor = TaskRuntime::class.java.getDeclaredConstructor()
        ctor.isAccessible = true
        runtime = ctor.newInstance()
    }

    private fun makeReferent(
        variableName: String,
        valueResolver: () -> Any?
    ) = VariableReferent(variableName, valueResolver)

    @Test
    fun `which=0 返回变量值`() {
        val referent = makeReferent("counter") { 42 }
        val result = referent.getReferredValue(0, runtime)
        assertEquals(42, result)
    }

    @Test
    fun `which=1 返回变量名`() {
        val referent = makeReferent("greeting") { "hello" }
        val result = referent.getReferredValue(1, runtime)
        assertEquals("greeting", result)
    }

    @Test
    fun `变量不存在时返回 null`() {
        val referent = makeReferent("missing") { null }
        val result = referent.getReferredValue(0, runtime)
        assertNull(result)
    }

    @Test
    fun `变量更新后引用获取最新值`() {
        var currentValue: Any? = "initial"
        val referent = makeReferent("dynamic") { currentValue }

        assertEquals("initial", referent.getReferredValue(0, runtime))

        // 模拟变量更新
        currentValue = "updated"
        assertEquals("updated", referent.getReferredValue(0, runtime))

        // 再次更新为不同类型
        currentValue = 999
        assertEquals(999, referent.getReferredValue(0, runtime))
    }

    @Test
    fun `var 前缀命名空间正确生成`() {
        val name = VariableReferent.referentName("myVariable")
        assertEquals("var:myVariable", name)
    }

    @Test
    fun `NAMESPACE_PREFIX 常量值正确`() {
        assertEquals("var:", VariableReferent.NAMESPACE_PREFIX)
    }

    @Test(expected = NullPointerException::class)
    fun `无效 which 值抛出异常`() {
        val referent = makeReferent("x") { "v" }
        referent.getReferredValue(99, runtime)
    }
}
