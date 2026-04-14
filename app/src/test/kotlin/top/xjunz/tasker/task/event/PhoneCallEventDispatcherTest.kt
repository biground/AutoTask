package top.xjunz.tasker.task.event

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.task.applet.flow.ref.PhoneCallReferent

/**
 * PhoneCallEventDispatcher 单元测试
 *
 * 纯 JVM 环境，直接测试 handleCallStateChanged() 逻辑及 PhoneCallReferent。
 */
class PhoneCallEventDispatcherTest {

    private lateinit var dispatcher: PhoneCallEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = PhoneCallEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `来电响铃时分发 RINGING 事件`() {
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_RINGING, "13800138000")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_CALL_STATE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `接听电话时分发 OFFHOOK 事件`() {
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, "13800138000")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_CALL_STATE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `挂断电话时分发 IDLE 事件`() {
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_IDLE, "")

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_CALL_STATE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `完整通话流程 RINGING - OFFHOOK - IDLE 分发三个事件`() {
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_RINGING, "13800138000")
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, "13800138000")
        dispatcher.handleCallStateChanged(TelephonyManager.CALL_STATE_IDLE, "")

        assertEquals(3, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_CALL_STATE_CHANGED })
    }

    @Test
    fun `权限缺失时 onRegistered 不注册`() {
        val noPermDispatcher = object : PhoneCallEventDispatcher() {
            override fun hasPhonePermission() = false
        }
        noPermDispatcher.onRegistered()

        assertFalse(noPermDispatcher.callbackRegistered)
        assertFalse(noPermDispatcher.receiverRegistered)
    }

    @Test
    fun `PhoneCallReferent 字段访问正确`() {
        val referent = PhoneCallReferent("13800138000", TelephonyManager.CALL_STATE_RINGING)

        assertEquals("13800138000", referent.phoneNumber)
        assertEquals(TelephonyManager.CALL_STATE_RINGING, referent.callState)
    }

    @Test
    fun `PhoneCallReferent data class 相等性`() {
        val referent1 = PhoneCallReferent("13800138000", TelephonyManager.CALL_STATE_RINGING)
        val referent2 = PhoneCallReferent("13800138000", TelephonyManager.CALL_STATE_RINGING)

        assertEquals(referent1, referent2)
        assertEquals(referent1.hashCode(), referent2.hashCode())
    }
}