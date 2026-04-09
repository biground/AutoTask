package top.xjunz.tasker.task.applet.util

import top.xjunz.tasker.engine.expression.Evaluator
import top.xjunz.tasker.engine.variable.MagicVariableRegistry
import top.xjunz.tasker.engine.variable.VariableRepository

/**
 * 表达式求值辅助类 — 封装 Lexer → Parser → Evaluator 管线
 */
object ExpressionHelper {

    // 空格分隔的算术运算符
    private val SPACED_ARITHMETIC = Regex("""\s[+\-*/%]\s""")
    // 比较/逻辑运算符
    private val COMPARISON_LOGIC = Regex("""[=!<>]=|&&|\|\|""")
    // 空格分隔的 < 或 >
    private val SPACED_RELATIONAL = Regex("""\s[<>]\s""")
    // 函数调用模式
    private val FUNCTION_CALL = Regex("""\w+\s*\(""")

    /**
     * 判断一个字符串是否可能是表达式。
     * 简单启发式：包含运算符、变量引用 ${...} 或函数调用 xxx(...)
     */
    fun isExpression(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return false
        // 纯数字字面量（整数或小数，含负数）
        if (trimmed.toLongOrNull() != null || trimmed.toDoubleOrNull() != null) return false
        // 纯布尔字面量
        if (trimmed == "true" || trimmed == "false") return false
        // 变量引用
        if (trimmed.contains("\${")) return true
        // 函数调用
        if (FUNCTION_CALL.containsMatchIn(trimmed)) return true
        // 算术运算符（两边有空格）
        if (SPACED_ARITHMETIC.containsMatchIn(trimmed)) return true
        // 比较/逻辑运算符
        if (COMPARISON_LOGIC.containsMatchIn(trimmed)) return true
        // 单字符比较运算符（两边有空格）
        if (SPACED_RELATIONAL.containsMatchIn(trimmed)) return true
        return false
    }

    /**
     * 求值表达式。
     * @return 表达式结果，或 null 如果语法错误
     */
    suspend fun evaluateExpression(
        expression: String,
        variableRepository: VariableRepository,
        magicVariableRegistry: MagicVariableRegistry? = null
    ): Any? {
        return try {
            val evaluator = Evaluator(variableRepository, magicVariableRegistry)
            evaluator.evaluateExpression(expression)
        } catch (_: Exception) {
            null
        }
    }
}
