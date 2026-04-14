/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * TTS（文本转语音）桥接器，封装 Android TextToSpeech API。
 */
object TtsBridge {

    @Volatile
    private var isReady = false

    private val tts: TextToSpeech by lazy {
        TextToSpeech(ContextBridge.getContext()) { status ->
            isReady = status == TextToSpeech.SUCCESS
        }.also { engine ->
            if (isReady) {
                engine.language = Locale.getDefault()
            }
        }
    }

    /**
     * 朗读指定文本。
     * @return true 表示成功提交朗读请求，false 表示 TTS 未就绪。
     */
    fun speak(text: String): Boolean {
        // 触发 lazy 初始化
        val engine = tts
        if (!isReady) return false
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
        return result == TextToSpeech.SUCCESS
    }
}
