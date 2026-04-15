/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 通知事件分发器，暴露 handleNotificationPosted() 供 AutoPilotNotificationListenerService 调用。
 */
class NotificationEventDispatcher : EventDispatcher() {

    companion object {
        /** SparseArray key: 通知标题 */
        const val EXTRA_NOTIF_TITLE = 0
        /** SparseArray key: 通知正文 */
        const val EXTRA_NOTIF_TEXT = 1
        /** SparseArray key: 通知副文本 */
        const val EXTRA_NOTIF_SUB_TEXT = 2
        /** SparseArray key: 通知发布时间 */
        const val EXTRA_NOTIF_POST_TIME = 3

        @Volatile
        var instance: NotificationEventDispatcher? = null
            private set
    }

    fun handleNotificationPosted(
        packageName: String,
        title: String?,
        text: String?,
        subText: String?,
        postTime: Long
    ) {
        val event = Event.obtain(
            Event.EVENT_ON_NOTIFICATION_RECEIVED,
            packageName
        ).apply {
            putExtra(EXTRA_NOTIF_TITLE, title ?: "")
            putExtra(EXTRA_NOTIF_TEXT, text ?: "")
            putExtra(EXTRA_NOTIF_SUB_TEXT, subText ?: "")
            putExtra(EXTRA_NOTIF_POST_TIME, postTime)
        }
        dispatchEvents(event)
    }

    override fun onRegistered() {
        instance = this
    }

    override fun destroy() {
        instance = null
    }
}
