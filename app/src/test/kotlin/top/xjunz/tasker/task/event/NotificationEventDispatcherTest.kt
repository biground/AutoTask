package top.xjunz.tasker.task.event

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * NotificationEventDispatcher 单元测试
 *
 * 纯 JVM 环境，直接测试 handleNotificationPosted() 逻辑。
 */
class NotificationEventDispatcherTest {

    private lateinit var dispatcher: NotificationEventDispatcher
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        dispatcher = NotificationEventDispatcher()
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        receivedEvents.clear()
    }

    @Test
    fun `收到通知时分发 EVENT_ON_NOTIFICATION_RECEIVED`() {
        dispatcher.handleNotificationPosted(
            packageName = "com.example.app",
            title = "标题",
            text = "内容",
            subText = "副文本",
            postTime = 1000L
        )

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_NOTIFICATION_RECEIVED, receivedEvents[0].type)
    }

    @Test
    fun `componentInfo packageName 等于传入的 packageName`() {
        dispatcher.handleNotificationPosted(
            packageName = "com.example.app",
            title = "标题",
            text = "内容",
            subText = null,
            postTime = 2000L
        )

        assertEquals("com.example.app", receivedEvents[0].componentInfo.packageName)
    }

    @Ignore("SparseArray 在纯 JVM 环境为 stub，无法验证 extras")
    @Test
    fun `extras 包含 title text subText postTime`() {
        dispatcher.handleNotificationPosted(
            packageName = "com.example.app",
            title = "通知标题",
            text = "通知内容",
            subText = "副标题",
            postTime = 12345L
        )

        val event = receivedEvents[0]
        assertEquals("通知标题", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_TITLE))
        assertEquals("通知内容", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_TEXT))
        assertEquals("副标题", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_SUB_TEXT))
        assertEquals(12345L, event.getExtra<Long>(NotificationEventDispatcher.EXTRA_NOTIF_POST_TIME))
    }

    @Ignore("SparseArray 在纯 JVM 环境为 stub，无法验证 extras")
    @Test
    fun `null 参数处理 — title text subText 为 null 时存为空字符串`() {
        dispatcher.handleNotificationPosted(
            packageName = "com.example.app",
            title = null,
            text = null,
            subText = null,
            postTime = 0L
        )

        val event = receivedEvents[0]
        assertEquals("", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_TITLE))
        assertEquals("", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_TEXT))
        assertEquals("", event.getExtra<String>(NotificationEventDispatcher.EXTRA_NOTIF_SUB_TEXT))
    }

    @Test
    fun `多次调用分发多个事件`() {
        dispatcher.handleNotificationPosted("com.app1", "t1", "c1", null, 100L)
        dispatcher.handleNotificationPosted("com.app2", "t2", "c2", "sub", 200L)
        dispatcher.handleNotificationPosted("com.app3", null, null, null, 300L)

        assertEquals(3, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_NOTIFICATION_RECEIVED })
        assertEquals("com.app1", receivedEvents[0].componentInfo.packageName)
        assertEquals("com.app2", receivedEvents[1].componentInfo.packageName)
        assertEquals("com.app3", receivedEvents[2].componentInfo.packageName)
    }
}
