/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import top.xjunz.tasker.engine.applet.action.ArgumentAction
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.variable.VariableRepository

/**
 * 删除变量的动作 (A-DATA-007)
 *
 * 参数索引：
 * - values[0]: 目标变量名 (String)
 */
class DeleteVariableAction(
    private val repositoryProvider: () -> VariableRepository
) : ArgumentAction() {

    override suspend fun doAction(args: Array<Any?>, runtime: TaskRuntime): AppletResult {
        val name = args[0] as? String ?: return AppletResult.EMPTY_FAILURE
        val deleted = repositoryProvider().deleteVariable(name)
        return if (deleted) AppletResult.EMPTY_SUCCESS else AppletResult.EMPTY_FAILURE
    }
}
