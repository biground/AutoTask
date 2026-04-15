package top.xjunz.tasker.task.runtime

import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.task.XTask
import top.xjunz.tasker.task.applet.criterion.FrequencyCounterRepository

object TaskExecutionRecorder {

    fun recordSuccessfulExecution(
        runtime: TaskRuntime,
        repository: FrequencyCounterRepository = FrequencyCounterRepository.getOrInitialize()
    ) {
        buildTaskKeys(runtime.attachingTask).forEach(repository::recordExecution)
    }

    private fun buildTaskKeys(task: XTask): Set<String> {
        val keys = linkedSetOf(task.checksum.toString())
        val title = task.title
        if (title.isNotBlank()) {
            keys += title
        }
        return keys
    }
}