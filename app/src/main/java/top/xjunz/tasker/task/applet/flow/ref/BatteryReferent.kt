/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 电池事件的结果引用，用于向后续 Applet 提供电池电量百分比。
 */
data class BatteryReferent(
    val batteryLevel: Int
) : Referent {

    override fun toString(): String =
        "BatteryReferent(batteryLevel=$batteryLevel%)"

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 电池引用本身
        0 -> this
        // 电量百分比
        1 -> batteryLevel
        else -> super.getReferredValue(which, runtime)
    }
}
