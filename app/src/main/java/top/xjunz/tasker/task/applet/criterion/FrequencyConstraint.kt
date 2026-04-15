package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 频率限制约束。
 *
 * 检查指定任务在给定时间窗口内的执行次数是否未超出上限。
 *
 * 参数：
 * - values[0]: taskKey (String) — 任务标识
 * - values[1]: maxCount (Int) — 最大执行次数
 * - values[2]: windowType (Int) — 时间窗口类型 (HOURLY=0, DAILY=1, WEEKLY=2)
 */
class FrequencyConstraint(
    private val repositoryProvider: () -> FrequencyCounterRepository
) : Applet() {

    /**
     * 检查是否满足频率限制。
     * @return true = 未超限（可继续执行），false = 已超限
     */
    fun check(taskKey: String, maxCount: Int, windowType: Int): Boolean {
        val repo = repositoryProvider()
        val count = repo.getExecutionCount(taskKey, windowType)
        return count < maxCount
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val taskKey = values[0] as? String ?: return AppletResult.EMPTY_FAILURE
        val maxCount = values[1] as? Int ?: return AppletResult.EMPTY_FAILURE
        val windowType = values[2] as? Int ?: return AppletResult.EMPTY_FAILURE

        val matched = check(taskKey, maxCount, windowType)

        return if (isInverted) {
            AppletResult.emptyResult(!matched)
        } else {
            AppletResult.emptyResult(matched)
        }
    }
}