package top.xjunz.tasker.task.applet.option.registry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * 验证 TextActionRegistry 中 makeToast (A-UI-007) 的声明存在性。
 */
class TextActionRegistryToastTest {

    private lateinit var registry: TextActionRegistry

    @Before
    fun setUp() {
        registry = TextActionRegistry(45)
    }

    @Test
    fun `makeToast 声明存在`() {
        val option = registry.makeToast
        assertNotNull("makeToast 应存在于 TextActionRegistry 中", option)
    }

    @Test
    fun `makeToast 不可取反且有参数`() {
        val option = registry.makeToast
        assertFalse("动作不应可取反", option.isInvertible)
        // makeToast 使用 withValueArgument，应有 1 个参数
        assertNotNull("应有参数列表", option.arguments)
    }
}
