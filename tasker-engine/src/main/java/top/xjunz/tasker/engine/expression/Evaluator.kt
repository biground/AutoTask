package top.xjunz.tasker.engine.expression

import top.xjunz.tasker.engine.variable.MagicVariableRegistry
import top.xjunz.tasker.engine.variable.VariableRepository
import kotlin.math.abs

/**
 * 表达式求值器 — 遍历 AST 节点递归求值。
 *
 * 支持的运算：算术、字符串拼接、比较、逻辑、变量解析、魔法变量、内置函数。
 */
class Evaluator(
    private val variableRepository: VariableRepository,
    private val magicVariableRegistry: MagicVariableRegistry? = null
) {

    /**
     * 便捷入口：表达式字符串 → Lexer → Parser → 求值
     */
    suspend fun evaluateExpression(expression: String): Any? {
        val tokens = Lexer(expression).tokenize()
        val ast = Parser(tokens).parse()
        return evaluate(ast)
    }

    /**
     * 对 AST 节点递归求值
     */
    suspend fun evaluate(node: AstNode): Any? = when (node) {
        is AstNode.IntLiteral -> node.value
        is AstNode.DecimalLiteral -> node.value
        is AstNode.StringLiteral -> node.value
        is AstNode.BoolLiteral -> node.value
        is AstNode.VariableRef -> resolveVariable(node.name)
        is AstNode.BinaryOp -> evaluateBinaryOp(node)
        is AstNode.UnaryOp -> evaluateUnaryOp(node)
        is AstNode.FunctionCall -> evaluateFunctionCall(node)
    }

    // ---- 变量解析 ----

    private suspend fun resolveVariable(name: String): Any? {
        // 魔法变量：__name__ 模式
        if (name.startsWith("__") && name.endsWith("__") && magicVariableRegistry != null) {
            val resolved = magicVariableRegistry.resolve(name)
            if (resolved != null) return resolved.value
        }
        val variable = variableRepository.getVariable(name)
            ?: throw EvaluationException("未定义的变量: $name")
        return variable.value
    }

    // ---- 二元运算 ----

    private suspend fun evaluateBinaryOp(node: AstNode.BinaryOp): Any? {
        // 短路求值：逻辑运算符
        if (node.operator == "&&") {
            val left = requireBoolean(evaluate(node.left), "&&")
            return if (!left) false else requireBoolean(evaluate(node.right), "&&")
        }
        if (node.operator == "||") {
            val left = requireBoolean(evaluate(node.left), "||")
            return if (left) true else requireBoolean(evaluate(node.right), "||")
        }

        val left = evaluate(node.left)
        val right = evaluate(node.right)

        return when (node.operator) {
            "+" -> evalAdd(left, right)
            "-" -> evalArithmetic(left, right) { a, b -> a - b }
            "*" -> evalArithmetic(left, right) { a, b -> a * b }
            "/" -> evalDivide(left, right)
            "%" -> evalModulo(left, right)
            "==" -> left == right
            "!=" -> left != right
            ">", "<", ">=", "<=" -> evalComparison(left, right, node.operator)
            else -> throw EvaluationException("未知的运算符: ${node.operator}")
        }
    }

    private fun evalAdd(left: Any?, right: Any?): Any {
        if (left is String || right is String) {
            return "${left ?: "null"}${right ?: "null"}"
        }
        return evalArithmetic(left, right) { a, b -> a + b }
    }

    private fun evalArithmetic(left: Any?, right: Any?, op: (Double, Double) -> Double): Any {
        val l = toNumber(left, "算术运算")
        val r = toNumber(right, "算术运算")
        val result = op(l.toDouble(), r.toDouble())
        return if (l is Long && r is Long) result.toLong() else result
    }

    private fun evalDivide(left: Any?, right: Any?): Any {
        val l = toNumber(left, "除法")
        val r = toNumber(right, "除法")
        if (r.toDouble() == 0.0) throw EvaluationException("除零错误")
        val result = l.toDouble() / r.toDouble()
        return if (l is Long && r is Long) result.toLong() else result
    }

    private fun evalModulo(left: Any?, right: Any?): Any {
        val l = toNumber(left, "取模")
        val r = toNumber(right, "取模")
        if (r.toDouble() == 0.0) throw EvaluationException("除零错误")
        val result = l.toDouble() % r.toDouble()
        return if (l is Long && r is Long) result.toLong() else result
    }

    private fun evalComparison(left: Any?, right: Any?, op: String): Boolean {
        val l = toNumber(left, "比较运算").toDouble()
        val r = toNumber(right, "比较运算").toDouble()
        return when (op) {
            ">" -> l > r
            "<" -> l < r
            ">=" -> l >= r
            "<=" -> l <= r
            else -> throw EvaluationException("未知的比较运算符: $op")
        }
    }

    // ---- 一元运算 ----

    private suspend fun evaluateUnaryOp(node: AstNode.UnaryOp): Any? {
        val operand = evaluate(node.operand)
        return when (node.operator) {
            "-" -> {
                val n = toNumber(operand, "一元取负")
                if (n is Long) -n else -(n.toDouble())
            }
            "!" -> !requireBoolean(operand, "!")
            else -> throw EvaluationException("未知的一元运算符: ${node.operator}")
        }
    }

    // ---- 内置函数 ----

    private suspend fun evaluateFunctionCall(node: AstNode.FunctionCall): Any? {
        val args = node.arguments.map { evaluate(it) }
        return when (node.name) {
            "now" -> {
                expectArgCount("now", args, 0)
                System.currentTimeMillis()
            }
            "len" -> {
                expectArgCount("len", args, 1)
                requireString(args[0], "len()").length.toLong()
            }
            "str" -> {
                expectArgCount("str", args, 1)
                args[0]?.toString() ?: "null"
            }
            "int" -> {
                expectArgCount("int", args, 1)
                toInt(args[0])
            }
            "dec" -> {
                expectArgCount("dec", args, 1)
                toDec(args[0])
            }
            "abs" -> {
                expectArgCount("abs", args, 1)
                val n = toNumber(args[0], "abs()")
                if (n is Long) abs(n) else abs(n.toDouble())
            }
            "min" -> {
                expectArgCount("min", args, 2)
                val a = toNumber(args[0], "min()")
                val b = toNumber(args[1], "min()")
                val result = kotlin.math.min(a.toDouble(), b.toDouble())
                if (a is Long && b is Long) result.toLong() else result
            }
            "max" -> {
                expectArgCount("max", args, 2)
                val a = toNumber(args[0], "max()")
                val b = toNumber(args[1], "max()")
                val result = kotlin.math.max(a.toDouble(), b.toDouble())
                if (a is Long && b is Long) result.toLong() else result
            }
            "upper" -> {
                expectArgCount("upper", args, 1)
                requireString(args[0], "upper()").uppercase()
            }
            "lower" -> {
                expectArgCount("lower", args, 1)
                requireString(args[0], "lower()").lowercase()
            }
            "contains" -> {
                expectArgCount("contains", args, 2)
                requireString(args[0], "contains() 第一个参数").contains(
                    requireString(args[1], "contains() 第二个参数")
                )
            }
            "startsWith" -> {
                expectArgCount("startsWith", args, 2)
                requireString(args[0], "startsWith() 第一个参数").startsWith(
                    requireString(args[1], "startsWith() 第二个参数")
                )
            }
            "endsWith" -> {
                expectArgCount("endsWith", args, 2)
                requireString(args[0], "endsWith() 第一个参数").endsWith(
                    requireString(args[1], "endsWith() 第二个参数")
                )
            }
            "substring" -> {
                if (args.size !in 2..3) {
                    throw EvaluationException("substring() 需要 2-3 个参数，实际: ${args.size}")
                }
                val s = requireString(args[0], "substring() 第一个参数")
                val start = toNumber(args[1], "substring() start").toInt()
                if (args.size == 3) {
                    val end = toNumber(args[2], "substring() end").toInt()
                    s.substring(start, end)
                } else {
                    s.substring(start)
                }
            }
            else -> throw EvaluationException("未定义的函数: ${node.name}")
        }
    }

    // ---- 工具方法 ----

    private fun toNumber(value: Any?, context: String): Number = when (value) {
        is Long -> value
        is Double -> value
        is Int -> value.toLong()
        is Float -> value.toDouble()
        else -> throw EvaluationException(
            "${context}需要数字类型，实际: ${value?.let { it::class.simpleName } ?: "null"}"
        )
    }

    private fun toInt(value: Any?): Long = when (value) {
        is Long -> value
        is Double -> value.toLong()
        is String -> value.toLongOrNull()
            ?: throw EvaluationException("int(): 无法将 \"$value\" 转为整数")
        is Boolean -> if (value) 1L else 0L
        else -> throw EvaluationException(
            "int(): 不支持的类型 ${value?.let { it::class.simpleName }}"
        )
    }

    private fun toDec(value: Any?): Double = when (value) {
        is Double -> value
        is Long -> value.toDouble()
        is String -> value.toDoubleOrNull()
            ?: throw EvaluationException("dec(): 无法将 \"$value\" 转为小数")
        is Boolean -> if (value) 1.0 else 0.0
        else -> throw EvaluationException(
            "dec(): 不支持的类型 ${value?.let { it::class.simpleName }}"
        )
    }

    private fun requireBoolean(value: Any?, context: String): Boolean {
        if (value !is Boolean) {
            throw EvaluationException(
                "'$context' 运算符需要布尔类型，实际: ${value?.let { it::class.simpleName }}"
            )
        }
        return value
    }

    private fun requireString(value: Any?, context: String): String {
        if (value !is String) {
            throw EvaluationException("$context 需要字符串参数")
        }
        return value
    }

    private fun expectArgCount(name: String, args: List<Any?>, expected: Int) {
        if (args.size != expected) {
            throw EvaluationException("$name() 需要 $expected 个参数，实际: ${args.size}")
        }
    }
}
