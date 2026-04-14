package top.xjunz.tasker.task.applet.option.registry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 验证 GlobalActionRegistry 中 6 个新设备设置动作的声明正确性。
 * 仅验证注册属性（shizukuOnly、isInvertible、参数数量），不执行实际逻辑。
 */
class GlobalActionRegistrySettingsTest {

    private lateinit var registry: GlobalActionRegistry

    @Before
    fun setUp() {
        registry = GlobalActionRegistry(42)
    }

    @Test
    fun `setAutoRotate 存在且标记为 shizukuOnly`() {
        val option = registry.setAutoRotate
        assertNotNull(option)
        assertTrue("应标记为 shizukuOnly", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
    }

    @Test
    fun `toggleBluetooth 存在且标记为 shizukuOnly`() {
        val option = registry.toggleBluetooth
        assertNotNull(option)
        assertTrue("应标记为 shizukuOnly", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
    }

    @Test
    fun `setBrightness 存在且有 Int 参数`() {
        val option = registry.setBrightness
        assertNotNull(option)
        assertTrue("应标记为 shizukuOnly", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
        assertEquals("应有 1 个参数", 1, option.arguments.size)
    }

    @Test
    fun `toggleDarkTheme 存在且标记为 shizukuOnly`() {
        val option = registry.toggleDarkTheme
        assertNotNull(option)
        assertTrue("应标记为 shizukuOnly", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
    }

    @Test
    fun `toggleWifi 存在且标记为 shizukuOnly`() {
        val option = registry.toggleWifi
        assertNotNull(option)
        assertTrue("应标记为 shizukuOnly", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
    }

    @Test
    fun `toggleTorch 存在且不需要 shizuku`() {
        val option = registry.toggleTorch
        assertNotNull(option)
        assertFalse("手电筒不需要 shizuku", option.isShizukuOnly)
        assertFalse("动作不应可取反", option.isInvertible)
    }

    @Test
    fun `parseDeclaredOptions 包含全部 6 个新动作`() {
        registry.parseDeclaredOptions()
        // 通过字段名 hashCode 查找，验证 parseDeclaredOptions 正常注册
        // toggleTorch 有 preset appletId = 0x700E（避免与 waitForIdle 碰撞）
        val names = listOf("setAutoRotate", "toggleBluetooth", "setBrightness",
            "toggleDarkTheme", "toggleWifi")
        for (name in names) {
            val id = name.hashCode() and 0xFFFF
            val found = registry.findAppletOptionById(id)
            assertNotNull("$name (id=$id) 应能通过 findAppletOptionById 查到", found)
            assertEquals("name 应正确设置", name, found!!.name)
        }
        // toggleTorch 使用 preset appletId
        val torchOption = registry.findAppletOptionById(0x700E)
        assertNotNull("toggleTorch (id=0x700E) 应能通过 findAppletOptionById 查到", torchOption)
        assertEquals("toggleTorch", torchOption!!.name)
    }
}
