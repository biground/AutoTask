/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 闹钟事件的结果引用，用于向后续 Applet 提供闹钟触发时间。
 */
data class AlarmReferent(
    val triggerTimeMillis: Long
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 闹钟引用本身
        0 -> this
        // 触发时间戳
        1 -> triggerTimeMillis
        else -> super.getReferredValue(which, runtime)
    }
}
