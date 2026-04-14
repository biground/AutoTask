package top.xjunz.tasker.task.event

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * PowerEventDispatcher 单元测试
 *
 * 由于纯 JVM 环境无法真实注册 BroadcastReceiver，
 * 这里直接测试提取出来的 handleAction() 逻辑，验证事件分发正确性。
 */
class PowerEventDispatcherTest {

    private lateinit var dispatcher: PowerEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = PowerEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `收到 POWER_CONNECTED 广播时分发 EVENT_ON_POWER_CONNECTED`() {
        dispatcher.handleAction(Intent.ACTION_POWER_CONNECTED)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_POWER_CONNECTED, receivedEvents[0].type)
    }

    @Test
    fun `收到 POWER_DISCONNECTED 广播时分发 EVENT_ON_POWER_DISCONNECTED`() {
        dispatcher.handleAction(Intent.ACTION_POWER_DISCONNECTED)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_POWER_DISCONNECTED, receivedEvents[0].type)
    }

    @Test
    fun `未知 action 不分发事件`() {
        dispatcher.handleAction("com.example.UNKNOWN")

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `null action 不分发事件`() {
        dispatcher.handleAction(null)

        assertTrue(receivedEvents.isEmpty())
    }
}
