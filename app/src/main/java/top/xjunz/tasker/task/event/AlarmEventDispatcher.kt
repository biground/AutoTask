/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.event

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import java.util.Calendar

/**
 * 闹钟事件分发器，支持一次性精确闹钟和周期性闹钟。
 */
open class AlarmEventDispatcher : EventDispatcher() {

    companion object {
        private const val TAG = "AlarmEventDispatcher"
        const val ACTION_ALARM_FIRED = "top.xjunz.tasker.ALARM_FIRED"
        /** SparseArray key: 闹钟触发时间戳 */
        const val EXTRA_ALARM_TRIGGER_TIME = 0
    }

    /** 周期性闹钟重复规则 */
    sealed class RepeatRule {
        object Daily : RepeatRule()
        /** @param daysOfWeek Calendar.MONDAY..Calendar.SATURDAY 等 */
        data class Weekly(val daysOfWeek: Set<Int>) : RepeatRule()
        /** @param daysOfMonth 1-31 */
        data class Monthly(val daysOfMonth: Set<Int>) : RepeatRule()
    }

    private val context by lazy { ContextBridge.getContext() }
    private var repeatRule: RepeatRule? = null
    internal var receiverRegistered = false
        private set

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ALARM_FIRED) {
                val triggerTime = System.currentTimeMillis()
                handleAlarmFired(triggerTime)
                // 如果有重复规则，计算下次触发时间并重新调度
                repeatRule?.let { rule ->
                    val nextTime = computeNextTriggerTime(rule, triggerTime)
                    scheduleAlarm(nextTime)
                }
            }
        }
    }

    /**
     * 处理闹钟触发事件。提取为 internal 方法以便纯 JVM 测试。
     */
    internal fun handleAlarmFired(triggerTime: Long) {
        val event = Event.obtain(Event.EVENT_ON_ALARM_FIRED).apply {
            putExtra(EXTRA_ALARM_TRIGGER_TIME, triggerTime)
        }
        dispatchEvents(event)
    }

    /**
     * 根据重复规则和上次触发时间，计算下一次触发时间。
     */
    internal fun computeNextTriggerTime(rule: RepeatRule, lastTrigger: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = lastTrigger }
        when (rule) {
            is RepeatRule.Daily -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            is RepeatRule.Weekly -> {
                do {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } while (calendar.get(Calendar.DAY_OF_WEEK) !in rule.daysOfWeek)
            }
            is RepeatRule.Monthly -> {
                do {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } while (calendar.get(Calendar.DAY_OF_MONTH) !in rule.daysOfMonth)
            }
        }
        return calendar.timeInMillis
    }

    /**
     * 调度一次性精确闹钟。
     */
    fun scheduleAlarm(timeInMillis: Long) {
        require(timeInMillis > System.currentTimeMillis()) { "闹钟时间必须在未来" }
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "无法调度精确闹钟，降级为非精确闹钟")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, timeInMillis, createPendingIntent()
            )
            return
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, timeInMillis, createPendingIntent()
        )
    }

    /**
     * 调度周期性闹钟。首次在 [startTimeMillis] 触发，之后按 [rule] 重复。
     */
    fun scheduleRepeating(startTimeMillis: Long, rule: RepeatRule) {
        this.repeatRule = rule
        scheduleAlarm(startTimeMillis)
    }

    /**
     * 取消当前闹钟。
     */
    fun cancelAlarm() {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(createPendingIntent())
        repeatRule = null
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(ACTION_ALARM_FIRED)
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onRegistered() {
        val filter = IntentFilter(ACTION_ALARM_FIRED)
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
        cancelAlarm()
    }
}
