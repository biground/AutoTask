package top.xjunz.tasker.task.mode

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ModeHistoryRepositoryTest {

    private val zoneId = ZoneId.systemDefault()
    private lateinit var repo: ModeHistoryRepository

    @Before
    fun setUp() {
        repo = ModeHistoryRepository.createForTest()
    }

    @Test
    fun `记录后今天查询返回true`() {
        repo.recordActivation("工作模式")
        assertTrue(repo.wasActivatedToday("工作模式"))
    }

    @Test
    fun `未记录的模式返回false`() {
        assertFalse(repo.wasActivatedToday("不存在的模式"))
    }

    @Test
    fun `昨天的记录今天返回false`() {
        val yesterday = LocalDate.now().minusDays(1)
            .atStartOfDay(zoneId).plusSeconds(1).toInstant().toEpochMilli()
        repo.recordActivation("工作模式", yesterday)
        assertFalse(repo.wasActivatedToday("工作模式"))
    }

    @Test
    fun `getLastActivationTime 返回正确时间戳`() {
        val ts = 1700000000000L
        repo.recordActivation("工作模式", ts)
        assertEquals(ts, repo.getLastActivationTime("工作模式"))
    }

    @Test
    fun `getLastActivationTime 未记录返回null`() {
        assertNull(repo.getLastActivationTime("不存在"))
    }

    @Test
    fun `多次激活覆盖旧时间戳`() {
        repo.recordActivation("工作模式", 1000L)
        repo.recordActivation("工作模式", 2000L)
        assertEquals(2000L, repo.getLastActivationTime("工作模式"))
    }

    @Test
    fun `不同模式互不影响`() {
        repo.recordActivation("工作模式")
        assertFalse(repo.wasActivatedToday("居家模式"))
        assertTrue(repo.wasActivatedToday("工作模式"))
    }

    @Test
    fun `getOrInitialize 提供可复用单例`() {
        val modeName = "singleton-${System.nanoTime()}"

        val singleton = ModeHistoryRepository.getOrInitialize()
        singleton.recordActivation(modeName)

        assertTrue(singleton === ModeHistoryRepository.getOrInitialize())
        assertTrue(ModeHistoryRepository.getOrInitialize().wasActivatedToday(modeName))
    }
}
