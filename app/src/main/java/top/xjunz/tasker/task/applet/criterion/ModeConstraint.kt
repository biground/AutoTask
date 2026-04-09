/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 模式约束 (C-DEV-XXX)
 *
 * 检查指定模式是否处于特定状态。
 *
 * 参数索引：
 * - values[0]: 模式名称 (String)
 * - values[1]: 期望状态 (Boolean, true=活跃, false=未激活)
 *
 * 如果模式不存在，视为未激活。
 */
class ModeConstraint(
    private val modeRepositoryProvider: () -> ModeRepository
) : Applet() {

    /**
     * 直接检查模式状态，返回是否匹配。
     * 供单元测试和外部调用使用。
     */
    suspend fun check(modeName: String, expectedActive: Boolean): Boolean {
        val repo = modeRepositoryProvider()
        val mode = repo.getModeByName(modeName)
        val actualActive = mode?.isActive ?: false
        return actualActive == expectedActive
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val modeName = values[0] as? String ?: return AppletResult.EMPTY_FAILURE
        val expectedActive = values[1] as? Boolean ?: return AppletResult.EMPTY_FAILURE

        val matched = check(modeName, expectedActive)

        return if (isInverted) {
            AppletResult.emptyResult(!matched)
        } else {
            AppletResult.emptyResult(matched)
        }
    }
}
