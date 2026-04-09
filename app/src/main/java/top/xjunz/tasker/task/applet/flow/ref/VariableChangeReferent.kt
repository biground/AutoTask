/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 变量变化事件的结果引用，用于向后续 Applet 提供变量名、旧值、新值。
 *
 * @see top.xjunz.tasker.task.applet.option.registry.EventCriterionRegistry.variableChanged
 */
class VariableChangeReferent(
    val variableName: String,
    val oldValue: Any?,
    val newValue: Any?
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? {
        return when (which) {
            // 变量变化引用本身
            0 -> this
            // 变化的变量名
            1 -> variableName
            // 变量旧值
            2 -> oldValue?.toString() ?: ""
            // 变量新值
            3 -> newValue?.toString() ?: ""
            else -> super.getReferredValue(which, runtime)
        }
    }
}
