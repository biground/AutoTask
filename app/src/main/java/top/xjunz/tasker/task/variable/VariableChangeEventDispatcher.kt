/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.variable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.engine.variable.VariableChangeEvent
import top.xjunz.tasker.engine.variable.VariableRepository

/**
 * 变量变化事件分发器，监听 [VariableRepository] 的变化事件并转换为引擎 [Event] 进行分发。
 *
 * @author AutoPilot 2026/04/09
 */
class VariableChangeEventDispatcher(
    private val repository: VariableRepository,
    private val scope: CoroutineScope
) : EventDispatcher() {

    companion object {
        /** Extra key：变量名 */
        const val EXTRA_VARIABLE_NAME = 0
        /** Extra key：变量旧值 */
        const val EXTRA_VARIABLE_OLD_VALUE = 1
        /** Extra key：变量新值 */
        const val EXTRA_VARIABLE_NEW_VALUE = 2
    }

    private var collectJob: Job? = null

    override fun onRegistered() {
        collectJob = repository.observeChanges()
            .onEach { changeEvent ->
                when (changeEvent) {
                    is VariableChangeEvent.Set -> {
                        val event = Event.obtain(Event.EVENT_ON_VARIABLE_CHANGED).apply {
                            putExtra(EXTRA_VARIABLE_NAME, changeEvent.variable.name)
                            putExtra(EXTRA_VARIABLE_NEW_VALUE, changeEvent.variable.value ?: "")
                            putExtra(EXTRA_VARIABLE_OLD_VALUE, changeEvent.oldValue ?: "")
                        }
                        dispatchEvents(event)
                    }
                    is VariableChangeEvent.Deleted -> {
                        val event = Event.obtain(Event.EVENT_ON_VARIABLE_CHANGED).apply {
                            putExtra(EXTRA_VARIABLE_NAME, changeEvent.name)
                            putExtra(EXTRA_VARIABLE_NEW_VALUE, "")
                            putExtra(EXTRA_VARIABLE_OLD_VALUE, "")
                        }
                        dispatchEvents(event)
                    }
                }
            }
            .launchIn(scope)
    }

    override fun destroy() {
        collectJob?.cancel()
        collectJob = null
    }
}
