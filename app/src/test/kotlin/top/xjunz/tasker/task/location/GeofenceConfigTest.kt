package top.xjunz.tasker.task.location

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GeofenceConfig 数据模型单元测试
 */
class GeofenceConfigTest {

    private fun validConfig() = GeofenceConfig(
        id = "test-uuid-001",
        name = "公司",
        latitude = 39.908823,
        longitude = 116.397470,
        radius = 200f
    )

    // ---- 构造默认值验证 ----

    @Test
    fun `默认值 enabled 为 true`() {
        val config = validConfig()
        assertTrue(config.enabled)
    }

    @Test
    fun `默认值 address 为 null`() {
        val config = validConfig()
        assertNull(config.address)
    }

    // ---- isValid 合法配置 ----

    @Test
    fun `合法配置 isValid 返回 true`() {
        assertTrue(validConfig().isValid())
    }

    // ---- isValid 边界检查 ----

    @Test
    fun `纬度越界（大于90）isValid 返回 false`() {
        assertFalse(validConfig().copy(latitude = 90.1).isValid())
    }

    @Test
    fun `纬度越界（小于-90）isValid 返回 false`() {
        assertFalse(validConfig().copy(latitude = -90.1).isValid())
    }

    @Test
    fun `经度越界（大于180）isValid 返回 false`() {
        assertFalse(validConfig().copy(longitude = 180.1).isValid())
    }

    @Test
    fun `经度越界（小于-180）isValid 返回 false`() {
        assertFalse(validConfig().copy(longitude = -180.1).isValid())
    }

    @Test
    fun `半径小于50 isValid 返回 false`() {
        assertFalse(validConfig().copy(radius = 49.9f).isValid())
    }

    @Test
    fun `半径大于5000 isValid 返回 false`() {
        assertFalse(validConfig().copy(radius = 5000.1f).isValid())
    }

    @Test
    fun `空名称 isValid 返回 false`() {
        assertFalse(validConfig().copy(name = "").isValid())
    }

    @Test
    fun `空白名称 isValid 返回 false`() {
        assertFalse(validConfig().copy(name = "   ").isValid())
    }

    // ---- 边界值恰好在范围内 ----

    @Test
    fun `纬度边界值 90 和 -90 isValid 返回 true`() {
        assertTrue(validConfig().copy(latitude = 90.0).isValid())
        assertTrue(validConfig().copy(latitude = -90.0).isValid())
    }

    @Test
    fun `经度边界值 180 和 -180 isValid 返回 true`() {
        assertTrue(validConfig().copy(longitude = 180.0).isValid())
        assertTrue(validConfig().copy(longitude = -180.0).isValid())
    }

    @Test
    fun `半径边界值 50 和 5000 isValid 返回 true`() {
        assertTrue(validConfig().copy(radius = 50f).isValid())
        assertTrue(validConfig().copy(radius = 5000f).isValid())
    }

    // ---- 序列化往返一致 ----

    @Test
    fun `序列化反序列化往返一致`() {
        val original = GeofenceConfig(
            id = "uuid-roundtrip",
            name = "测试围栏",
            latitude = 31.230416,
            longitude = 121.473701,
            radius = 500f,
            enabled = false,
            address = "上海市黄浦区"
        )
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<GeofenceConfig>(json)
        assertEquals(original, restored)
    }

    // ---- GeofenceTransition 枚举 ----

    @Test
    fun `GeofenceTransition 包含 ENTER 和 EXIT`() {
        val values = GeofenceTransition.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(GeofenceTransition.ENTER))
        assertTrue(values.contains(GeofenceTransition.EXIT))
    }
}
