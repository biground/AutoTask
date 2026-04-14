/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 短信事件的结果引用，用于向后续 Applet 提供发送者和短信内容。
 */
data class SmsReferent(
    val sender: String,
    val body: String
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 短信引用本身
        0 -> this
        // 发送者
        1 -> sender
        // 短信内容
        2 -> body
        else -> super.getReferredValue(which, runtime)
    }
}
