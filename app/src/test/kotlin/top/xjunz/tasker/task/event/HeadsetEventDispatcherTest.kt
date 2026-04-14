package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * HeadsetEventDispatcher 单元测试
 *
 * 由于纯 JVM 环境无法真实注册 BroadcastReceiver，
 * 这里直接测试提取出来的 handleHeadsetState() 逻辑，验证事件分发正确性。
 */
class HeadsetEventDispatcherTest {

    private lateinit var dispatcher: HeadsetEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = HeadsetEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `耳机插入时分发 EVENT_ON_HEADSET_PLUGGED`() {
        dispatcher.handleHeadsetState(1)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_HEADSET_PLUGGED, receivedEvents[0].type)
    }

    @Test
    fun `耳机拔出时分发 EVENT_ON_HEADSET_UNPLUGGED`() {
        dispatcher.handleHeadsetState(0)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_HEADSET_UNPLUGGED, receivedEvents[0].type)
    }

    @Test
    fun `未知状态不分发事件`() {
        dispatcher.handleHeadsetState(-1)

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `无效状态值不分发事件`() {
        dispatcher.handleHeadsetState(2)

        assertTrue(receivedEvents.isEmpty())
    }
}
