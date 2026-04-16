/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.media.AudioManager
import android.view.KeyEvent

/**
 * AudioManager 的桥接封装。
 */
object AudioManagerBridge {

    private val audioManager: AudioManager by lazy {
        ContextBridge.getContext().getSystemService(AudioManager::class.java)
    }

    @Suppress("DEPRECATION")
    val isHeadphoneConnected: Boolean
        get() = audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn

    fun setStreamVolume(streamType: Int, volume: Int) {
        // 安全: 校验 streamType 有效范围
        val validStreams = intArrayOf(
            AudioManager.STREAM_VOICE_CALL, AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING, AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM, AudioManager.STREAM_NOTIFICATION
        )
        if (streamType !in validStreams) return
        val maxVol = audioManager.getStreamMaxVolume(streamType)
        audioManager.setStreamVolume(streamType, volume.coerceIn(0, maxVol), 0)
    }

    fun setRingerMode(mode: Int) {
        audioManager.ringerMode = mode
    }

    fun dispatchMediaKeyEvent(keyCode: Int) {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    /**
     * 将闹钟以外的所有音频流静音。
     * 适用于"静音除闹钟外的所有声音"场景。
     */
    fun silenceNonAlarmStreams() {
        val streams = intArrayOf(
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_NOTIFICATION
        )
        for (stream in streams) {
            audioManager.setStreamVolume(stream, 0, 0)
        }
    }
}
