/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import top.xjunz.tasker.engine.applet.action.ArgumentAction
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 模式切换动作 (A-MODE-001)
 *
 * 参数索引：
 * - args[0]: 目标模式名称 (String)
 * - args[1]: 操作类型 (String) — "activate" | "deactivate" | "toggle"
 *
 * 行为：
 * - activate: 激活指定模式（ModeRepository 自动停用其他互斥模式）
 * - deactivate: 停用指定模式（仅当该模式当前处于激活状态时生效）
 * - toggle: 切换模式状态（激活↔停用）
 */
class SetModeAction(
    private val modeRepositoryProvider: () -> ModeRepository
) : ArgumentAction() {

    companion object {
        const val ACTION_ACTIVATE = "activate"
        const val ACTION_DEACTIVATE = "deactivate"
        const val ACTION_TOGGLE = "toggle"

        private val VALID_ACTIONS = setOf(ACTION_ACTIVATE, ACTION_DEACTIVATE, ACTION_TOGGLE)
    }

    override suspend fun doAction(args: Array<Any?>, runtime: TaskRuntime): AppletResult {
        val modeName = args[0] as? String
        if (modeName.isNullOrBlank()) return AppletResult.EMPTY_FAILURE

        val actionType = args[1] as? String
        if (actionType !in VALID_ACTIONS) return AppletResult.EMPTY_FAILURE

        val repo = modeRepositoryProvider()
        val mode = repo.getModeByName(modeName) ?: return AppletResult.EMPTY_FAILURE

        when (actionType) {
            ACTION_ACTIVATE -> {
                repo.setActiveMode(mode.id)
            }
            ACTION_DEACTIVATE -> {
                if (mode.isActive) {
                    repo.deactivateAll()
                }
            }
            ACTION_TOGGLE -> {
                if (mode.isActive) {
                    repo.deactivateAll()
                } else {
                    repo.setActiveMode(mode.id)
                }
            }
        }
        return AppletResult.EMPTY_SUCCESS
    }
}
