/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.mode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import top.xjunz.tasker.engine.mode.ModeChangeEvent
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 模式变化事件分发器，监听 [ModeRepository] 的变化事件并转换为引擎 [Event] 进行分发。
 * 仅关注 Activated 和 Deactivated 事件（Created/Deleted 不触发）。
 *
 * @author AutoPilot 2026/04/09
 */
class ModeChangeEventDispatcher(
    private val repository: ModeRepository,
    private val scope: CoroutineScope
) : EventDispatcher() {

    companion object {
        /** Extra key：模式名称 */
        const val EXTRA_MODE_NAME = 0
        /** Extra key：变化类型（"activated" / "deactivated"） */
        const val EXTRA_CHANGE_TYPE = 1
        /** Extra key：前一个模式名称（仅 Activated 时有值，否则空串） */
        const val EXTRA_PREVIOUS_MODE_NAME = 2
    }

    private var collectJob: Job? = null

    override fun onRegistered() {
        collectJob = repository.observeChanges()
            .onEach { changeEvent ->
                when (changeEvent) {
                    is ModeChangeEvent.Activated -> {
                        val event = Event.obtain(Event.EVENT_ON_MODE_CHANGED).apply {
                            putExtra(EXTRA_MODE_NAME, changeEvent.mode.name)
                            putExtra(EXTRA_CHANGE_TYPE, "activated")
                            putExtra(EXTRA_PREVIOUS_MODE_NAME, changeEvent.previousMode?.name ?: "")
                        }
                        dispatchEvents(event)
                    }
                    is ModeChangeEvent.Deactivated -> {
                        val event = Event.obtain(Event.EVENT_ON_MODE_CHANGED).apply {
                            putExtra(EXTRA_MODE_NAME, changeEvent.mode.name)
                            putExtra(EXTRA_CHANGE_TYPE, "deactivated")
                            putExtra(EXTRA_PREVIOUS_MODE_NAME, "")
                        }
                        dispatchEvents(event)
                    }
                    // Created / Deleted 不触发事件
                    else -> { /* no-op */ }
                }
            }
            .launchIn(scope)
    }

    override fun destroy() {
        collectJob?.cancel()
        collectJob = null
    }
}
