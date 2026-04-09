package top.xjunz.tasker.engine.expression

/**
 * Pratt Parser：自顶向下运算符优先级解析器。
 * 将 [Token] 列表解析为 [AstNode] 抽象语法树。
 *
 * 每个运算符通过 binding power（绑定力）控制优先级：
 * - nud（前缀解析）：字面量、变量、一元运算、括号分组
 * - led（中缀解析）：二元运算
 */
class Parser(private val tokens: List<Token>) {

    private var pos = 0

    /** 当前 token */
    private fun current(): Token = tokens[pos]

    /** 消费当前 token 并前进 */
    private fun advance(): Token = tokens[pos++]

    /** 断言当前 token 类型，不匹配则抛异常 */
    private fun expect(expected: Token) {
        val actual = current()
        if (actual != expected) {
            throw IllegalArgumentException("期望 $expected，实际 $actual（位置 $pos）")
        }
        advance()
    }

    /**
     * 解析完整表达式，入口方法。
     * 解析后必须到达 EOF，否则抛异常。
     */
    fun parse(): AstNode {
        if (current() is Token.EOF) {
            throw IllegalArgumentException("表达式为空")
        }
        val result = parseExpression(0)
        expect(Token.EOF)
        return result
    }

    /**
     * Pratt 核心：递归解析表达式。
     * @param minBp 最小绑定力，只有更高优先级的运算符才能"吸附"右操作数
     */
    private fun parseExpression(minBp: Int): AstNode {
        var left = parsePrefix()
        while (true) {
            val bp = infixBindingPower(current()) ?: break
            if (bp.left < minBp) break
            left = parseInfix(left, bp)
        }
        return left
    }

    // ---- 前缀解析（nud） ----

    private fun parsePrefix(): AstNode {
        return when (val token = advance()) {
            is Token.IntLiteral -> AstNode.IntLiteral(token.value)
            is Token.DecimalLiteral -> AstNode.DecimalLiteral(token.value)
            is Token.StringLiteral -> AstNode.StringLiteral(token.value)
            is Token.BoolLiteral -> AstNode.BoolLiteral(token.value)
            is Token.VariableRef -> AstNode.VariableRef(token.name)
            is Token.Identifier -> parseFunctionCall(token.name)
            is Token.LeftParen -> parseGrouping()
            is Token.Operator -> {
                if (token.symbol == "-" || token.symbol == "!") {
                    parseUnary(token.symbol)
                } else {
                    throw IllegalArgumentException("意外的运算符 '${token.symbol}'（位置 ${pos - 1}）")
                }
            }
            else -> throw IllegalArgumentException("意外的 token: $token（位置 ${pos - 1}）")
        }
    }

    // ---- 中缀解析（led） ----

    private fun parseInfix(left: AstNode, bp: BindingPower): AstNode {
        val op = (advance() as Token.Operator).symbol
        val right = parseExpression(bp.right)
        return AstNode.BinaryOp(op, left, right)
    }

    // ---- 一元运算 ----

    private fun parseUnary(symbol: String): AstNode {
        val operand = parseExpression(PREFIX_BP)
        return AstNode.UnaryOp(symbol, operand)
    }

    // ---- 括号分组 ----

    private fun parseGrouping(): AstNode {
        val expr = parseExpression(0)
        if (current() !is Token.RightParen) {
            throw IllegalArgumentException("括号未闭合，期望 ')'（位置 $pos）")
        }
        advance()
        return expr
    }

    // ---- 函数调用 ----

    private fun parseFunctionCall(name: String): AstNode {
        if (current() !is Token.LeftParen) {
            throw IllegalArgumentException("标识符 '$name' 后期望 '('（位置 $pos）")
        }
        advance() // 消费 '('
        val args = mutableListOf<AstNode>()
        if (current() !is Token.RightParen) {
            args.add(parseExpression(0))
            while (current() is Token.Comma) {
                advance() // 消费 ','
                args.add(parseExpression(0))
            }
        }
        if (current() !is Token.RightParen) {
            throw IllegalArgumentException("函数调用未闭合，期望 ')'（位置 $pos）")
        }
        advance() // 消费 ')'
        return AstNode.FunctionCall(name, args)
    }

    // ---- 绑定力（优先级） ----

    /** 中缀运算符绑定力 */
    private fun infixBindingPower(token: Token): BindingPower? {
        if (token !is Token.Operator) return null
        return when (token.symbol) {
            "||" -> BindingPower(1, 2)
            "&&" -> BindingPower(3, 4)
            "==", "!=" -> BindingPower(5, 6)
            "<", ">", "<=", ">=" -> BindingPower(7, 8)
            "+", "-" -> BindingPower(9, 10)
            "*", "/", "%" -> BindingPower(11, 12)
            else -> null
        }
    }

    /** 绑定力对：left 用于左侧比较，right 用于递归右侧 */
    private data class BindingPower(val left: Int, val right: Int)

    companion object {
        /** 一元运算符的前缀绑定力 */
        private const val PREFIX_BP = 13
    }
}
