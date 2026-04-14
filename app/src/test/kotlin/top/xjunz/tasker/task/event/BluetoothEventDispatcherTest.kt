package top.xjunz.tasker.task.event

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * BluetoothEventDispatcher 单元测试
 *
 * 由于纯 JVM 环境无法真实注册 BroadcastReceiver 和操作蓝牙，
 * 这里直接测试提取出来的 handleBluetoothEvent() 逻辑，验证事件分发正确性。
 * extras 因 SparseArray 为 stub 实现不做断言。
 */
class BluetoothEventDispatcherTest {

    private lateinit var dispatcher: BluetoothEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = BluetoothEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `蓝牙状态变化时分发 EVENT_ON_BT_STATE_CHANGED`() {
        dispatcher.handleBluetoothEvent(
            BluetoothAdapter.ACTION_STATE_CHANGED, null, null
        )

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_BT_STATE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `蓝牙设备连接时分发 EVENT_ON_BT_DEVICE_CONNECTED`() {
        dispatcher.handleBluetoothEvent(
            BluetoothDevice.ACTION_ACL_CONNECTED, "MyHeadset", "AA:BB:CC:DD:EE:FF"
        )

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_BT_DEVICE_CONNECTED, receivedEvents[0].type)
    }

    @Test
    fun `蓝牙设备断开时分发 EVENT_ON_BT_DEVICE_DISCONNECTED`() {
        dispatcher.handleBluetoothEvent(
            BluetoothDevice.ACTION_ACL_DISCONNECTED, "MyHeadset", "AA:BB:CC:DD:EE:FF"
        )

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_BT_DEVICE_DISCONNECTED, receivedEvents[0].type)
    }

    @Test
    fun `设备信息为 null 时使用空字符串兜底`() {
        dispatcher.handleBluetoothEvent(
            BluetoothDevice.ACTION_ACL_CONNECTED, null, null
        )

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_BT_DEVICE_CONNECTED, receivedEvents[0].type)
    }

    @Test
    fun `未知 action 不分发事件`() {
        dispatcher.handleBluetoothEvent("com.example.UNKNOWN", null, null)

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `null action 不分发事件`() {
        dispatcher.handleBluetoothEvent(null, null, null)

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `权限缺失时 onRegistered 不注册 receiver`() {
        val noPermDispatcher = object : BluetoothEventDispatcher() {
            override fun hasBluetoothPermission() = false
        }
        noPermDispatcher.onRegistered()

        assertFalse(noPermDispatcher.receiverRegistered)
    }
}
