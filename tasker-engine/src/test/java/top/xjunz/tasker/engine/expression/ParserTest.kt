package top.xjunz.tasker.engine.expression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

internal class ParserTest {

    private fun parse(input: String): AstNode {
        val tokens = Lexer(input).tokenize()
        return Parser(tokens).parse()
    }

    @Test
    fun testIntLiteral() {
        assertEquals(
            AstNode.IntLiteral(42L),
            parse("42")
        )
    }

    @Test
    fun testBinaryAdd() {
        assertEquals(
            AstNode.BinaryOp("+", AstNode.IntLiteral(1L), AstNode.IntLiteral(2L)),
            parse("1 + 2")
        )
    }

    @Test
    fun testOperatorPrecedence() {
        // 1 + 2 * 3 → BinaryOp("+", 1, BinaryOp("*", 2, 3))
        assertEquals(
            AstNode.BinaryOp(
                "+",
                AstNode.IntLiteral(1L),
                AstNode.BinaryOp("*", AstNode.IntLiteral(2L), AstNode.IntLiteral(3L))
            ),
            parse("1 + 2 * 3")
        )
    }

    @Test
    fun testParentheses() {
        // (1 + 2) * 3 → BinaryOp("*", BinaryOp("+", 1, 2), 3)
        assertEquals(
            AstNode.BinaryOp(
                "*",
                AstNode.BinaryOp("+", AstNode.IntLiteral(1L), AstNode.IntLiteral(2L)),
                AstNode.IntLiteral(3L)
            ),
            parse("(1 + 2) * 3")
        )
    }

    @Test
    fun testUnaryNegation() {
        assertEquals(
            AstNode.UnaryOp("-", AstNode.IntLiteral(5L)),
            parse("-5")
        )
    }

    @Test
    fun testUnaryNot() {
        assertEquals(
            AstNode.UnaryOp("!", AstNode.BoolLiteral(true)),
            parse("!true")
        )
    }

    @Test
    fun testLogicalExpression() {
        // true && false || true → BinaryOp("||", BinaryOp("&&", true, false), true)
        // && 优先级高于 ||
        assertEquals(
            AstNode.BinaryOp(
                "||",
                AstNode.BinaryOp(
                    "&&",
                    AstNode.BoolLiteral(true),
                    AstNode.BoolLiteral(false)
                ),
                AstNode.BoolLiteral(true)
            ),
            parse("true && false || true")
        )
    }

    @Test
    fun testComparisonChain() {
        // ${x} > 0 && ${x} < 100
        assertEquals(
            AstNode.BinaryOp(
                "&&",
                AstNode.BinaryOp(">", AstNode.VariableRef("x"), AstNode.IntLiteral(0L)),
                AstNode.BinaryOp("<", AstNode.VariableRef("x"), AstNode.IntLiteral(100L))
            ),
            parse("\${x} > 0 && \${x} < 100")
        )
    }

    @Test
    fun testFunctionCall() {
        assertEquals(
            AstNode.FunctionCall("len", listOf(AstNode.StringLiteral("hello"))),
            parse("len(\"hello\")")
        )
    }

    @Test
    fun testFunctionMultiArgs() {
        assertEquals(
            AstNode.FunctionCall(
                "max",
                listOf(
                    AstNode.IntLiteral(1L),
                    AstNode.IntLiteral(2L),
                    AstNode.IntLiteral(3L)
                )
            ),
            parse("max(1, 2, 3)")
        )
    }

    @Test
    fun testNestedExpression() {
        // (${a} + ${b}) * (${c} - 1)
        assertEquals(
            AstNode.BinaryOp(
                "*",
                AstNode.BinaryOp("+", AstNode.VariableRef("a"), AstNode.VariableRef("b")),
                AstNode.BinaryOp("-", AstNode.VariableRef("c"), AstNode.IntLiteral(1L))
            ),
            parse("(\${a} + \${b}) * (\${c} - 1)")
        )
    }

    @Test
    fun testComplexMixed() {
        // ${x} >= 10 && len("hello") == 5
        assertEquals(
            AstNode.BinaryOp(
                "&&",
                AstNode.BinaryOp(">=", AstNode.VariableRef("x"), AstNode.IntLiteral(10L)),
                AstNode.BinaryOp(
                    "==",
                    AstNode.FunctionCall("len", listOf(AstNode.StringLiteral("hello"))),
                    AstNode.IntLiteral(5L)
                )
            ),
            parse("\${x} >= 10 && len(\"hello\") == 5")
        )
    }

    @Test
    fun testEmptyExpression() {
        try {
            parse("")
            fail("应抛出异常")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("空"))
        }
    }

    @Test
    fun testUnmatchedParen() {
        try {
            parse("(1 + 2")
            fail("应抛出异常")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("括号") || e.message!!.contains(")"))
        }
    }

    @Test
    fun testTrailingOperator() {
        try {
            parse("1 +")
            fail("应抛出异常")
        } catch (e: IllegalArgumentException) {
            // 尾随运算符导致解析失败
            assertTrue(e.message != null)
        }
    }
}
