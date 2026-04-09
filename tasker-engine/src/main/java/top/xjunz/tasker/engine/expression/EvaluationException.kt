package top.xjunz.tasker.engine.expression

/**
 * 表达式求值异常 — 明确表达式求值中遇到的问题
 * （类型不匹配、未定义变量、除零等）
 */
class EvaluationException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
