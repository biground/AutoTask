package top.xjunz.tasker.task.applet.criterion

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.task.mode.ModeHistoryRepository
import java.time.LocalDate
import java.time.ZoneId

class ModeActivatedTodayConstraintTest {

    private lateinit var historyRepo: ModeHistoryRepository
    private lateinit var constraint: ModeActivatedTodayConstraint

    @Before
    fun setUp() {
        historyRepo = ModeHistoryRepository.createForTest()
        constraint = ModeActivatedTodayConstraint { historyRepo }
    }

    @Test
    fun `今天激活过返回true`() {
        historyRepo.recordActivation("工作模式")
        assertTrue(constraint.check("工作模式"))
    }

    @Test
    fun `未激活过返回false`() {
        assertFalse(constraint.check("工作模式"))
    }

    @Test
    fun `昨天激活过今天返回false`() {
        val yesterday = LocalDate.now().minusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .plusSeconds(1).toInstant().toEpochMilli()
        historyRepo.recordActivation("工作模式", yesterday)
        assertFalse(constraint.check("工作模式"))
    }

    @Test
    fun `不同模式互不影响`() {
        historyRepo.recordActivation("工作模式")
        assertTrue(constraint.check("工作模式"))
        assertFalse(constraint.check("居家模式"))
    }
}
