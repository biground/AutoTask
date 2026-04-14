/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 蓝牙事件分发器，监听蓝牙状态变化和设备连接/断开事件。
 */
open class BluetoothEventDispatcher : EventDispatcher() {

    companion object {
        private const val TAG = "BluetoothEventDispatcher"
        /** SparseArray key: 蓝牙设备名 */
        const val EXTRA_BT_DEVICE_NAME = 0
        /** SparseArray key: 蓝牙 MAC 地址 */
        const val EXTRA_BT_MAC_ADDRESS = 1
    }

    private val context by lazy { ContextBridge.getContext() }

    internal var receiverRegistered = false
        private set

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            @Suppress("DEPRECATION")
            val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            handleBluetoothEvent(action, device?.name, device?.address)
        }
    }

    /**
     * 检查蓝牙连接权限。internal open 以便纯 JVM 测试覆写。
     */
    internal open fun hasBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 根据蓝牙广播 action 和设备信息分发对应事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleBluetoothEvent(action: String?, deviceName: String?, macAddress: String?) {
        val eventType = when (action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> Event.EVENT_ON_BT_STATE_CHANGED
            BluetoothDevice.ACTION_ACL_CONNECTED -> Event.EVENT_ON_BT_DEVICE_CONNECTED
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> Event.EVENT_ON_BT_DEVICE_DISCONNECTED
            else -> return
        }
        val event = Event.obtain(eventType).apply {
            putExtra(EXTRA_BT_DEVICE_NAME, deviceName ?: "")
            putExtra(EXTRA_BT_MAC_ADDRESS, macAddress ?: "")
        }
        dispatchEvents(event)
    }

    override fun onRegistered() {
        if (!hasBluetoothPermission()) {
            Log.w(TAG, "缺少 BLUETOOTH_CONNECT 权限，跳过蓝牙事件监听")
            return
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
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
