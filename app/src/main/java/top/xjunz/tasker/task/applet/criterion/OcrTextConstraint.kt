/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.ocr.OcrManager
import top.xjunz.tasker.task.ocr.OcrResult
import top.xjunz.tasker.task.ocr.OcrTextMatchMode
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

    companion object {
        private const val REGEX_TIMEOUT_MS = 200L

        /**
         * 简单检查正则是否包含嵌套量词模式（ReDoS 高风险）
         * 检测模式如：(a+)+, (a*)+, (a+)*, (.+)+ 等
         */
        internal fun isUnsafeRegex(pattern: String): Boolean {
            val nestedQuantifier = Regex("""\([^)]*[+*]\)[+*?]""")
            return nestedQuantifier.containsMatchIn(pattern)
        }
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val matchText = values[0] as? String ?: return AppletResult.EMPTY_FAILURE

        val result = recognizer() ?: return AppletResult.EMPTY_FAILURE

        val matched = when (matchMode) {
            OcrTextMatchMode.CONTAINS -> result.fullText.contains(matchText)
            OcrTextMatchMode.EXACT -> result.fullText == matchText
            OcrTextMatchMode.REGEX -> {
                try {
                    // ReDoS 防护：拒绝包含嵌套量词的危险模式
                    if (isUnsafeRegex(matchText)) return@apply AppletResult.EMPTY_FAILURE

                    val regex = matchText.toRegex()
                    // 在独立线程执行正则匹配，通过 Future.cancel(true) 实现真正超时
                    val executor = Executors.newSingleThreadExecutor()
                    try {
                        val future = executor.submit(Callable {
                            regex.containsMatchIn(result.fullText)
                        })
                        try {
                            future.get(REGEX_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        } catch (_: TimeoutException) {
                            future.cancel(true)
                            false
                        }
                    } finally {
                        executor.shutdownNow()
                    }
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
