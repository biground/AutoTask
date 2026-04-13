/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.OcrTextConstraint
import top.xjunz.tasker.task.ocr.OcrTextMatchMode

/**
 * 屏幕 OCR 约束注册表
 *
 * 注册三种 OCR 文字匹配模式：包含、精确、正则
 */
class ScreenOcrCriterionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val ocrTextContains = invertibleAppletOption(R.string.ocr_screen_contains_text) {
        OcrTextConstraint(OcrTextMatchMode.CONTAINS)
    }.withValueArgument<String>(R.string.ocr_match_text)

    @AppletOrdinal(0x0001)
    val ocrTextEquals = invertibleAppletOption(R.string.ocr_screen_text_equals) {
        OcrTextConstraint(OcrTextMatchMode.EXACT)
    }.withValueArgument<String>(R.string.ocr_match_text)

    @AppletOrdinal(0x0002)
    val ocrTextMatches = invertibleAppletOption(R.string.ocr_screen_text_matches_regex) {
        OcrTextConstraint(OcrTextMatchMode.REGEX)
    }.withValueArgument<String>(R.string.ocr_regex_pattern)
}
