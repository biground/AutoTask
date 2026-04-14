package top.xjunz.tasker.task.applet.option.registry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 验证 GlobalCriterionRegistry 中新增约束（isDeviceLocked / isHeadphoneConnected /
 * isDarkMode / callState / isMacroRunning）的声明正确性。
 */
class GlobalCriterionRegistryExtTest {

    private lateinit var registry: GlobalCriterionRegistry

    @Before
    fun setUp() {
        registry = GlobalCriterionRegistry(43)
    }

    @Test
    fun `isDeviceLocked 存在且可取反`() {
        val option = registry.isDeviceLocked
        assertNotNull(option)
        assertTrue("应可取反", option.isInvertible)
    }

    @Test
    fun `isHeadphoneConnected 存在且可取反`() {
        val option = registry.isHeadphoneConnected
        assertNotNull(option)
        assertTrue("应可取反", option.isInvertible)
    }

    @Test
    fun `isDarkMode 存在且可取反`() {
        val option = registry.isDarkMode
        assertNotNull(option)
        assertTrue("应可取反", option.isInvertible)
    }

    @Test
    fun `callState 使用 unaryEqual 模式且不可取反`() {
        val option = registry.callState
        assertNotNull(option)
        assertFalse("unaryEqual 不应可取反", option.isInvertible)
    }

    @Test
    fun `isMacroRunning 存在且可取反`() {
        val option = registry.isMacroRunning
        assertNotNull(option)
        assertTrue("应可取反", option.isInvertible)
    }
}
