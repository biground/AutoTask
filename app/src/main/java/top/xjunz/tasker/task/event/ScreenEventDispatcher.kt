/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 屏幕状态事件分发器，监听亮屏/息屏/解锁事件。
 */
class ScreenEventDispatcher : EventDispatcher() {

    private val context by lazy { ContextBridge.getContext() }

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleAction(intent?.action)
        }
    }

    /**
     * 根据广播 action 分发对应事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleAction(action: String?) {
        val event = when (action) {
            Intent.ACTION_SCREEN_ON -> Event.obtain(Event.EVENT_ON_SCREEN_ON)
            Intent.ACTION_SCREEN_OFF -> Event.obtain(Event.EVENT_ON_SCREEN_OFF)
            Intent.ACTION_USER_PRESENT -> Event.obtain(Event.EVENT_ON_SCREEN_UNLOCKED)
            else -> null
        }
        event?.let { dispatchEvents(it) }
    }

    override fun onRegistered() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(receiver, filter)
    }

    override fun destroy() {
        context.unregisterReceiver(receiver)
    }
}
