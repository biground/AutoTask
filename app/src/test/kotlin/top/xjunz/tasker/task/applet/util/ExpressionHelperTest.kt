package top.xjunz.tasker.task.applet.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableChangeEvent
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType

/**
 * ExpressionHelper 单元测试
 */
class ExpressionHelperTest {

    // 简易内存变量仓库，用于测试
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

    // ========== isExpression ==========

    @Test
    fun `isExpression - 纯整数字面量 42 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("42"))
    }

    @Test
    fun `isExpression - 纯负整数 -10 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("-10"))
    }

    @Test
    fun `isExpression - 纯小数 3_14 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("3.14"))
    }

    @Test
    fun `isExpression - 纯布尔 true 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("true"))
    }

    @Test
    fun `isExpression - 纯布尔 false 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("false"))
    }

    @Test
    fun `isExpression - 算术表达式 1 + 2 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("1 + 2"))
    }

    @Test
    fun `isExpression - 乘法表达式 3 * 4 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("3 * 4"))
    }

    @Test
    fun `isExpression - 变量引用 dollar_name 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("\${name}"))
    }

    @Test
    fun `isExpression - 纯字符串 hello world 返回 false`() {
        assertFalse(ExpressionHelper.isExpression("hello world"))
    }

    @Test
    fun `isExpression - 空字符串返回 false`() {
        assertFalse(ExpressionHelper.isExpression(""))
    }

    @Test
    fun `isExpression - 比较运算符 x == 5 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("x == 5"))
    }

    @Test
    fun `isExpression - 逻辑运算符 a && b 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("a && b"))
    }

    @Test
    fun `isExpression - 函数调用 len_hello 返回 true`() {
        assertTrue(ExpressionHelper.isExpression("len(\"hello\")"))
    }

    @Test
    fun `isExpression - 字符串拼接表达式返回 true`() {
        assertTrue(ExpressionHelper.isExpression("\"hello\" + \" world\""))
    }

    // ========== evaluateExpression ==========

    @Test
    fun `evaluateExpression - 简单加法 1 + 2 等于 3`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("1 + 2", repo)
        assertEquals(3L, result)
    }

    @Test
    fun `evaluateExpression - 字符串拼接`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("\"hello\" + \" \" + \"world\"", repo)
        assertEquals("hello world", result)
    }

    @Test
    fun `evaluateExpression - 带变量引用的字符串拼接`() = runBlocking {
        val repo = InMemoryVariableRepository()
        repo.setVariable(
            Variable(
                id = "1",
                name = "name",
                type = VariableType.STRING,
                value = "World"
            )
        )
        val result = ExpressionHelper.evaluateExpression("\"Hello \" + \${name}", repo)
        assertEquals("Hello World", result)
    }

    @Test
    fun `evaluateExpression - 语法错误优雅降级返回 null`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("###", repo)
        assertNull(result)
    }

    @Test
    fun `evaluateExpression - 复杂算术 2 * 3 + 1`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("2 * 3 + 1", repo)
        assertEquals(7L, result)
    }

    @Test
    fun `evaluateExpression - 带变量的算术`() = runBlocking {
        val repo = InMemoryVariableRepository()
        repo.setVariable(
            Variable(id = "1", name = "x", type = VariableType.INTEGER, value = 10L)
        )
        val result = ExpressionHelper.evaluateExpression("\${x} + 5", repo)
        assertEquals(15L, result)
    }

    @Test
    fun `evaluateExpression - 比较运算`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("10 > 5", repo)
        assertEquals(true, result)
    }

    @Test
    fun `evaluateExpression - 函数调用 abs`() = runBlocking {
        val repo = InMemoryVariableRepository()
        val result = ExpressionHelper.evaluateExpression("abs(-42)", repo)
        assertEquals(42L, result)
    }
}
