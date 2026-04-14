/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import top.xjunz.tasker.R
import java.util.concurrent.atomic.AtomicInteger

/**
 * NotificationManager 的桥接封装。
 */
object NotificationManagerBridge {

    private const val CHANNEL_ID = "autopilot_macro_actions"
    private const val CHANNEL_NAME = "AutoPilot 宏操作通知"

    private val notificationIdCounter = AtomicInteger(0)

    private val context get() = ContextBridge.getContext()

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(title: String, content: String) {
        ensureChannel()
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_task_alt_24)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationIdCounter.incrementAndGet(), notification)
    }

    fun setInterruptionFilter(filter: Int) {
        notificationManager.setInterruptionFilter(filter)
    }
}
