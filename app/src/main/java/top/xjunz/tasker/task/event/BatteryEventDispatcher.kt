/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.task.applet.flow.ref.BatteryReferent

/**
 * 电池电量事件分发器，监听 ACTION_BATTERY_CHANGED 广播，
 * 当电量百分比相较上次变化 ≥1% 时分发事件。
 *
 * 注意：ACTION_BATTERY_CHANGED 是 sticky broadcast，
 * 注册后会立即收到一次回调，首次仅记录电量而不分发事件。
 */
class BatteryEventDispatcher : EventDispatcher() {

    private val context by lazy { ContextBridge.getContext() }

    /** 上次记录的电量百分比，-1 表示尚未初始化 */
    @Volatile
    private var lastLevel: Int = -1

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            handleBatteryChanged(level, scale)
        }
    }

    /** 最近一次分发的 BatteryReferent，供纯 JVM 测试验证 */
    @Volatile
    internal var lastDispatchedReferent: BatteryReferent? = null
        private set

    /**
     * 处理电量变化逻辑。提取为 internal 方法以便纯 JVM 测试。
     *
     * @param level 电池当前电量原始值
     * @param scale 电池满电量值
     */
    internal fun handleBatteryChanged(level: Int, scale: Int) {
        if (scale <= 0) return // 避免除零
        val percent = ((level * 100) / scale).coerceIn(0, 100)

        if (lastLevel < 0) {
            // sticky broadcast 首次回调：仅记录，不分发
            lastLevel = percent
            return
        }

        if (percent == lastLevel) return // 无变化，不分发

        lastLevel = percent
        val referent = BatteryReferent(percent)
        lastDispatchedReferent = referent
        val event = Event.obtain(Event.EVENT_ON_BATTERY_LEVEL_CHANGED)
        event.putExtra(0, referent)
        dispatchEvents(event)
    }

    override fun onRegistered() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    override fun destroy() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) { }
    }
}
