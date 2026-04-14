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
 * 电源连接事件分发器，监听充电器插入/拔出事件。
 */
class PowerEventDispatcher : EventDispatcher() {

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
            Intent.ACTION_POWER_CONNECTED -> Event.obtain(Event.EVENT_ON_POWER_CONNECTED)
            Intent.ACTION_POWER_DISCONNECTED -> Event.obtain(Event.EVENT_ON_POWER_DISCONNECTED)
            else -> null
        }
        event?.let { dispatchEvents(it) }
    }

    override fun onRegistered() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
    }

    override fun destroy() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) { }
    }
}
