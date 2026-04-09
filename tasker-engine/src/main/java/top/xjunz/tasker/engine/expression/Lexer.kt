package top.xjunz.tasker.engine.expression

/**
 * 词法分析器：将表达式字符串转换为 [Token] 列表。
 *
 * 支持的词法单元：整数、小数、字符串（双/单引号，含转义）、布尔、
 * 变量引用 `${...}`、运算符（最长匹配）、括号、逗号、标识符。
 */
class Lexer(private val input: String) {

    private var pos = 0

    /** 当前字符，越界时返回 '\u0000' */
    private fun peek(): Char = if (pos < input.length) input[pos] else '\u0000'

    /** 预览下一个字符 */
    private fun peekNext(): Char = if (pos + 1 < input.length) input[pos + 1] else '\u0000'

    /** 消费当前字符并前进 */
    private fun advance(): Char = input[pos++]

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (pos < input.length) {
            val c = peek()
            when {
                c.isWhitespace() -> advance()
                c.isDigit() -> tokens.add(readNumber())
                c == '"' || c == '\'' -> tokens.add(readString(c))
                c == '$' && peekNext() == '{' -> tokens.add(readVariableRef())
                c == '(' -> { advance(); tokens.add(Token.LeftParen) }
                c == ')' -> { advance(); tokens.add(Token.RightParen) }
                c == ',' -> { advance(); tokens.add(Token.Comma) }
                isOperatorStart(c) -> tokens.add(readOperator())
                c.isLetter() || c == '_' -> tokens.add(readIdentifierOrKeyword())
                else -> throw IllegalArgumentException(
                    "无法识别的字符 '$c'（位置 $pos）"
                )
            }
        }
        tokens.add(Token.EOF)
        return tokens
    }

    // ---- 数字 ----

    private fun readNumber(): Token {
        val start = pos
        while (pos < input.length && peek().isDigit()) advance()
        if (pos < input.length && peek() == '.' && (pos + 1 < input.length && input[pos + 1].isDigit())) {
            advance() // 消费 '.'
            while (pos < input.length && peek().isDigit()) advance()
            return Token.DecimalLiteral(input.substring(start, pos).toDouble())
        }
        return Token.IntLiteral(input.substring(start, pos).toLong())
    }

    // ---- 字符串 ----

    private fun readString(quote: Char): Token {
        advance() // 消费开头引号
        val sb = StringBuilder()
        while (pos < input.length) {
            val c = advance()
            if (c == '\\') {
                if (pos >= input.length) throw IllegalArgumentException("字符串未闭合的转义序列（位置 $pos）")
                when (val escaped = advance()) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    '\\' -> sb.append('\\')
                    '"' -> sb.append('"')
                    '\'' -> sb.append('\'')
                    else -> { sb.append('\\'); sb.append(escaped) }
                }
            } else if (c == quote) {
                return Token.StringLiteral(sb.toString())
            } else {
                sb.append(c)
            }
        }
        throw IllegalArgumentException("字符串未闭合（缺少 $quote）")
    }

    // ---- 变量引用 ----

    private fun readVariableRef(): Token {
        advance() // 消费 '$'
        advance() // 消费 '{'
        val start = pos
        while (pos < input.length && peek() != '}') advance()
        if (pos >= input.length) throw IllegalArgumentException("变量引用未闭合（缺少 }）")
        val name = input.substring(start, pos)
        advance() // 消费 '}'
        return Token.VariableRef(name)
    }

    // ---- 运算符 ----

    private fun isOperatorStart(c: Char): Boolean =
        c in "+-*/%=!<>&|"

    private fun readOperator(): Token {
        val c = advance()
        // 双字符运算符最长匹配
        if (pos < input.length) {
            val next = peek()
            val twoChar = "$c$next"
            if (twoChar in TWO_CHAR_OPERATORS) {
                advance()
                return Token.Operator(twoChar)
            }
        }
        // 单字符运算符
        if ("$c" in SINGLE_CHAR_OPERATORS) {
            return Token.Operator("$c")
        }
        throw IllegalArgumentException("无法识别的运算符 '$c'（位置 ${pos - 1}）")
    }

    // ---- 标识符 / 关键字 ----

    private fun readIdentifierOrKeyword(): Token {
        val start = pos
        while (pos < input.length && (peek().isLetterOrDigit() || peek() == '_')) advance()
        return when (val word = input.substring(start, pos)) {
            "true" -> Token.BoolLiteral(true)
            "false" -> Token.BoolLiteral(false)
            else -> Token.Identifier(word)
        }
    }

    companion object {
        private val TWO_CHAR_OPERATORS = setOf("==", "!=", "<=", ">=", "&&", "||")
        private val SINGLE_CHAR_OPERATORS = setOf("+", "-", "*", "/", "%", "<", ">", "!")
    }
}
