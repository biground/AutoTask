/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 蓝牙事件的结果引用，用于向后续 Applet 提供蓝牙设备名和 MAC 地址。
 */
data class BluetoothReferent(
    val deviceName: String,
    val macAddress: String
) : Referent {

    override fun toString(): String =
        "BluetoothReferent(deviceName=$deviceName, macAddress=${macAddress.take(8)}:XX:XX:XX)"

    override fun getReferredValue(which: Int, runtime: TaskRuntime): Any? = when (which) {
        // 蓝牙引用本身
        0 -> this
        // 设备名称
        1 -> deviceName
        // MAC 地址
        2 -> macAddress
        else -> super.getReferredValue(which, runtime)
    }
}
