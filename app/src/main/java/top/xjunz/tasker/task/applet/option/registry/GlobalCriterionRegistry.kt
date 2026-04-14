/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import android.app.KeyguardManager
import android.content.res.Configuration
import android.telephony.TelephonyManager
import top.xjunz.tasker.R
import top.xjunz.tasker.bridge.AudioManagerBridge
import top.xjunz.tasker.bridge.BatteryManagerBridge
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.bridge.DisplayManagerBridge
import top.xjunz.tasker.engine.applet.criterion.booleanCriterion
import top.xjunz.tasker.engine.applet.criterion.unaryEqualCriterion
import top.xjunz.tasker.ktx.format
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.NumberRangeCriterion.Companion.simpleNumberRangeCriterion
import top.xjunz.tasker.task.applet.value.VariantArgType

/**
 * @author xjunz 2022/11/10
 */
class GlobalCriterionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val isScreenPortrait = invertibleAppletOption(R.string.screen_orientation_portrait) {
        booleanCriterion {
            val realSize = DisplayManagerBridge.size
            realSize.x < realSize.y
        }
    }

    @AppletOrdinal(0x0010)
    val isBatteryCharging = invertibleAppletOption(R.string.is_charging) {
        booleanCriterion {
            BatteryManagerBridge.isCharging
        }
    }

    @AppletOrdinal(0x0011)
    val batteryCapacityRange = invertibleAppletOption(R.string.in_battery_capacity_range) {
        simpleNumberRangeCriterion {
            BatteryManagerBridge.capacity
        }
    }.withValueArgument<Int>(
        R.string.in_battery_capacity_range, VariantArgType.INT_PERCENT, true
    ).withSingleValueDescriber<Collection<Int?>> {
        val first = it.firstOrNull()
        val last = it.lastOrNull()
        if (first == null && last != null) {
            R.string.format_percent_less_than.format(last)
        } else if (last == null && first != null) {
            R.string.format_percent_larger_than.format(first)
        } else if (last == first) {
            "$first%"
        } else {
            R.string.format_percent_range.format(first, last)
        }
    }

    @AppletOrdinal(0x0012)
    val isDeviceLocked = invertibleAppletOption(R.string.criterion_is_device_locked) {
        booleanCriterion {
            val context = ContextBridge.getContext()
            context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
        }
    }

    @AppletOrdinal(0x0013)
    val isHeadphoneConnected = invertibleAppletOption(R.string.criterion_is_headphone_connected) {
        booleanCriterion {
            AudioManagerBridge.isHeadphoneConnected
        }
    }

    @AppletOrdinal(0x0014)
    val isDarkMode = invertibleAppletOption(R.string.criterion_is_dark_mode) {
        booleanCriterion {
            val uiMode = ContextBridge.getContext().resources.configuration.uiMode
            uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }

    @AppletOrdinal(0x0015)
    val callState = appletOption(R.string.criterion_call_state) {
        @Suppress("DEPRECATION")
        unaryEqualCriterion {
            ContextBridge.getContext().getSystemService(TelephonyManager::class.java).callState
        }
    }

}