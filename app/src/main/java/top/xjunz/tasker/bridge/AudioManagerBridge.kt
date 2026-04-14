/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.media.AudioManager

/**
 * AudioManager 的桥接封装。
 */
object AudioManagerBridge {

    private val audioManager: AudioManager by lazy {
        ContextBridge.getContext().getSystemService(AudioManager::class.java)
    }
}
