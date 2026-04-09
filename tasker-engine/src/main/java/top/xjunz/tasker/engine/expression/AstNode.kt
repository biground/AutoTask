package top.xjunz.tasker.engine.expression

/**
 * 表达式引擎的抽象语法树节点密封类。
 * Parser 将 Token 列表解析为 AstNode 树结构。
 */
sealed class AstNode {

    // ---- 字面量节点 ----

    /** 整数字面量 */
    data class IntLiteral(val value: Long) : AstNode()

    /** 十进制小数字面量 */
    data class DecimalLiteral(val value: Double) : AstNode()

    /** 字符串字面量 */
    data class StringLiteral(val value: String) : AstNode()

    /** 布尔字面量 */
    data class BoolLiteral(val value: Boolean) : AstNode()

    // ---- 引用节点 ----

    /** 变量引用 */
    data class VariableRef(val name: String) : AstNode()

    // ---- 运算节点 ----

    /** 二元运算 */
    data class BinaryOp(val operator: String, val left: AstNode, val right: AstNode) : AstNode()

    /** 一元运算（-x, !x） */
    data class UnaryOp(val operator: String, val operand: AstNode) : AstNode()

    // ---- 函数调用节点 ----

    /** 函数调用 */
    data class FunctionCall(val name: String, val arguments: List<AstNode>) : AstNode()
}
