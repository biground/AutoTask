/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.bridge.AudioManagerBridge
import top.xjunz.tasker.bridge.NotificationManagerBridge
import top.xjunz.tasker.engine.applet.action.doubleArgsAction
import top.xjunz.tasker.engine.applet.action.simpleSingleNonNullArgAction
import top.xjunz.tasker.ktx.array
import top.xjunz.tasker.ktx.foreColored
import top.xjunz.tasker.ktx.formatSpans
import top.xjunz.tasker.task.applet.anno.AppletOrdinal

/**
 * 声音相关动作的 Registry。
 */
class SoundActionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val volumeChange = appletOption(R.string.set_volume) {
        doubleArgsAction<Int, Int> { streamType, volume, _ ->
            AudioManagerBridge.setStreamVolume(streamType!!, volume!!)
            true
        }
    }.withValueArgument<Int>(R.string.stream_type)
        .withValueArgument<Int>(R.string.volume_level)
        .withSingleValueAppletDescriber<Int> { _, t ->
            val streams = R.array.stream_types.array
            val idx = t ?: 0
            if (idx in streams.indices) {
                R.string.format_set_volume_desc.formatSpans(
                    streams[idx].foreColored()
                )
            } else {
                R.string.set_volume.array.toString()
            }
        }

    @AppletOrdinal(0x0001)
    val silentVibrateMode = appletOption(R.string.set_ringer_mode) {
        simpleSingleNonNullArgAction<Int> {
            AudioManagerBridge.setRingerMode(it)
            true
        }
    }.withValueArgument<Int>(R.string.ringer_mode)
        .withSingleValueAppletDescriber<Int> { applet, t ->
            val modes = R.array.ringer_modes.array
            val idx = t ?: 0
            if (idx in modes.indices) {
                modes[idx].clickToEdit(applet)
            } else {
                null
            }
        }.descAsTitle()

    @AppletOrdinal(0x0002)
    val doNotDisturb = appletOption(R.string.set_dnd) {
        simpleSingleNonNullArgAction<Int> {
            try {
                NotificationManagerBridge.setInterruptionFilter(it)
                true
            } catch (_: SecurityException) {
                false
            }
        }
    }.withValueArgument<Int>(R.string.set_dnd)
        .withSingleValueAppletDescriber<Int> { applet, t ->
            val filters = R.array.dnd_filters.array
            // INTERRUPTION_FILTER 值从 1 开始
            val idx = (t ?: 1) - 1
            if (idx in filters.indices) {
                filters[idx].clickToEdit(applet)
            } else {
                null
            }
        }.descAsTitle()
}
