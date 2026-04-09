package top.xjunz.tasker.engine.variable

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 变量系统到 Referent 系统的桥接适配器。
 *
 * 通过 [NAMESPACE_PREFIX] ("var:") 前缀命名空间区分变量引用和其他引用。
 * 例如：var:myVariable → 从 VariableRepository 获取 myVariable 的值。
 *
 * @param variableName 变量名称
 * @param valueResolver 值解析函数，延迟获取变量的最新值
 */
class VariableReferent(
    val variableName: String,
    private val valueResolver: () -> Any?
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? {
        return when (which) {
            // 变量当前值
            0 -> valueResolver()
            // 变量名称
            1 -> variableName
            else -> super.getReferredValue(which, runtime)
        }
    }

    companion object {
        const val NAMESPACE_PREFIX = "var:"

        /**
         * 为变量名生成带 "var:" 前缀的引用名称，用于 Applet.referents 注册。
         */
        fun referentName(variableName: String): String = "$NAMESPACE_PREFIX$variableName"
    }
}
