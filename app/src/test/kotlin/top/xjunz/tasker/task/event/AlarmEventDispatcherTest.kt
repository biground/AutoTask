package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import java.util.Calendar

/**
 * AlarmEventDispatcher 单元测试
 *
 * 由于纯 JVM 环境无法使用 AlarmManager / PendingIntent，
 * 这里测试提取出来的 handleAlarmFired() 和 computeNextTriggerTime() 逻辑。
 */
class AlarmEventDispatcherTest {

    private lateinit var dispatcher: AlarmEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = AlarmEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    // ─── handleAlarmFired ───────────────────────────────────────

    @Test
    fun `闹钟触发时分发 EVENT_ON_ALARM_FIRED`() {
        val triggerTime = 1_700_000_000_000L
        dispatcher.handleAlarmFired(triggerTime)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_ALARM_FIRED, receivedEvents[0].type)
    }

    @Test
    fun `连续触发多次闹钟均正确分发`() {
        dispatcher.handleAlarmFired(1_000L)
        dispatcher.handleAlarmFired(2_000L)

        assertEquals(2, receivedEvents.size)
        assertEquals(Event.EVENT_ON_ALARM_FIRED, receivedEvents[0].type)
        assertEquals(Event.EVENT_ON_ALARM_FIRED, receivedEvents[1].type)
    }

    // ─── computeNextTriggerTime: Daily ──────────────────────────

    @Test
    fun `Daily 规则返回次日同一时刻`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Daily, lastTrigger
        )

        val expected = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 15, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(expected, nextTrigger)
    }

    // ─── computeNextTriggerTime: Weekly ─────────────────────────

    @Test
    fun `Weekly 规则跳到下一个匹配的星期几`() {
        // 2026-04-14 是周二 (Calendar.TUESDAY=3)
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 9, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每周五 (Calendar.FRIDAY=6) 触发
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Weekly(setOf(Calendar.FRIDAY)), lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(Calendar.FRIDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(17, result.get(Calendar.DAY_OF_MONTH)) // 2026-04-17 周五
    }

    @Test
    fun `Weekly 规则多个星期几时选最近的`() {
        // 2026-04-14 周二
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每周一 (2) 和周四 (5)
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Weekly(setOf(Calendar.MONDAY, Calendar.THURSDAY)),
            lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        // 周二之后最近的匹配日是周四 (4月16日)
        assertEquals(Calendar.THURSDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `Weekly 规则跨周时正确计算`() {
        // 2026-04-18 周六
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 18, 7, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每周一 (2)
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Weekly(setOf(Calendar.MONDAY)), lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(Calendar.MONDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(20, result.get(Calendar.DAY_OF_MONTH)) // 2026-04-20 周一
    }

    // ─── computeNextTriggerTime: Monthly ────────────────────────

    @Test
    fun `Monthly 规则跳到下一个匹配的日期`() {
        // 2026-04-14
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每月 20 日
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Monthly(setOf(20)), lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(20, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, result.get(Calendar.MONTH))
    }

    @Test
    fun `Monthly 规则跨月时正确计算`() {
        // 2026-04-25
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 25, 6, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每月 5 日
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Monthly(setOf(5)), lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(5, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MAY, result.get(Calendar.MONTH)) // 跨到 5 月
    }

    @Test
    fun `Monthly 规则多个日期时选最近的`() {
        // 2026-04-14
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastTrigger = cal.timeInMillis

        // 每月 1 日和 18 日
        val nextTrigger = dispatcher.computeNextTriggerTime(
            AlarmEventDispatcher.RepeatRule.Monthly(setOf(1, 18)), lastTrigger
        )

        val result = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(18, result.get(Calendar.DAY_OF_MONTH)) // 14 日之后最近的是 18 日
    }

    // ─── 空集防护 ─────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `Weekly 空 daysOfWeek 应抛出 IllegalArgumentException`() {
        AlarmEventDispatcher.RepeatRule.Weekly(emptySet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Monthly 空 daysOfMonth 应抛出 IllegalArgumentException`() {
        AlarmEventDispatcher.RepeatRule.Monthly(emptySet())
    }
}
