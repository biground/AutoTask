/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.ModeActivatedTodayConstraint
import top.xjunz.tasker.task.applet.criterion.ModeConstraint
import top.xjunz.tasker.task.mode.ModeHistoryRepository

/**
 * 模式条件注册表，注册 ModeConstraint 约束选项。
 */
class ModeCriterionRegistry(
    id: Int,
    private val modeRepositoryProvider: () -> ModeRepository
) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val modeConstraint = invertibleAppletOption(R.string.check_mode_status) {
        ModeConstraint(modeRepositoryProvider)
    }.withValueArgument<String>(R.string.mode_name)
        .withValueArgument<Boolean>(R.string.expected_mode_active)

    @AppletOrdinal(0x0001)
    val modeActivatedToday = invertibleAppletOption(R.string.mode_activated_today) {
        ModeActivatedTodayConstraint { ModeHistoryRepository.getOrInitialize() }
    }.withValueArgument<String>(R.string.mode_name)
}
