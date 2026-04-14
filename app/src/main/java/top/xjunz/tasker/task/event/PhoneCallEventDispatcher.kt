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
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 来电事件分发器，监听电话状态变化并获取来电号码。
 * 使用 TelephonyCallback (API 31+) 监听 callState，
 * 同时注册 BroadcastReceiver 监听 ACTION_PHONE_STATE 获取来电号码。
 */
open class PhoneCallEventDispatcher : EventDispatcher() {

    companion object {
        private const val TAG = "PhoneCallEventDispatcher"
        /** SparseArray key: 电话号码 */
        const val EXTRA_PHONE_NUMBER = 0
        /** SparseArray key: 通话状态 (TelephonyManager.CALL_STATE_*) */
        const val EXTRA_CALL_STATE = 1
    }

    private val context by lazy { ContextBridge.getContext() }

    private var telephonyCallback: TelephonyCallback? = null
    internal var callbackRegistered = false
        private set

    internal var receiverRegistered = false
        private set

    /** 缓存最近一次来电号码（从 BroadcastReceiver 获取） */
    @Volatile
    private var lastIncomingNumber: String = ""

    internal val phoneStateReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (!number.isNullOrEmpty()) {
                    lastIncomingNumber = number
                }
            }
        }
    }

    /**
     * 检查来电权限。internal open 以便纯 JVM 测试覆写。
     */
    internal open fun hasPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 处理通话状态变化事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleCallStateChanged(state: Int, phoneNumber: String) {
        val event = Event.obtain(Event.EVENT_ON_CALL_STATE_CHANGED).apply {
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_CALL_STATE, state)
        }
        dispatchEvents(event)
    }

    @Suppress("DEPRECATION")
    override fun onRegistered() {
        if (!hasPhonePermission()) {
            Log.w(TAG, "缺少 READ_PHONE_STATE 权限，跳过来电事件监听")
            return
        }

        // 注册 BroadcastReceiver 获取来电号码
        val phoneFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(phoneStateReceiver, phoneFilter)
        receiverRegistered = true

        // 注册 TelephonyCallback 获取通话状态
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                handleCallStateChanged(state, lastIncomingNumber)
                // IDLE 状态时清空缓存号码
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    lastIncomingNumber = ""
                }
            }
        }
        telephonyCallback = callback
        telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
        callbackRegistered = true
    }

    override fun destroy() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(phoneStateReceiver)
            } catch (_: IllegalArgumentException) {
                // receiver 已注销
            }
            receiverRegistered = false
        }
        if (callbackRegistered) {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            telephonyCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
            telephonyCallback = null
            callbackRegistered = false
        }
    }
}
