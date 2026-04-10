package top.xjunz.tasker.task.location

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// DataStore 扩展属性
private val Context.geofenceDataStore by preferencesDataStore(name = "autopilot_geofences")

/**
 * 围栏配置持久化仓库
 * 双层存储：MutableStateFlow 内存缓存 + DataStore Preferences (JSON) 持久化
 */
class GeofenceConfigRepository private constructor(
    private val context: Context?,
    private val persistEnabled: Boolean
) {

    constructor(context: Context) : this(context, true)

    // 内存缓存
    private val _configs = MutableStateFlow<List<GeofenceConfig>>(emptyList())

    // 互斥锁：保护并发写操作
    private val mutex = Mutex()

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val GEOFENCES_KEY = stringPreferencesKey("geofences_json")

        // 测试用工厂方法：跳过 DataStore，仅使用内存缓存
        @VisibleForTesting
        internal fun createForTest(): GeofenceConfigRepository {
            return GeofenceConfigRepository(null, false)
        }
    }

    // 从 DataStore 加载到内存缓存
    suspend fun initialize() {
        if (!persistEnabled || context == null) return
        val prefs = context.geofenceDataStore.data.first()
        val raw = prefs[GEOFENCES_KEY]
        if (raw != null) {
            try {
                _configs.value = json.decodeFromString<List<GeofenceConfig>>(raw)
            } catch (_: Exception) {
                // 忽略损坏的数据
            }
        }
    }

    suspend fun addGeofence(config: GeofenceConfig): Unit = mutex.withLock {
        _configs.value = _configs.value + config
        persist()
    }

    suspend fun removeGeofence(id: String): Unit = mutex.withLock {
        _configs.value = _configs.value.filter { it.id != id }
        persist()
    }

    suspend fun updateGeofence(config: GeofenceConfig): Unit = mutex.withLock {
        _configs.value = _configs.value.map { if (it.id == config.id) config else it }
        persist()
    }

    suspend fun getGeofence(id: String): GeofenceConfig? = _configs.value.find { it.id == id }

    suspend fun getAllGeofences(): List<GeofenceConfig> = _configs.value

    fun observeGeofences(): Flow<List<GeofenceConfig>> = _configs.asStateFlow()

    // 内存缓存 → JSON → DataStore
    private suspend fun persist() {
        if (!persistEnabled || context == null) return
        val encoded = json.encodeToString(_configs.value)
        context.geofenceDataStore.edit { prefs ->
            prefs[GEOFENCES_KEY] = encoded
        }
    }
}
