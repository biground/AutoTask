package top.xjunz.tasker.task.mode

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// DataStoreModeRepository 的 DTO 序列化/反序列化测试
// DataStore 操作需要 Android Context，此处仅验证序列化层的正确性
class DataStoreModeRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testModeDtoSerializeDeserialize() {
        val dto = DataStoreModeRepository.ModeDto(
            id = "m1",
            name = "工作模式",
            icon = "ic_work",
            color = 0xFF0000,
            isActive = true
        )
        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<DataStoreModeRepository.ModeDto>(encoded)

        assertEquals("m1", decoded.id)
        assertEquals("工作模式", decoded.name)
        assertEquals("ic_work", decoded.icon)
        assertEquals(0xFF0000, decoded.color)
        assertEquals(true, decoded.isActive)
    }

    @Test
    fun testModeDtoDefaultValues() {
        val dto = DataStoreModeRepository.ModeDto(
            id = "m2",
            name = "睡眠模式"
        )
        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<DataStoreModeRepository.ModeDto>(encoded)

        assertEquals("m2", decoded.id)
        assertEquals("睡眠模式", decoded.name)
        assertNull(decoded.icon)
        assertNull(decoded.color)
        assertEquals(false, decoded.isActive)
    }

    @Test
    fun testModeDtoIgnoresUnknownKeys() {
        val rawJson = """{"id":"m3","name":"测试","unknownField":"xyz","isActive":false}"""
        val decoded = json.decodeFromString<DataStoreModeRepository.ModeDto>(rawJson)

        assertEquals("m3", decoded.id)
        assertEquals("测试", decoded.name)
        assertEquals(false, decoded.isActive)
    }
}
