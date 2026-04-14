package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * BatteryEventDispatcher 单元测试
 *
 * 纯 JVM 环境直接测试 handleBatteryChanged() 逻辑，验证：
 * 1. 电量变化 ≥1% 时分发事件
 * 2. 首次回调只记录电量，不分发事件（sticky broadcast）
 * 3. 电量未变化时不重复分发
 * 4. 通过 lastDispatchedReferent 验证电量百分比正确
 *
 * 注意：纯 JVM 下 android.util.SparseArray 为 stub，
 * 不通过 Event.getExtra() 读取 Referent，改用 dispatcher 的 internal 字段。
 */
class BatteryEventDispatcherTest {

    private lateinit var dispatcher: BatteryEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = BatteryEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `首次回调只记录电量不分发事件`() {
        dispatcher.handleBatteryChanged(level = 50, scale = 100)

        assertTrue("首次回调不应分发事件", receivedEvents.isEmpty())
        assertNull("首次回调不应产生 referent", dispatcher.lastDispatchedReferent)
    }

    @Test
    fun `电量变化时分发 EVENT_ON_BATTERY_LEVEL_CHANGED`() {
        dispatcher.handleBatteryChanged(level = 50, scale = 100)
        dispatcher.handleBatteryChanged(level = 45, scale = 100)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_BATTERY_LEVEL_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `BatteryReferent 携带正确的电量百分比`() {
        dispatcher.handleBatteryChanged(level = 80, scale = 100)
        dispatcher.handleBatteryChanged(level = 75, scale = 100)

        assertEquals(1, receivedEvents.size)
        val referent = dispatcher.lastDispatchedReferent
        assertNotNull("应该产生 BatteryReferent", referent)
        assertEquals(75, referent!!.batteryLevel)
    }

    @Test
    fun `电量未变化时不分发事件`() {
        dispatcher.handleBatteryChanged(level = 50, scale = 100)
        dispatcher.handleBatteryChanged(level = 50, scale = 100)

        assertTrue("电量未变化不应分发事件", receivedEvents.isEmpty())
    }

    @Test
    fun `scale 非 100 时正确计算百分比`() {
        dispatcher.handleBatteryChanged(level = 150, scale = 200)  // 75%
        dispatcher.handleBatteryChanged(level = 120, scale = 200)  // 60%

        assertEquals(1, receivedEvents.size)
        val referent = dispatcher.lastDispatchedReferent
        assertNotNull(referent)
        assertEquals(60, referent!!.batteryLevel)
    }

    @Test
    fun `连续多次变化均正确分发`() {
        dispatcher.handleBatteryChanged(level = 100, scale = 100) // 首次，不分发
        dispatcher.handleBatteryChanged(level = 90, scale = 100)  // 变化，分发
        dispatcher.handleBatteryChanged(level = 80, scale = 100)  // 变化，分发

        assertEquals(2, receivedEvents.size)
        // 最后一次分发的 referent 应为 80%
        val referent = dispatcher.lastDispatchedReferent
        assertNotNull(referent)
        assertEquals(80, referent!!.batteryLevel)
    }

    @Test
    fun `scale 为 0 时不分发事件避免除零`() {
        dispatcher.handleBatteryChanged(level = 50, scale = 0)

        assertTrue("scale=0 不应分发事件", receivedEvents.isEmpty())
        assertNull(dispatcher.lastDispatchedReferent)
    }

    @Test
    fun `计算结果超过 100 时按 100 分发`() {
        dispatcher.handleBatteryChanged(level = 99, scale = 100)
        dispatcher.handleBatteryChanged(level = 120, scale = 100)

        assertEquals(1, receivedEvents.size)
        val referent = dispatcher.lastDispatchedReferent
        assertNotNull(referent)
        assertEquals(100, referent!!.batteryLevel)
    }

    @Test
    fun `计算结果低于 0 时按 0 分发`() {
        dispatcher.handleBatteryChanged(level = 50, scale = 100)
        dispatcher.handleBatteryChanged(level = -10, scale = 100)

        assertEquals(1, receivedEvents.size)
        val referent = dispatcher.lastDispatchedReferent
        assertNotNull(referent)
        assertEquals(0, referent!!.batteryLevel)
    }
}
