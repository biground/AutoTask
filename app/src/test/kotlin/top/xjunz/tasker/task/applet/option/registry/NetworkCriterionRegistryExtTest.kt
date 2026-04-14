package top.xjunz.tasker.task.applet.option.registry

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 验证 NetworkCriterionRegistry 中新增 isAirplaneMode 约束的声明正确性。
 */
class NetworkCriterionRegistryExtTest {

    private lateinit var registry: NetworkCriterionRegistry

    @Before
    fun setUp() {
        registry = NetworkCriterionRegistry(44)
    }

    @Test
    fun `isAirplaneMode 存在且可取反`() {
        val option = registry.isAirplaneMode
        assertNotNull(option)
        assertTrue("应可取反", option.isInvertible)
    }
}
