package top.xjunz.tasker.engine.variable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryVariableRepositoryTest {

    private lateinit var repo: InMemoryVariableRepository

    @Before
    fun setUp() {
        repo = InMemoryVariableRepository()
    }

    private fun makeVar(
        name: String,
        value: Any? = null,
        scope: VariableScope = VariableScope.LOCAL
    ) = Variable(
        id = name,
        name = name,
        type = VariableType.STRING,
        value = value,
        scope = scope
    )

    @Test
    fun testSetAndGetVariable() = runTest {
        val v = makeVar("greeting", "hello")
        repo.setVariable(v)
        val result = repo.getVariable("greeting")
        assertEquals(v, result)
    }

    @Test
    fun testDeleteVariable() = runTest {
        val v = makeVar("temp", "x")
        repo.setVariable(v)
        val deleted = repo.deleteVariable("temp")
        assertTrue(deleted)
        assertNull(repo.getVariable("temp"))
    }

    @Test
    fun testGetAllVariables() = runTest {
        val v1 = makeVar("a", "1")
        val v2 = makeVar("b", "2")
        repo.setVariable(v1)
        repo.setVariable(v2)
        val all = repo.getAllVariables()
        assertEquals(2, all.size)
        assertTrue(all.containsAll(listOf(v1, v2)))
    }

    @Test
    fun testGetVariablesByScope() = runTest {
        val local = makeVar("loc", "1", VariableScope.LOCAL)
        val global = makeVar("glo", "2", VariableScope.GLOBAL)
        repo.setVariable(local)
        repo.setVariable(global)

        val locals = repo.getVariablesByScope(VariableScope.LOCAL)
        assertEquals(1, locals.size)
        assertEquals(local, locals[0])

        val globals = repo.getVariablesByScope(VariableScope.GLOBAL)
        assertEquals(1, globals.size)
        assertEquals(global, globals[0])
    }

    @Test
    fun testObserveSetEvent() = runTest {
        val v = makeVar("x", "val")
        val event = mutableListOf<VariableChangeEvent>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.observeChanges().first().also { event.add(it) }
        }

        repo.setVariable(v)
        job.join()

        assertEquals(1, event.size)
        val e = event[0] as VariableChangeEvent.Set
        assertEquals(v, e.variable)
        assertNull(e.oldValue)
    }

    @Test
    fun testObserveDeletedEvent() = runTest {
        val v = makeVar("y", "val")
        repo.setVariable(v)

        val event = mutableListOf<VariableChangeEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.observeChanges().first().also { event.add(it) }
        }

        repo.deleteVariable("y")
        job.join()

        assertEquals(1, event.size)
        val e = event[0] as VariableChangeEvent.Deleted
        assertEquals("y", e.name)
    }

    @Test
    fun testGetNonExistentReturnsNull() = runTest {
        assertNull(repo.getVariable("nonexistent"))
    }

    @Test
    fun testDeleteNonExistentReturnsFalse() = runTest {
        assertFalse(repo.deleteVariable("nonexistent"))
    }
}
