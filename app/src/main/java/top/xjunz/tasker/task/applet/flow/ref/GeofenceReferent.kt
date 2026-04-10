/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 地理围栏事件的结果引用，用于向后续 Applet 提供围栏名称、坐标、转场类型。
 */
class GeofenceReferent(
    val name: String,
    val lat: Double,
    val lng: Double,
    val transition: String
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? {
        return when (which) {
            // 围栏引用本身
            0 -> this
            // 围栏名称
            1 -> name
            // 纬度
            2 -> lat
            // 经度
            3 -> lng
            // 转场类型（ENTER / EXIT）
            4 -> transition
            else -> super.getReferredValue(which, runtime)
        }
    }
}
