package top.xjunz.tasker.task.applet.criterion

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FrequencyCounterRepositoryTest {

    private lateinit var repo: FrequencyCounterRepository
    private var mockTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        mockTime = System.currentTimeMillis()
        repo = FrequencyCounterRepository.createForTest { mockTime }
    }

    @Test
    fun `记录后计数为1`() {
        repo.recordExecution("task1")
        assertEquals(1, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `多次记录累加`() {
        repo.recordExecution("task1")
        repo.recordExecution("task1")
        repo.recordExecution("task1")
        assertEquals(3, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `不同 taskKey 互不影响`() {
        repo.recordExecution("task1")
        repo.recordExecution("task2")
        assertEquals(1, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_DAILY))
        assertEquals(1, repo.getExecutionCount("task2", FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `未记录的 taskKey 返回0`() {
        assertEquals(0, repo.getExecutionCount("nonexistent", FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `DAILY 窗口过滤昨天的记录`() {
        // 设置为昨天 23:00
        val yesterday = LocalDate.now().minusDays(1).atTime(23, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        mockTime = yesterday
        repo.recordExecution("task1")

        // 切换到今天
        mockTime = System.currentTimeMillis()
        assertEquals(0, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `HOURLY 窗口过滤上一小时的记录`() {
        // 设置为 2 小时前
        mockTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000
        repo.recordExecution("task1")

        // 切换到当前
        mockTime = System.currentTimeMillis()
        assertEquals(0, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_HOURLY))
    }

    @Test
    fun `cleanup 移除超过7天的记录`() {
        // 记录 8 天前的执行
        mockTime = System.currentTimeMillis() - 8L * 24 * 60 * 60 * 1000
        repo.recordExecution("task1")

        // 切换到当前
        mockTime = System.currentTimeMillis()
        repo.cleanup("task1")

        // 应已被清理
        assertEquals(0, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_WEEKLY))
    }

    @Test
    fun `cleanup 保留7天内的记录`() {
        // 记录 5 天前的执行
        mockTime = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000
        repo.recordExecution("task1")

        // 切换到当前
        mockTime = System.currentTimeMillis()
        repo.cleanup("task1")

        // 应保留
        assertEquals(1, repo.getExecutionCount("task1", FrequencyCounterRepository.WINDOW_WEEKLY))
    }
}
