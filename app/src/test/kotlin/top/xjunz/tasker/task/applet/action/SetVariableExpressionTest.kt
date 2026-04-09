package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableChangeEvent
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType

/**
 * SetVariableAction 表达式集成测试
 */
class SetVariableExpressionTest {

    // 简易内存变量仓库
    private class InMemoryVariableRepository : VariableRepository {
        private val store = mutableMapOf<String, Variable>()

        override suspend fun getVariable(name: String): Variable? = store[name]
        override suspend fun setVariable(variable: Variable) {
            store[variable.name] = variable
        }
        override suspend fun deleteVariable(name: String): Boolean = store.remove(name) != null
        override suspend fun getAllVariables(): List<Variable> = store.values.toList()
        override suspend fun getVariablesByScope(scope: VariableScope): List<Variable> =
            store.values.filter { it.scope == scope }
        override fun observeChanges(): Flow<VariableChangeEvent> = emptyFlow()
    }

    private val repo = InMemoryVariableRepository()
    private val action = SetVariableAction { repo }

    @Test
    fun `表达式赋值 - 整数表达式 1 + 2 设置变量值为 3`() = runBlocking {
        val args: Array<Any?> = arrayOf(
            "result",                       // 变量名
            VariableType.INTEGER.ordinal,   // 类型
            "1 + 2",                        // 值（表达式）
            VariableScope.GLOBAL.ordinal,   // 作用域
            false                           // 持久化
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("result")
        assertEquals(3L, variable?.value)
    }

    @Test
    fun `字面量赋值 - 直接数字字符串 42 设置变量值为 42`() = runBlocking {
        val args: Array<Any?> = arrayOf(
            "count",
            VariableType.INTEGER.ordinal,
            "42",
            VariableScope.GLOBAL.ordinal,
            false
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("count")
        assertEquals(42L, variable?.value)
    }

    @Test
    fun `表达式赋值 - 字符串拼接`() = runBlocking {
        val args: Array<Any?> = arrayOf(
            "greeting",
            VariableType.STRING.ordinal,
            "\"Hello\" + \" \" + \"World\"",
            VariableScope.GLOBAL.ordinal,
            false
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("greeting")
        assertEquals("Hello World", variable?.value)
    }

    @Test
    fun `字面量赋值 - 非表达式字符串直接赋值`() = runBlocking {
        val args: Array<Any?> = arrayOf(
            "msg",
            VariableType.STRING.ordinal,
            "hello world",
            VariableScope.GLOBAL.ordinal,
            false
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("msg")
        assertEquals("hello world", variable?.value)
    }

    @Test
    fun `表达式赋值 - 乘法表达式`() = runBlocking {
        val args: Array<Any?> = arrayOf(
            "product",
            VariableType.INTEGER.ordinal,
            "3 * 4",
            VariableScope.GLOBAL.ordinal,
            false
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("product")
        assertEquals(12L, variable?.value)
    }

    @Test
    fun `表达式语法错误 - 降级为原始字符串`() = runBlocking {
        // "###" 包含 isExpression 不识别的字符，不会走表达式引擎
        // 如果走了表达式引擎，会返回 null，降级为原始字符串传给 coerceValue
        val args: Array<Any?> = arrayOf(
            "bad",
            VariableType.STRING.ordinal,
            "###",
            VariableScope.GLOBAL.ordinal,
            false
        )
        action.doAction(args, createMockRuntime())

        val variable = repo.getVariable("bad")
        // "###" 不包含运算符，isExpression 返回 false，直接走 coerceValue
        // STRING 类型的 coerceValue 会 toString()
        assertEquals("###", variable?.value)
    }

    /**
     * 通过反射创建 TaskRuntime 实例（私有构造器）。
     * 由于 app 模块 testOptions.unitTests.returnDefaultValues = true，
     * Android API 调用会返回默认值。
     */
    private fun createMockRuntime(): TaskRuntime {
        val ctor = TaskRuntime::class.java.getDeclaredConstructor()
        ctor.isAccessible = true
        return ctor.newInstance()
    }
}
