package top.xjunz.tasker.task.applet.action

import top.xjunz.tasker.engine.applet.action.Action
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.ocr.OcrManager
import top.xjunz.tasker.task.ocr.OcrResult

/**
 * OCR 识别动作
 *
 * 执行屏幕 OCR 识别，将识别结果文本输出供下游引用。
 *
 * @param recognizer OCR 识别函数（可注入以便测试）
 */
class OcrRecognizeAction(
    private val recognizer: suspend () -> OcrResult? = { OcrManager.recognizeScreen() }
) : Action() {

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val result = recognizer() ?: return AppletResult.EMPTY_FAILURE
        return AppletResult.succeeded(result.fullText)
    }
}
