package top.xjunz.tasker.engine.variable

// 变量变化事件密封类
sealed class VariableChangeEvent {
    data class Set(val variable: Variable, val oldValue: Any?) : VariableChangeEvent()
    data class Deleted(val name: String) : VariableChangeEvent()
}
