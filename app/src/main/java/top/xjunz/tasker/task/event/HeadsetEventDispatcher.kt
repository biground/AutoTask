/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 耳机插拔事件分发器，监听有线耳机插入/拔出事件。
 */
class HeadsetEventDispatcher : EventDispatcher() {

    private val context by lazy { ContextBridge.getContext() }

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                handleHeadsetState(state)
            }
        }
    }

    /**
     * 根据耳机状态值分发对应事件。提取为 internal 方法以便纯 JVM 测试。
     * @param state 1=插入, 0=拔出, 其他=忽略
     */
    internal fun handleHeadsetState(state: Int) {
        val event = when (state) {
            1 -> Event.obtain(Event.EVENT_ON_HEADSET_PLUGGED)
            0 -> Event.obtain(Event.EVENT_ON_HEADSET_UNPLUGGED)
            else -> null
        }
        event?.let { dispatchEvents(it) }
    }

    override fun onRegistered() {
        val filter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        context.registerReceiver(receiver, filter)
    }

    override fun destroy() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) { }
    }
}
