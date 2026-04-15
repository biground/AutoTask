package top.xjunz.tasker.task.runtime

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import top.xjunz.tasker.engine.runtime.TaskRuntime.Companion.obtainTaskRuntime
import top.xjunz.tasker.engine.task.XTask
import top.xjunz.tasker.task.applet.criterion.FrequencyCounterRepository

class TaskExecutionRecorderTest {

    @Test
    fun `成功执行会按校验和和标题记录频次`() = runTest {
        val repository = FrequencyCounterRepository.createForTest()
        val task = XTask().apply {
            metadata = XTask.Metadata(
                title = "晨间任务",
                checksum = 42L
            )
        }
        val runtime = backgroundScope.obtainTaskRuntime(task)

        try {
            TaskExecutionRecorder.recordSuccessfulExecution(runtime, repository)

            assertEquals(
                1,
                repository.getExecutionCount("42", FrequencyCounterRepository.WINDOW_DAILY)
            )
            assertEquals(
                1,
                repository.getExecutionCount("晨间任务", FrequencyCounterRepository.WINDOW_DAILY)
            )
        } finally {
            runtime.recycle()
        }
    }
}