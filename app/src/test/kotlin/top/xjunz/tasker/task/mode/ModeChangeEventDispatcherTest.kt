package top.xjunz.tasker.task.mode

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.mode.InMemoryModeRepository
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * ModeChangeEventDispatcher 单元测试
 *
 * 注意：Event.extras 使用 android.util.SparseArray，在纯 JVM 测试中为 mock 实现（returnDefaultValues=true），
 * 因此这里只验证事件分发逻辑（事件数量、类型），不验证 extras 内容。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ModeChangeEventDispatcherTest {

    private lateinit var repository: InMemoryModeRepository
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        repository = InMemoryModeRepository()
        receivedEvents.clear()
    }

    @Test
    fun `激活模式后分发 EVENT_ON_MODE_CHANGED 事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = ModeChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.createMode(Mode("m1", "工作模式"))
        advanceUntilIdle()
        receivedEvents.clear()

        repository.setActiveMode("m1")
        advanceUntilIdle()

        // setActiveMode 产生 Activated 事件
        assertTrue(receivedEvents.isNotEmpty())
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_MODE_CHANGED })
    }

    @Test
    fun `去激活模式后分发事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = ModeChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.createMode(Mode("m1", "休息模式"))
        repository.setActiveMode("m1")
        advanceUntilIdle()
        receivedEvents.clear()

        repository.deactivateAll()
        advanceUntilIdle()

        assertTrue(receivedEvents.isNotEmpty())
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_MODE_CHANGED })
    }

    @Test
    fun `destroy 后不再分发事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = ModeChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        dispatcher.destroy()
        advanceUntilIdle()

        repository.createMode(Mode("m1", "静音模式"))
        repository.setActiveMode("m1")
        advanceUntilIdle()

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `切换模式时产生去激活和激活两个事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = ModeChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.createMode(Mode("m1", "工作模式"))
        repository.createMode(Mode("m2", "休息模式"))
        repository.setActiveMode("m1")
        advanceUntilIdle()
        receivedEvents.clear()

        // 从 m1 切到 m2 → 先 Deactivated(m1) 再 Activated(m2)
        repository.setActiveMode("m2")
        advanceUntilIdle()

        assertEquals(2, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_MODE_CHANGED })
    }

    @Test
    fun `仅监听激活和去激活事件而忽略创建和删除`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = ModeChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        // Created 和 Deleted 不应产生事件
        repository.createMode(Mode("m1", "测试模式"))
        advanceUntilIdle()
        assertEquals(0, receivedEvents.size)

        repository.deleteMode("m1")
        advanceUntilIdle()
        assertEquals(0, receivedEvents.size)
    }
}
