/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import java.util.concurrent.ConcurrentHashMap

/**
 * 通用 Intent 事件分发器，支持动态注册任意 action 的广播监听。
 */
open class IntentEventDispatcher : EventDispatcher() {

    companion object {
        private const val TAG = "IntentEventDispatcher"
        /** SparseArray key: Intent action */
        const val EXTRA_INTENT_ACTION = 0
        /** SparseArray key: Intent data URI */
        const val EXTRA_INTENT_DATA_URI = 1
    }

    private val context by lazy { ContextBridge.getContext() }

    internal val receivers = ConcurrentHashMap<String, BroadcastReceiver>()

    /**
     * 处理收到 Intent 事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleIntentReceived(action: String, dataUri: String) {
        val event = Event.obtain(Event.EVENT_ON_INTENT_RECEIVED).apply {
            putExtra(EXTRA_INTENT_ACTION, action)
            putExtra(EXTRA_INTENT_DATA_URI, dataUri)
        }
        dispatchEvents(event)
    }

    /**
     * 注册监听指定 action 的广播。
     * @param action 要监听的 Intent action，不能为空白字符串
     */
    fun registerAction(action: String) {
        if (action.isBlank()) {
            Log.w(TAG, "action 为空白字符串，拒绝注册")
            return
        }
        if (receivers.containsKey(action)) {
            Log.d(TAG, "action 已注册: $action")
            return
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                handleIntentReceived(
                    intent.action ?: "",
                    intent.data?.toString() ?: ""
                )
            }
        }
        context.registerReceiver(receiver, IntentFilter(action))
        receivers[action] = receiver
    }

    /**
     * 注销指定 action 的广播监听。
     */
    fun unregisterAction(action: String) {
        val receiver = receivers.remove(action) ?: return
        try {
            context.unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {
            // receiver 已注销
        }
    }

    override fun onRegistered() {
        // 空实现：actions 由用户后续通过 registerAction 配置
    }

    override fun destroy() {
        val iterator = receivers.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            try {
                context.unregisterReceiver(entry.value)
            } catch (_: Exception) { }
            iterator.remove()
        }
    }
}
