package top.xjunz.tasker.task.location

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicReference

// DataStore 扩展属性
private val Context.amapKeyDataStore by preferencesDataStore(name = "amap_api_key")

/**
 * 高德地图 API Key 管理器
 * 用户在设置界面自行填写 Key，Key 存储在 DataStore Preferences 中
 */
class AmapApiKeyManager(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
    }

    // 内存缓存，避免频繁读 DataStore
    private val cachedKey = AtomicReference<String?>(null)
    private var initialized = false

    /**
     * 初始化：从 DataStore 同步到缓存
     */
    suspend fun initialize() {
        if (!initialized) {
            cachedKey.set(context.amapKeyDataStore.data.first()[API_KEY])
            initialized = true
        }
    }

    suspend fun getApiKey(): String? {
        initialize()
        return cachedKey.get()
    }

    suspend fun setApiKey(key: String) {
        context.amapKeyDataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
        cachedKey.set(key)
        initialized = true
    }

    fun hasApiKey(): Boolean = cachedKey.get()?.isNotBlank() == true

    fun observeApiKeyState(): Flow<Boolean> {
        return context.amapKeyDataStore.data.map { prefs ->
            prefs[API_KEY]?.isNotBlank() == true
        }
    }
}
