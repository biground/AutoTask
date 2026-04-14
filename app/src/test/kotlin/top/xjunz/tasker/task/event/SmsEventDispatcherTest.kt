package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.task.applet.flow.ref.SmsReferent

/**
 * SmsEventDispatcher 单元测试
 *
 * 纯 JVM 环境，直接测试 handleSmsReceived() 逻辑。
 */
class SmsEventDispatcherTest {

    private lateinit var dispatcher: SmsEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = SmsEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `收到短信时分发 EVENT_ON_SMS_RECEIVED`() {
        dispatcher.handleSmsReceived("10086", "您的话费余额为100元")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_SMS_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `多段短信合并 body`() {
        // 模拟多段短信合并后的结果
        val mergedBody = "第一段内容" + "第二段内容" + "第三段内容"
        dispatcher.handleSmsReceived("10086", mergedBody)

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_SMS_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `空发送者和空内容也能正常分发`() {
        dispatcher.handleSmsReceived("", "")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_SMS_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `连续收到多条短信`() {
        dispatcher.handleSmsReceived("10086", "余额提醒")
        dispatcher.handleSmsReceived("10010", "流量提醒")
        dispatcher.handleSmsReceived("95588", "交易通知")

        assertEquals(3, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_SMS_RECEIVED })
    }

    @Test
    fun `权限缺失时 onRegistered 不注册 receiver`() {
        val noPermDispatcher = object : SmsEventDispatcher() {
            override fun hasSmsPermission() = false
        }
        noPermDispatcher.onRegistered()

        assertFalse(noPermDispatcher.receiverRegistered)
    }

    @Test
    fun `SmsReferent 字段访问正确`() {
        val referent = SmsReferent("10086", "您的话费余额为100元")

        assertEquals("10086", referent.sender)
        assertEquals("您的话费余额为100元", referent.body)
    }

    @Test
    fun `SmsReferent data class 相等性`() {
        val referent1 = SmsReferent("10086", "测试")
        val referent2 = SmsReferent("10086", "测试")

        assertEquals(referent1, referent2)
        assertEquals(referent1.hashCode(), referent2.hashCode())
    }
}
