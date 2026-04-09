/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.task.applet.action.DeleteVariableAction
import top.xjunz.tasker.task.applet.action.SetVariableAction
import top.xjunz.tasker.task.applet.anno.AppletOrdinal

/**
 * 变量操作注册表，注册 SetVariable 和 DeleteVariable 动作选项。
 */
class VariableActionRegistry(
    id: Int,
    private val repositoryProvider: () -> VariableRepository
) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val setVariable = appletOption(R.string.set_variable) {
        SetVariableAction(repositoryProvider)
    }.withValueArgument<String>(R.string.variable_name)
        .withValueArgument<Int>(R.string.variable_type)
        .withValueArgument<String>(R.string.variable_value)

    @AppletOrdinal(0x0001)
    val deleteVariable = appletOption(R.string.delete_variable) {
        DeleteVariableAction(repositoryProvider)
    }.withValueArgument<String>(R.string.variable_name)
}
