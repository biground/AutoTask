package top.xjunz.tasker.engine.variable

/**
 * 魔术变量提供者接口
 * 魔术变量是系统内置的只读变量，命名约定 __name__
 * 子类实现具体的值获取逻辑
 */
interface MagicVariableProvider {
    /** 魔术变量名（如 __current_time__） */
    val name: String
    /** 变量类型 */
    val type: VariableType
    /** 获取当前值（可能每次调用都不同） */
    fun getCurrentValue(): Any?
}
