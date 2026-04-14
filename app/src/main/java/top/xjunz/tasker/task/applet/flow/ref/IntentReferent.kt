/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * Intent 事件的结果引用，用于向后续 Applet 提供 Intent action 和 data URI。
 */
data class IntentReferent(
    val action: String,
    val dataUri: String
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // Intent 引用本身
        0 -> this
        // action
        1 -> action
        // data URI
        2 -> dataUri
        else -> super.getReferredValue(which, runtime)
    }
}
