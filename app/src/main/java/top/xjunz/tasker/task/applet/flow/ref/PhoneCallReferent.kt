/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 来电事件的结果引用，用于向后续 Applet 提供来电号码和通话状态。
 */
data class PhoneCallReferent(
    val phoneNumber: String,
    val callState: Int
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 来电引用本身
        0 -> this
        // 电话号码
        1 -> phoneNumber
        // 通话状态
        2 -> callState
        else -> super.getReferredValue(which, runtime)
    }
}
