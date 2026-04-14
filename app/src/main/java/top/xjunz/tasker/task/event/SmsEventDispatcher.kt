/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 短信事件分发器，监听收到短信事件并解析发送者和内容。
 */
open class SmsEventDispatcher : EventDispatcher() {

    companion object {
        private const val TAG = "SmsEventDispatcher"
        /** SparseArray key: 短信发送者 */
        const val EXTRA_SMS_SENDER = 0
        /** SparseArray key: 短信内容 */
        const val EXTRA_SMS_BODY = 1
    }

    private val context by lazy { ContextBridge.getContext() }

    internal var receiverRegistered = false
        private set

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return
            val sender = messages[0].displayOriginatingAddress ?: ""
            val body = messages.joinToString("") { it.displayMessageBody ?: "" }
            handleSmsReceived(sender, body)
        }
    }

    /**
     * 检查短信权限。internal open 以便纯 JVM 测试覆写。
     */
    internal open fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 处理收到短信事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleSmsReceived(sender: String, body: String) {
        val event = Event.obtain(Event.EVENT_ON_SMS_RECEIVED).apply {
            putExtra(EXTRA_SMS_SENDER, sender)
            putExtra(EXTRA_SMS_BODY, body)
        }
        dispatchEvents(event)
    }

    override fun onRegistered() {
        if (!hasSmsPermission()) {
            Log.w(TAG, "缺少 RECEIVE_SMS 权限，跳过短信事件监听")
            return
        }
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        context.registerReceiver(receiver, filter)
        receiverRegistered = true
    }

    override fun destroy() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                // receiver 已注销
            }
            receiverRegistered = false
        }
    }
}
