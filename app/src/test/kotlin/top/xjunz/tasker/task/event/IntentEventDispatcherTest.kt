package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.task.applet.flow.ref.IntentReferent

/**
 * IntentEventDispatcher 单元测试
 *
 * 纯 JVM 环境，直接测试 handleIntentReceived() 逻辑。
 * registerAction/unregisterAction/destroy 涉及 Context.registerReceiver，
 * 在纯 JVM 中无法测试注册/注销流程，仅测试输入校验和事件分发。
 */
class IntentEventDispatcherTest {

    private lateinit var dispatcher: IntentEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = IntentEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `收到 Intent 时分发 EVENT_ON_INTENT_RECEIVED`() {
        dispatcher.handleIntentReceived("com.example.ACTION_TEST", "content://test/1")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_INTENT_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `空 action 和空 dataUri 也能正常分发`() {
        dispatcher.handleIntentReceived("", "")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_INTENT_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `registerAction 空白字符串被拒绝`() {
        dispatcher.registerAction("")
        dispatcher.registerAction("   ")

        assertTrue(dispatcher.receivers.isEmpty())
    }

    @Test
    fun `unregisterAction 未注册 action 不报错`() {
        // 不应抛出异常
        dispatcher.unregisterAction("com.example.NOT_EXIST")
    }

    @Test
    fun `多个事件连续分发`() {
        dispatcher.handleIntentReceived("action1", "uri1")
        dispatcher.handleIntentReceived("action2", "uri2")
        dispatcher.handleIntentReceived("action3", "")

        assertEquals(3, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_INTENT_RECEIVED })
    }

    @Test
    fun `不同 action 和 dataUri 的事件正确分发`() {
        dispatcher.handleIntentReceived("com.example.A", "content://a")
        dispatcher.handleIntentReceived("com.example.B", "https://b.com")

        assertEquals(2, receivedEvents.size)
        assertEquals(Event.EVENT_ON_INTENT_RECEIVED, receivedEvents[0].type)
        assertEquals(Event.EVENT_ON_INTENT_RECEIVED, receivedEvents[1].type)
    }

    @Test
    fun `onRegistered 空实现不抛异常`() {
        // IntentEventDispatcher 的 onRegistered 为空实现
        dispatcher.onRegistered()
        // 不应抛出异常
    }

    @Test
    fun `IntentReferent 字段访问正确`() {
        val referent = IntentReferent("com.example.ACTION", "content://test")

        assertEquals("com.example.ACTION", referent.action)
        assertEquals("content://test", referent.dataUri)
    }

    @Test
    fun `IntentReferent data class 相等性`() {
        val referent1 = IntentReferent("action", "uri")
        val referent2 = IntentReferent("action", "uri")

        assertEquals(referent1, referent2)
        assertEquals(referent1.hashCode(), referent2.hashCode())
    }
}
