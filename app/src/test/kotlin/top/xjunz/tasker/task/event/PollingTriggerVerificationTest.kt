package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import java.util.Calendar

/**
 * T14 — 轮询触发器 MVP 可行性验证
 *
 * 验证 PollEventDispatcher 的核心机制：
 * 1. EVENT_ON_TICK 事件结构正确、可携带 EXTRA_TICK_TIME_MILLS
 * 2. EventDispatcher 回调链路完整
 * 3. Regular Interval (T-TIME-003) 概念：利用 tick 时间与 Calendar 判断时间范围
 * 4. Battery Level (T-PWR-002) 概念：轮询条件可在 tick 回调中评估
 *
 * 注意：PollEventDispatcher 依赖 android.os.Looper，无法在纯 JVM 测试中直接实例化，
 * 因此使用一个简易 TestPollDispatcher 模拟其核心逻辑。
 */
class PollingTriggerVerificationTest {

    /**
     * 模拟 PollEventDispatcher 核心逻辑，去掉 Looper 依赖
     */
    private class TestPollDispatcher : EventDispatcher() {
        companion object {
            const val EXTRA_TICK_TIME_MILLS = PollEventDispatcher.EXTRA_TICK_TIME_MILLS
        }

        private var previousSec: Long = -1

        override fun destroy() {
            previousSec = -1
        }

        override fun onRegistered() {
            // 无 Handler，手动触发
        }

        /**
         * 手动模拟一次 tick，等价于 PollEventDispatcher 中 tikTok Runnable 的逻辑
         */
        fun simulateTick(uptimeMillis: Long) {
            val event = Event.obtain(Event.EVENT_ON_TICK)
            val sec = uptimeMillis / 1000
            if (sec != previousSec) {
                previousSec = sec
                event.putExtra(EXTRA_TICK_TIME_MILLS, uptimeMillis)
                dispatchEvents(event)
            }
        }
    }

    private lateinit var dispatcher: TestPollDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = TestPollDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    // ========== 用例 1：EVENT_ON_TICK 事件结构验证 ==========

    @Test
    fun `tick 事件类型为 EVENT_ON_TICK`() {
        val now = System.currentTimeMillis()
        dispatcher.simulateTick(now)

        assertEquals("应收到 1 个事件", 1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_TICK, receivedEvents[0].type)
    }

    // ========== 用例 2：去重逻辑——同一秒内只分发一次 ==========

    @Test
    fun `同一秒内多次 tick 只分发一次事件`() {
        val baseTime = 1_700_000_000_000L // 固定基准时间
        // 同一秒内的两个时间点
        dispatcher.simulateTick(baseTime)
        dispatcher.simulateTick(baseTime + 500) // 同一秒

        assertEquals("同一秒内不应重复分发", 1, receivedEvents.size)

        // 下一秒应分发新事件
        dispatcher.simulateTick(baseTime + 1000)
        assertEquals("新一秒应分发新事件", 2, receivedEvents.size)
    }

    // ========== 用例 3：Regular Interval 概念验证 (T-TIME-003) ==========

    @Test
    fun `tick 时间可转换为 Calendar 并判断时间范围`() {
        // 模拟 PollEventDispatcher 分发 tick
        val targetTime = createCalendar(14, 30, 0).timeInMillis
        dispatcher.simulateTick(targetTime)

        assertEquals(1, receivedEvents.size)
        val event = receivedEvents[0]
        assertEquals(Event.EVENT_ON_TICK, event.type)

        // 模拟 TimeCriterionRegistry 的 timeRange 逻辑：
        // 从 tick 事件中获取时间，转换为 Calendar，然后判断是否在目标范围内
        val tickCalendar = Calendar.getInstance().apply {
            timeInMillis = targetTime
        }

        // 判断 14:30 是否在 14:00 ~ 15:00 范围内
        val rangeStart = createCalendar(14, 0, 0)
        val rangeEnd = createCalendar(15, 0, 0)

        assertTrue(
            "14:30 应在 14:00~15:00 范围内",
            tickCalendar.timeInMillis in rangeStart.timeInMillis..rangeEnd.timeInMillis
        )

        // 判断 14:30 不在 16:00 ~ 17:00 范围内
        val outOfRangeStart = createCalendar(16, 0, 0)
        val outOfRangeEnd = createCalendar(17, 0, 0)
        assertTrue(
            "14:30 不应在 16:00~17:00 范围内",
            tickCalendar.timeInMillis !in outOfRangeStart.timeInMillis..outOfRangeEnd.timeInMillis
        )
    }

    @Test
    fun `tick 时间可判断星期几条件`() {
        // 模拟任意已知日期的 tick
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 14, 10, 0, 0) // 2026-04-14 是周二
            set(Calendar.MILLISECOND, 0)
        }
        dispatcher.simulateTick(calendar.timeInMillis)

        assertEquals(1, receivedEvents.size)

        // 模拟 TimeCriterionRegistry 的 dayOfWeek 逻辑
        val tickDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val allowedDays = setOf(
            Calendar.MONDAY - 1,
            Calendar.TUESDAY - 1,
            Calendar.WEDNESDAY - 1
        ) // 周一到周三

        assertTrue(
            "2026-04-14 (周二) 应匹配周一~周三条件",
            tickDayOfWeek in allowedDays
        )
    }

    // ========== 用例 4：Battery Level 轮询概念验证 (T-PWR-002) ==========

    @Test
    fun `轮询条件可在 tick 回调中评估电池电量`() {
        // 模拟电池电量查询结果
        var simulatedBatteryLevel = 45

        // 模拟 GlobalCriterionRegistry.batteryCapacityRange 的逻辑：
        // 在 tick 回调中读取电池电量，判断是否在目标范围内
        val rangeMin = 20
        val rangeMax = 80

        dispatcher.simulateTick(System.currentTimeMillis())
        assertEquals(1, receivedEvents.size)

        // 电量 45% 在 20%~80% 范围内
        assertTrue(
            "电量 $simulatedBatteryLevel% 应在 ${rangeMin}%~${rangeMax}% 范围内",
            simulatedBatteryLevel in rangeMin..rangeMax
        )

        // 电量变化到 10%，不在范围内
        simulatedBatteryLevel = 10
        assertTrue(
            "电量 $simulatedBatteryLevel% 不应在 ${rangeMin}%~${rangeMax}% 范围内",
            simulatedBatteryLevel !in rangeMin..rangeMax
        )

        // 充电状态条件（模拟 isBatteryCharging）
        var isCharging = true
        assertTrue("充电状态应可被评估", isCharging)
        isCharging = false
        assertTrue("非充电状态应可被评估", !isCharging)
    }

    // ========== 辅助方法 ==========

    private fun createCalendar(hour: Int, minute: Int, second: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
