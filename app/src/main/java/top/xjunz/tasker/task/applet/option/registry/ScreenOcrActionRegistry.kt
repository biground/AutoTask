/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.task.applet.action.OcrRecognizeAction
import top.xjunz.tasker.task.applet.anno.AppletOrdinal

/**
 * 屏幕 OCR 动作注册表
 *
 * 注册 OCR 屏幕识别动作
 */
class ScreenOcrActionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val recognizeScreenText = appletOption(R.string.ocr_recognize_screen_text) {
        OcrRecognizeAction()
    }.withResult<String>(R.string.ocr_recognized_text)
}
