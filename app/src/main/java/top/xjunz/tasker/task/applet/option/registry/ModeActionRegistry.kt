/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.task.applet.action.SetModeAction
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.value.VariantArgType

/**
 * 模式操作注册表，注册 SetModeAction 动作选项。
 */
class ModeActionRegistry(
    id: Int,
    private val modeRepositoryProvider: () -> ModeRepository
) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val setMode = appletOption(R.string.set_mode) {
        SetModeAction(modeRepositoryProvider)
    }.withValueArgument<String>(R.string.mode_name, VariantArgType.TEXT_MODE_NAME)
        .withValueArgument<String>(R.string.mode_operation_type, VariantArgType.TEXT_MODE_OPERATION)
}
