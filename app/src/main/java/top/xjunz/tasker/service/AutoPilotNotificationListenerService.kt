/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import top.xjunz.tasker.task.event.NotificationEventDispatcher

/**
 * 通知监听服务，桥接系统通知到 NotificationEventDispatcher。
 * 需要用户在系统设置中手动授权「通知访问」权限。
 */
class AutoPilotNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        var isConnected: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        isConnected = true
    }

    override fun onListenerDisconnected() {
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val packageName = sbn.packageName ?: return
        // 过滤自身通知，防止循环
        if (packageName == applicationContext.packageName) return

        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val subText = extras.getCharSequence("android.subText")?.toString()
        val postTime = sbn.postTime

        NotificationEventDispatcher.instance
            ?.handleNotificationPosted(packageName, title, text, subText, postTime)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // MVP 阶段暂不处理通知移除事件
    }
}
