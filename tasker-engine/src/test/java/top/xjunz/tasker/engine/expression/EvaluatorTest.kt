package top.xjunz.tasker.engine.expression

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.variable.InMemoryVariableRepository
import top.xjunz.tasker.engine.variable.MagicVariableProvider
import top.xjunz.tasker.engine.variable.MagicVariableRegistry
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType

internal class EvaluatorTest {

    private lateinit var repo: InMemoryVariableRepository
    private lateinit var magicRegistry: MagicVariableRegistry
    private lateinit var evaluator: Evaluator

    @Before
    fun setUp() {
        repo = InMemoryVariableRepository()
        magicRegistry = MagicVariableRegistry()
        evaluator = Evaluator(repo, magicRegistry)
    }

    // ---- 1. 整数算术 ----
    @Test
    fun testIntegerArithmetic() = runTest {
        assertEquals(7L, evaluator.evaluateExpression("1 + 2 * 3"))
    }

    // ---- 2. 小数算术 ----
    @Test
    fun testDecimalArithmetic() = runTest {
        assertEquals(6.28, evaluator.evaluateExpression("3.14 * 2") as Double, 0.001)
    }

    // ---- 3. 字符串拼接 ----
    @Test
    fun testStringConcatenation() = runTest {
        assertEquals("hello world", evaluator.evaluateExpression("\"hello\" + \" \" + \"world\""))
    }

    // ---- 4. 布尔逻辑 ----
    @Test
    fun testBooleanLogic() = runTest {
        assertEquals(true, evaluator.evaluateExpression("true && false || true"))
    }

    // ---- 5. 比较运算 ----
    @Test
    fun testComparisonOperators() = runTest {
        assertEquals(true, evaluator.evaluateExpression("10 > 5 && 3 <= 3"))
    }

    // ---- 6. 变量解析 ----
    @Test
    fun testVariableResolution() = runTest {
        repo.setVariable(Variable("v1", "count", VariableType.INTEGER, 10L, VariableScope.LOCAL))
        assertEquals(11L, evaluator.evaluateExpression("\${count} + 1"))
    }

    // ---- 7. 魔法变量 ----
    @Test
    fun testMagicVariable() = runTest {
        magicRegistry.register(object : MagicVariableProvider {
            override val name = "__current_time__"
            override val type = VariableType.INTEGER
            override fun getCurrentValue(): Any = System.currentTimeMillis()
        })
        val result = evaluator.evaluateExpression("\${__current_time__}")
        assertTrue("魔法变量应返回 Long 类型", result is Long)
        assertTrue("时间戳应大于 0", (result as Long) > 0)
    }

    // ---- 8. 一元运算 ----
    @Test
    fun testUnaryNegation() = runTest {
        assertEquals(-2L, evaluator.evaluateExpression("-5 + 3"))
    }

    @Test
    fun testUnaryNot() = runTest {
        assertEquals(false, evaluator.evaluateExpression("!true"))
    }

    // ---- 9. 内置函数 len ----
    @Test
    fun testFunctionLen() = runTest {
        assertEquals(5L, evaluator.evaluateExpression("len(\"hello\")"))
    }

    // ---- 10. 内置函数 str / int ----
    @Test
    fun testFunctionStr() = runTest {
        assertEquals("42", evaluator.evaluateExpression("str(42)"))
    }

    @Test
    fun testFunctionInt() = runTest {
        assertEquals(42L, evaluator.evaluateExpression("int(\"42\")"))
    }

    // ---- 11. 内置函数 upper / lower ----
    @Test
    fun testFunctionUpper() = runTest {
        assertEquals("HELLO", evaluator.evaluateExpression("upper(\"hello\")"))
    }

    @Test
    fun testFunctionLower() = runTest {
        assertEquals("world", evaluator.evaluateExpression("lower(\"WORLD\")"))
    }

    // ---- 12. 内置函数 contains ----
    @Test
    fun testFunctionContains() = runTest {
        assertEquals(true, evaluator.evaluateExpression("contains(\"hello world\", \"world\")"))
    }

    @Test
    fun testFunctionContainsFalse() = runTest {
        assertEquals(false, evaluator.evaluateExpression("contains(\"hello world\", \"xyz\")"))
    }

    // ---- 13. 内置函数 substring ----
    @Test
    fun testFunctionSubstring() = runTest {
        assertEquals("el", evaluator.evaluateExpression("substring(\"hello\", 1, 3)"))
    }

    @Test
    fun testFunctionSubstringNoEnd() = runTest {
        assertEquals("llo", evaluator.evaluateExpression("substring(\"hello\", 2)"))
    }

    // ---- 14. 除零异常 ----
    @Test(expected = EvaluationException::class)
    fun testDivisionByZero() = runTest {
        evaluator.evaluateExpression("1 / 0")
    }

    // ---- 15. 未定义变量 ----
    @Test(expected = EvaluationException::class)
    fun testUndefinedVariable() = runTest {
        evaluator.evaluateExpression("\${undefined_var}")
    }

    // ---- 额外覆盖：类型自动提升 ----
    @Test
    fun testTypePromotion_IntPlusDouble() = runTest {
        val result = evaluator.evaluateExpression("1 + 2.5")
        assertTrue("Int + Double 应返回 Double", result is Double)
        assertEquals(3.5, result as Double, 0.001)
    }

    @Test
    fun testIntegerDivision() = runTest {
        assertEquals(3L, evaluator.evaluateExpression("7 / 2"))
    }

    @Test
    fun testModulo() = runTest {
        assertEquals(1L, evaluator.evaluateExpression("7 % 3"))
    }

    // ---- 额外覆盖：内置函数 dec / abs / min / max ----
    @Test
    fun testFunctionDec() = runTest {
        assertEquals(3.14, evaluator.evaluateExpression("dec(\"3.14\")") as Double, 0.001)
    }

    @Test
    fun testFunctionAbs() = runTest {
        assertEquals(5L, evaluator.evaluateExpression("abs(-5)"))
    }

    @Test
    fun testFunctionMin() = runTest {
        assertEquals(2L, evaluator.evaluateExpression("min(2, 8)"))
    }

    @Test
    fun testFunctionMax() = runTest {
        assertEquals(8L, evaluator.evaluateExpression("max(2, 8)"))
    }

    @Test
    fun testFunctionNow() = runTest {
        val result = evaluator.evaluateExpression("now()")
        assertTrue("now() 应返回 Long", result is Long)
        assertTrue("时间戳应大于 0", (result as Long) > 0)
    }

    // ---- 额外覆盖：startsWith / endsWith ----
    @Test
    fun testFunctionStartsWith() = runTest {
        assertEquals(true, evaluator.evaluateExpression("startsWith(\"hello\", \"hel\")"))
    }

    @Test
    fun testFunctionEndsWith() = runTest {
        assertEquals(true, evaluator.evaluateExpression("endsWith(\"hello\", \"llo\")"))
    }

    // ---- 额外覆盖：类型错误 ----
    @Test(expected = EvaluationException::class)
    fun testTypeMismatch_BooleanArithmetic() = runTest {
        evaluator.evaluateExpression("true + 1")
    }

    // ---- 额外覆盖：未定义函数 ----
    @Test(expected = EvaluationException::class)
    fun testUndefinedFunction() = runTest {
        evaluator.evaluateExpression("unknown(1)")
    }
}
