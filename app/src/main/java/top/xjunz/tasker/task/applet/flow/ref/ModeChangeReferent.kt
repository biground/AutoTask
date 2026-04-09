/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 模式变化事件的结果引用，用于向后续 Applet 提供模式名称、变化类型等。
 *
 * @see top.xjunz.tasker.task.applet.option.registry.EventCriterionRegistry.modeChanged
 */
class ModeChangeReferent(
    val modeName: String,
    val changeType: String,
    val previousModeName: String
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? {
        return when (which) {
            // 模式变化引用本身
            0 -> this
            // 变化的模式名称
            1 -> modeName
            // 变化类型（activated / deactivated）
            2 -> changeType
            // 前一个模式名称
            3 -> previousModeName
            else -> super.getReferredValue(which, runtime)
        }
    }
}
