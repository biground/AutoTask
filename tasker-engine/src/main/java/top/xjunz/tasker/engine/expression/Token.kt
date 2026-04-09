package top.xjunz.tasker.engine.expression

/**
 * 表达式引擎的词法单元密封类。
 * Lexer 将表达式字符串拆分为 Token 列表，供 Parser 消费。
 */
sealed class Token {

    // ---- 字面量 ----

    /** 整数字面量 */
    data class IntLiteral(val value: Long) : Token()

    /** 十进制小数字面量 */
    data class DecimalLiteral(val value: Double) : Token()

    /** 字符串字面量（已解转义） */
    data class StringLiteral(val value: String) : Token()

    /** 布尔字面量 */
    data class BoolLiteral(val value: Boolean) : Token()

    // ---- 引用 ----

    /** 变量引用：`${varName}` */
    data class VariableRef(val name: String) : Token()

    // ---- 运算符 ----

    /** 运算符：+  -  *  /  %  ==  !=  <  >  <=  >=  &&  ||  ! */
    data class Operator(val symbol: String) : Token()

    // ---- 标点 ----

    /** 左括号 `(` */
    object LeftParen : Token() {
        override fun toString(): String = "LeftParen"
    }

    /** 右括号 `)` */
    object RightParen : Token() {
        override fun toString(): String = "RightParen"
    }

    /** 逗号 `,` */
    object Comma : Token() {
        override fun toString(): String = "Comma"
    }

    // ---- 标识符 ----

    /** 标识符（函数名等） */
    data class Identifier(val name: String) : Token()

    // ---- 结束标记 ----

    /** 输入结束 */
    object EOF : Token() {
        override fun toString(): String = "EOF"
    }
}
