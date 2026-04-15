package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.mode.ModeHistoryRepository

/**
 * 模式今日激活约束。
 *
 * 检查指定模式今天是否已经被激活过。
 *
 * 参数：
 * - values[0]: modeName (String) — 模式名称
 */
class ModeActivatedTodayConstraint(
    private val modeHistoryProvider: () -> ModeHistoryRepository
) : Applet() {

    /**
     * 检查指定模式今天是否已激活。
     * @return true = 今天激活过，false = 今天未激活
     */
    fun check(modeName: String): Boolean {
        val repo = modeHistoryProvider()
        return repo.wasActivatedToday(modeName)
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val modeName = values[0] as? String ?: return AppletResult.EMPTY_FAILURE

        val matched = check(modeName)

        return if (isInverted) {
            AppletResult.emptyResult(!matched)
        } else {
            AppletResult.emptyResult(matched)
        }
    }
}
