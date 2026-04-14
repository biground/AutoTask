/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import top.xjunz.tasker.R
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.ui.main.MainActivity

/**
 * 前台服务通知管理器。
 * 负责创建通知频道、构建和更新前台服务通知。
 */
object ForegroundNotificationManager {

    const val NOTIFICATION_ID = 1001

    private const val CHANNEL_ID = "autopilot_service"
    private const val ACTION_TOGGLE_PAUSE = "top.xjunz.tasker.ACTION_TOGGLE_PAUSE"

    private val context: Context get() = ContextBridge.getContext()

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    /**
     * 创建前台服务通知频道（IMPORTANCE_LOW，不发出声音）。
     */
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_service),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 构建前台服务通知。
     *
     * @param activeMacroCount 当前活跃宏数量
     * @param isPaused 服务是否处于暂停状态
     */
    fun buildForegroundNotification(activeMacroCount: Int, isPaused: Boolean): Notification {
        val title = if (isPaused) {
            context.getString(R.string.autopilot_paused)
        } else {
            context.getString(R.string.autopilot_running)
        }
        val content = context.getString(R.string.n_macros_active, activeMacroCount)

        // 点击通知跳转 MainActivity
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 暂停/恢复 action button
        val toggleAction = buildTogglePauseAction(isPaused)

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_task_alt_24)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .addAction(toggleAction)
            .build()
    }

    /**
     * 更新前台服务通知。
     *
     * @param activeMacroCount 当前活跃宏数量
     * @param isPaused 服务是否处于暂停状态
     */
    fun updateNotification(activeMacroCount: Int, isPaused: Boolean) {
        val notification = buildForegroundNotification(activeMacroCount, isPaused)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 构建暂停/恢复切换按钮。
     */
    private fun buildTogglePauseAction(isPaused: Boolean): Notification.Action {
        val actionLabel = if (isPaused) {
            context.getString(R.string.action_resume)
        } else {
            context.getString(R.string.action_pause)
        }
        val intent = Intent(ACTION_TOGGLE_PAUSE).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Action.Builder(null, actionLabel, pendingIntent).build()
    }
}
