/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.task.applet.action.HttpRequestAction
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.flow.ref.HttpResponseReferent

/**
 * 网络与内部操作相关动作的 Registry。
 */
class NetworkActionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val httpRequest = appletOption(R.string.http_request) {
        HttpRequestAction()
    }.withValueArgument<String>(R.string.http_url)
        .withValueArgument<Int>(R.string.http_method)
        .withValueArgument<String>(R.string.http_headers)
        .withValueArgument<String>(R.string.http_body)
        .withResult<HttpResponseReferent>(R.string.http_request)
        .withResult<Int>(R.string.http_response_code)
        .withResult<String>(R.string.http_response_body)
}
