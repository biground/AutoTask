/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.applet.option.registry.EventCriterionRegistry

/**
 * @see [EventCriterionRegistry.notificationReceived]
 *
 * @author xjunz 2023/02/12
 */
class NotificationReferent(
    private val componentInfo: ComponentInfoWrapper,
    val title: String? = null,
    val text: String? = null,
    val subText: String? = null,
    val postTime: Long = 0L
) : Referent {

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? {
        return when (which) {
            0 -> this
            // 通知内容（paneTitle）
            1 -> componentInfo.paneTitle
            // 发送通知的 ComponentInfo
            2 -> componentInfo
            3 -> componentInfo.label
            // 通知标题
            4 -> title
            // 通知正文
            5 -> text
            // 通知副标题
            6 -> subText
            // 通知时间戳
            7 -> postTime
            else -> super.getReferredValue(which, runtime)
        }
    }

    override fun toString(): String {
        val t = title?.take(10)?.let { if (title.length > 10) "$it..." else it }
        val tx = text?.take(10)?.let { if (text.length > 10) "$it..." else it }
        val st = subText?.take(10)?.let { if (subText.length > 10) "$it..." else it }
        return "NotificationReferent(title=$t, text=$tx, subText=$st, postTime=$postTime)"
    }
}