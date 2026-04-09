package top.xjunz.tasker.engine.expression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class LexerTest {

    private fun tokenize(input: String): List<Token> = Lexer(input).tokenize()

    @Test
    fun testIntegerLiteral() {
        val tokens = tokenize("42")
        assertEquals(
            listOf(Token.IntLiteral(42L), Token.EOF),
            tokens
        )
    }

    @Test
    fun testDecimalLiteral() {
        val tokens = tokenize("3.14")
        assertEquals(
            listOf(Token.DecimalLiteral(3.14), Token.EOF),
            tokens
        )
    }

    @Test
    fun testStringLiteral() {
        val tokens = tokenize("\"hello\"")
        assertEquals(
            listOf(Token.StringLiteral("hello"), Token.EOF),
            tokens
        )
    }

    @Test
    fun testStringEscape() {
        val tokens = tokenize("\"say \\\"hi\\\"\"")
        assertEquals(
            listOf(Token.StringLiteral("say \"hi\""), Token.EOF),
            tokens
        )
    }

    @Test
    fun testBoolLiteral() {
        val tokens = tokenize("true")
        assertEquals(
            listOf(Token.BoolLiteral(true), Token.EOF),
            tokens
        )
    }

    @Test
    fun testVariableRef() {
        val tokens = tokenize("\${count}")
        assertEquals(
            listOf(Token.VariableRef("count"), Token.EOF),
            tokens
        )
    }

    @Test
    fun testSimpleExpression() {
        val tokens = tokenize("1 + 2")
        assertEquals(
            listOf(
                Token.IntLiteral(1L),
                Token.Operator("+"),
                Token.IntLiteral(2L),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testComparisonExpression() {
        val tokens = tokenize("\${x} >= 10")
        assertEquals(
            listOf(
                Token.VariableRef("x"),
                Token.Operator(">="),
                Token.IntLiteral(10L),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testLogicalExpression() {
        val tokens = tokenize("true && false")
        assertEquals(
            listOf(
                Token.BoolLiteral(true),
                Token.Operator("&&"),
                Token.BoolLiteral(false),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testFunctionCall() {
        val tokens = tokenize("len(\"hello\")")
        assertEquals(
            listOf(
                Token.Identifier("len"),
                Token.LeftParen,
                Token.StringLiteral("hello"),
                Token.RightParen,
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testComplexExpression() {
        val tokens = tokenize("\${a} + \${b} * (3 - 1)")
        assertEquals(
            listOf(
                Token.VariableRef("a"),
                Token.Operator("+"),
                Token.VariableRef("b"),
                Token.Operator("*"),
                Token.LeftParen,
                Token.IntLiteral(3L),
                Token.Operator("-"),
                Token.IntLiteral(1L),
                Token.RightParen,
                Token.EOF
            ),
            tokens
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUnrecognizedCharacter() {
        tokenize("@")
    }

    // --- 补充边界测试 ---

    @Test
    fun testSingleQuoteString() {
        val tokens = tokenize("'world'")
        assertEquals(
            listOf(Token.StringLiteral("world"), Token.EOF),
            tokens
        )
    }

    @Test
    fun testStringEscapeNewlineAndTab() {
        val tokens = tokenize("\"line1\\nline2\\tend\"")
        assertEquals(
            listOf(Token.StringLiteral("line1\nline2\tend"), Token.EOF),
            tokens
        )
    }

    @Test
    fun testNotOperator() {
        val tokens = tokenize("!true")
        assertEquals(
            listOf(Token.Operator("!"), Token.BoolLiteral(true), Token.EOF),
            tokens
        )
    }

    @Test
    fun testNotEqualsOperator() {
        // != 应最长匹配为 != 而非 ! + =
        val tokens = tokenize("1 != 2")
        assertEquals(
            listOf(
                Token.IntLiteral(1L),
                Token.Operator("!="),
                Token.IntLiteral(2L),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testOrOperator() {
        val tokens = tokenize("true || false")
        assertEquals(
            listOf(
                Token.BoolLiteral(true),
                Token.Operator("||"),
                Token.BoolLiteral(false),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testComma() {
        val tokens = tokenize("max(1, 2)")
        assertEquals(
            listOf(
                Token.Identifier("max"),
                Token.LeftParen,
                Token.IntLiteral(1L),
                Token.Comma,
                Token.IntLiteral(2L),
                Token.RightParen,
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testEmptyInput() {
        val tokens = tokenize("")
        assertEquals(listOf(Token.EOF), tokens)
    }

    @Test
    fun testWhitespaceOnly() {
        val tokens = tokenize("   \t  ")
        assertEquals(listOf(Token.EOF), tokens)
    }

    @Test
    fun testFalseBoolLiteral() {
        val tokens = tokenize("false")
        assertEquals(
            listOf(Token.BoolLiteral(false), Token.EOF),
            tokens
        )
    }

    @Test
    fun testModuloOperator() {
        val tokens = tokenize("10 % 3")
        assertEquals(
            listOf(
                Token.IntLiteral(10L),
                Token.Operator("%"),
                Token.IntLiteral(3L),
                Token.EOF
            ),
            tokens
        )
    }

    @Test
    fun testBackslashEscapeInString() {
        val tokens = tokenize("\"a\\\\b\"")
        assertEquals(
            listOf(Token.StringLiteral("a\\b"), Token.EOF),
            tokens
        )
    }
}
