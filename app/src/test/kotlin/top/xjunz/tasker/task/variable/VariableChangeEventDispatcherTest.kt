package top.xjunz.tasker.task.variable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.engine.variable.InMemoryVariableRepository
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType

/**
 * VariableChangeEventDispatcher 单元测试
 *
 * 注意：Event.extras 使用 android.util.SparseArray，在纯 JVM 测试中为 mock 实现（returnDefaultValues=true），
 * 因此这里只验证事件分发逻辑（事件数量、类型），不验证 extras 内容。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VariableChangeEventDispatcherTest {

    private lateinit var repository: InMemoryVariableRepository
    private val receivedEvents = mutableListOf<Event>()

    @Before
    fun setUp() {
        repository = InMemoryVariableRepository()
        receivedEvents.clear()
    }

    @Test
    fun `设置变量后分发 EVENT_ON_VARIABLE_CHANGED 事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = VariableChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.setVariable(
            Variable("v1", "test_var", VariableType.STRING, "hello", VariableScope.GLOBAL)
        )
        advanceUntilIdle()

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_VARIABLE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `更新变量后分发新的事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = VariableChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        val v1 = Variable("v1", "counter", VariableType.INTEGER, 10, VariableScope.GLOBAL)
        repository.setVariable(v1)
        advanceUntilIdle()
        receivedEvents.clear()

        repository.setVariable(v1.copy(value = 20))
        advanceUntilIdle()

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_VARIABLE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `删除变量后分发事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = VariableChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.setVariable(
            Variable("v1", "temp", VariableType.STRING, "val", VariableScope.LOCAL)
        )
        advanceUntilIdle()
        receivedEvents.clear()

        repository.deleteVariable("temp")
        advanceUntilIdle()

        assertEquals(1, receivedEvents.size)
        assertEquals(Event.EVENT_ON_VARIABLE_CHANGED, receivedEvents[0].type)
    }

    @Test
    fun `destroy 后不再分发事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = VariableChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        dispatcher.destroy()
        advanceUntilIdle()

        repository.setVariable(
            Variable("v1", "test", VariableType.STRING, "x", VariableScope.GLOBAL)
        )
        advanceUntilIdle()

        assertTrue(receivedEvents.isEmpty())
    }

    @Test
    fun `多次变量变化产生多个事件`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = VariableChangeEventDispatcher(repository, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events ->
            receivedEvents.addAll(events)
        })
        dispatcher.onRegistered()
        advanceUntilIdle()

        repository.setVariable(
            Variable("v1", "a", VariableType.BOOLEAN, true, VariableScope.GLOBAL)
        )
        repository.setVariable(
            Variable("v2", "b", VariableType.STRING, "hello", VariableScope.GLOBAL)
        )
        advanceUntilIdle()

        assertEquals(2, receivedEvents.size)
        assertTrue(receivedEvents.all { it.type == Event.EVENT_ON_VARIABLE_CHANGED })
    }
}
