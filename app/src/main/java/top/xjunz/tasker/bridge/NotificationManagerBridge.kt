/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.app.NotificationManager

/**
 * NotificationManager 的桥接封装。
 */
object NotificationManagerBridge {

    private val notificationManager: NotificationManager by lazy {
        ContextBridge.getContext().getSystemService(NotificationManager::class.java)
    }

    fun setInterruptionFilter(filter: Int) {
        notificationManager.setInterruptionFilter(filter)
    }
}
