package top.xjunz.tasker.task.location

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * GeofenceConfigRepository 单元测试
 * 使用 createForTest() 跳过 DataStore，仅测试内存缓存层 CRUD + Flow 订阅
 */
class GeofenceConfigRepositoryTest {

    private lateinit var repo: GeofenceConfigRepository

    private fun sampleConfig(
        id: String = "test-001",
        name: String = "公司",
        latitude: Double = 39.908823,
        longitude: Double = 116.397470,
        radius: Float = 200f
    ) = GeofenceConfig(
        id = id, name = name,
        latitude = latitude, longitude = longitude, radius = radius
    )

    @Before
    fun setUp() {
        repo = GeofenceConfigRepository.createForTest()
    }

    // ---- add ----

    @Test
    fun `addGeofence 后 getAllGeofences 包含新围栏`() = runTest {
        val config = sampleConfig()
        repo.addGeofence(config)

        val all = repo.getAllGeofences()
        assertEquals(1, all.size)
        assertEquals(config, all[0])
    }

    @Test
    fun `addGeofence 多个围栏都保留`() = runTest {
        repo.addGeofence(sampleConfig(id = "a"))
        repo.addGeofence(sampleConfig(id = "b"))

        assertEquals(2, repo.getAllGeofences().size)
    }

    // ---- remove ----

    @Test
    fun `removeGeofence 后 getAllGeofences 不包含该围栏`() = runTest {
        val config = sampleConfig()
        repo.addGeofence(config)
        repo.removeGeofence(config.id)

        assertTrue(repo.getAllGeofences().isEmpty())
    }

    @Test
    fun `removeGeofence 不存在的 ID 不报错`() = runTest {
        repo.removeGeofence("nonexistent")
        assertTrue(repo.getAllGeofences().isEmpty())
    }

    // ---- update ----

    @Test
    fun `updateGeofence 验证字段变化`() = runTest {
        val config = sampleConfig()
        repo.addGeofence(config)

        val updated = config.copy(name = "家", radius = 500f)
        repo.updateGeofence(updated)

        val result = repo.getGeofence(config.id)
        assertNotNull(result)
        assertEquals("家", result!!.name)
        assertEquals(500f, result.radius)
    }

    // ---- get ----

    @Test
    fun `getGeofence 找到已添加的围栏`() = runTest {
        val config = sampleConfig()
        repo.addGeofence(config)

        assertEquals(config, repo.getGeofence(config.id))
    }

    @Test
    fun `getGeofence 找不到不存在的围栏返回 null`() = runTest {
        assertNull(repo.getGeofence("nonexistent"))
    }

    // ---- observe ----

    @Test
    fun `observeGeofences 收到变化通知`() = runTest {
        val results = mutableListOf<List<GeofenceConfig>>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.observeGeofences().collect { results.add(it) }
        }

        repo.addGeofence(sampleConfig(id = "a"))
        repo.addGeofence(sampleConfig(id = "b"))

        // StateFlow: 初始空列表 + 两次 add = 至少 3 次发射
        assertTrue("期望至少 3 次发射，实际 ${results.size}", results.size >= 3)
        assertTrue(results[0].isEmpty())
        assertEquals(1, results[1].size)
        assertEquals(2, results[2].size)

        job.cancel()
    }

    @Test
    fun `observeGeofences 反映 remove 操作`() = runTest {
        val results = mutableListOf<List<GeofenceConfig>>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.observeGeofences().collect { results.add(it) }
        }

        repo.addGeofence(sampleConfig(id = "a"))
        repo.removeGeofence("a")

        val last = results.last()
        assertTrue(last.isEmpty())

        job.cancel()
    }

    // ---- JSON 序列化 ----

    @Test
    fun `List of GeofenceConfig JSON 往返序列化正确`() {
        val configs = listOf(
            sampleConfig(),
            sampleConfig(id = "test-002", name = "家", latitude = 31.0, longitude = 121.0, radius = 300f)
        )
        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(configs)
        val decoded = json.decodeFromString<List<GeofenceConfig>>(encoded)

        assertEquals(configs, decoded)
    }
}
