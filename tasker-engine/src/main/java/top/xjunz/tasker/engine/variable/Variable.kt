package top.xjunz.tasker.engine.variable

// 类型化变量数据类
data class Variable(
    val id: String,
    val name: String,
    val type: VariableType,
    val value: Any? = null,
    val scope: VariableScope = VariableScope.LOCAL,
    val persisted: Boolean = false
)
