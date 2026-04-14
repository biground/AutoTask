/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * HTTP 响应的结果引用，向后续 Applet 提供状态码和响应体。
 */
data class HttpResponseReferent(
    val statusCode: Int,
    val responseBody: String
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 引用本身
        0 -> this
        // 状态码
        1 -> statusCode
        // 响应体
        2 -> responseBody
        else -> super.getReferredValue(which, runtime)
    }
}
