package top.xjunz.tasker.engine.mode

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModeRepositoryTest {

    private lateinit var repository: InMemoryModeRepository

    @Before
    fun setUp() {
        repository = InMemoryModeRepository()
    }

    @Test
    fun testCreateAndGetMode() = runTest {
        val mode = Mode(id = "m1", name = "工作模式")
        repository.createMode(mode)

        val retrieved = repository.getMode("m1")
        assertNotNull(retrieved)
        assertEquals("m1", retrieved!!.id)
        assertEquals("工作模式", retrieved.name)
    }

    @Test
    fun testGetModeByName() = runTest {
        val mode = Mode(id = "m1", name = "睡眠模式")
        repository.createMode(mode)

        val found = repository.getModeByName("睡眠模式")
        assertNotNull(found)
        assertEquals("m1", found!!.id)

        val notFound = repository.getModeByName("不存在")
        assertNull(notFound)
    }

    @Test
    fun testSetActiveModeExclusive() = runTest {
        val mode1 = Mode(id = "m1", name = "工作模式")
        val mode2 = Mode(id = "m2", name = "休息模式")
        repository.createMode(mode1)
        repository.createMode(mode2)

        // 激活第一个
        repository.setActiveMode("m1")
        assertTrue(repository.getMode("m1")!!.isActive)

        // 激活第二个 → 第一个自动去激活
        repository.setActiveMode("m2")
        assertFalse(repository.getMode("m1")!!.isActive)
        assertTrue(repository.getMode("m2")!!.isActive)
    }

    @Test
    fun testDeactivateAll() = runTest {
        val mode1 = Mode(id = "m1", name = "工作模式")
        val mode2 = Mode(id = "m2", name = "休息模式")
        repository.createMode(mode1)
        repository.createMode(mode2)

        repository.setActiveMode("m1")
        repository.setActiveMode("m2")
        // m2 活跃
        assertTrue(repository.getMode("m2")!!.isActive)

        repository.deactivateAll()
        assertNull(repository.getActiveMode())
        assertFalse(repository.getMode("m1")!!.isActive)
        assertFalse(repository.getMode("m2")!!.isActive)
    }

    @Test
    fun testDeleteActiveMode() = runTest {
        val mode = Mode(id = "m1", name = "工作模式")
        repository.createMode(mode)
        repository.setActiveMode("m1")
        assertTrue(repository.getMode("m1")!!.isActive)

        val deleted = repository.deleteMode("m1")
        assertTrue(deleted)
        assertNull(repository.getMode("m1"))
        assertNull(repository.getActiveMode())
    }

    @Test
    fun testGetAllModes() = runTest {
        repository.createMode(Mode(id = "m1", name = "模式A"))
        repository.createMode(Mode(id = "m2", name = "模式B"))
        repository.createMode(Mode(id = "m3", name = "模式C"))

        val all = repository.getAllModes()
        assertEquals(3, all.size)
        assertTrue(all.map { it.id }.containsAll(listOf("m1", "m2", "m3")))
    }

    @Test
    fun testObserveActivatedEvent() = runTest {
        val mode = Mode(id = "m1", name = "工作模式")
        repository.createMode(mode)

        val events = mutableListOf<ModeChangeEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeChanges().collect { events.add(it) }
        }

        repository.setActiveMode("m1")

        // 应包含 Activated 事件
        assertTrue(events.any { it is ModeChangeEvent.Activated })
        val activated = events.filterIsInstance<ModeChangeEvent.Activated>().first()
        assertEquals("m1", activated.mode.id)
        assertTrue(activated.mode.isActive)

        job.cancel()
    }

    @Test
    fun testObserveDeactivatedEvent() = runTest {
        val mode1 = Mode(id = "m1", name = "工作模式")
        val mode2 = Mode(id = "m2", name = "休息模式")
        repository.createMode(mode1)
        repository.createMode(mode2)
        repository.setActiveMode("m1")

        val events = mutableListOf<ModeChangeEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeChanges().collect { events.add(it) }
        }

        // 激活 m2 → m1 应被去激活
        repository.setActiveMode("m2")

        val deactivated = events.filterIsInstance<ModeChangeEvent.Deactivated>()
        assertTrue(deactivated.isNotEmpty())
        assertEquals("m1", deactivated.first().mode.id)

        job.cancel()
    }
}
