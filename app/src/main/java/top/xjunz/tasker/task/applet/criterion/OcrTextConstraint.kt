/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import kotlinx.coroutines.withTimeoutOrNull
import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.ocr.OcrManager
import top.xjunz.tasker.task.ocr.OcrResult
import top.xjunz.tasker.task.ocr.OcrTextMatchMode

/**
 * OCR 文字约束 (Criterion)
 *
 * 判断屏幕上识别到的文字是否匹配指定条件。
 * 支持三种匹配模式：包含(CONTAINS)、精确(EXACT)、正则(REGEX)。
 *
 * 参数索引：
 * - values[0]: 匹配文本/正则（String）
 *
 * @param matchMode 文字匹配模式
 * @param recognizer OCR 识别函数，默认调用 OcrManager.recognizeScreen()，测试时可注入 lambda
 */
class OcrTextConstraint(
    private val matchMode: OcrTextMatchMode,
    private val recognizer: suspend () -> OcrResult? = { OcrManager.recognizeScreen() }
) : Applet() {

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val matchText = values[0] as? String ?: return AppletResult.EMPTY_FAILURE

        val result = recognizer() ?: return AppletResult.EMPTY_FAILURE

        val matched = when (matchMode) {
            OcrTextMatchMode.CONTAINS -> result.fullText.contains(matchText)
            OcrTextMatchMode.EXACT -> result.fullText == matchText
            OcrTextMatchMode.REGEX -> {
                try {
                    // ReDoS 防护：200ms 超时保护
                    withTimeoutOrNull(200) {
                        matchText.toRegex().containsMatchIn(result.fullText)
                    } ?: false
                } catch (_: Exception) {
                    // 非法正则表达式安全返回 false
                    false
                }
            }
        }

        return if (isInverted) AppletResult.emptyResult(!matched)
        else AppletResult.emptyResult(matched)
    }
}
