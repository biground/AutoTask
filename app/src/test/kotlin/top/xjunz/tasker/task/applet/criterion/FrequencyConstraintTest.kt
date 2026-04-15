package top.xjunz.tasker.task.applet.criterion

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FrequencyConstraintTest {

    private lateinit var repo: FrequencyCounterRepository
    private lateinit var constraint: FrequencyConstraint

    @Before
    fun setUp() {
        repo = FrequencyCounterRepository.createForTest()
        constraint = FrequencyConstraint { repo }
    }

    @Test
    fun `未超限时返回true`() {
        repo.recordExecution("task1")
        assertTrue(constraint.check("task1", 5, FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `超限时返回false`() {
        repeat(5) { repo.recordExecution("task1") }
        assertFalse(constraint.check("task1", 5, FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `刚好达到上限时返回false`() {
        repeat(3) { repo.recordExecution("task1") }
        assertFalse(constraint.check("task1", 3, FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `无记录时返回true`() {
        assertTrue(constraint.check("task1", 1, FrequencyCounterRepository.WINDOW_DAILY))
    }

    @Test
    fun `不同 windowType 独立判断`() {
        repo.recordExecution("task1")
        assertTrue(constraint.check("task1", 2, FrequencyCounterRepository.WINDOW_HOURLY))
        assertTrue(constraint.check("task1", 2, FrequencyCounterRepository.WINDOW_DAILY))
    }
}
