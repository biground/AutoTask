package top.xjunz.tasker.task.mode

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.mode.ModeChangeEvent
import top.xjunz.tasker.engine.mode.ModeRepository
import java.util.concurrent.ConcurrentHashMap

// DataStore 扩展属性
private val Context.modeDataStore by preferencesDataStore(name = "autopilot_modes")

// 基于 DataStore Preferences 的 Mode 仓库实现
class DataStoreModeRepository(
    private val context: Context
) : ModeRepository {

    // 内存缓存
    private val memoryCache = ConcurrentHashMap<String, Mode>()

    // 互斥锁：保护激活/去激活操作
    private val mutex = Mutex()

    // 变化事件流
    private val changeFlow = MutableSharedFlow<ModeChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    // 用于 DataStore 序列化的 DTO
    @Serializable
    internal data class ModeDto(
        val id: String,
        val name: String,
        val icon: String? = null,
        val color: Int? = null,
        val isActive: Boolean = false
    )

    private val json = Json { ignoreUnknownKeys = true }

    // 初始化：从 DataStore 加载已持久化的 Mode
    suspend fun initialize() {
        val prefs = context.modeDataStore.data.first()
        for ((key, value) in prefs.asMap()) {
            if (key.name.startsWith("mode_") && value is String) {
                try {
                    val dto = json.decodeFromString<ModeDto>(value)
                    memoryCache[dto.id] = dto.toMode()
                } catch (_: Exception) {
                    // 忽略损坏的数据
                }
            }
        }
    }

    override suspend fun getMode(id: String): Mode? = memoryCache[id]

    override suspend fun getModeByName(name: String): Mode? =
        memoryCache.values.firstOrNull { it.name == name }

    override suspend fun getActiveMode(): Mode? =
        memoryCache.values.firstOrNull { it.isActive }

    override suspend fun setActiveMode(modeId: String): Unit = mutex.withLock {
        val target = memoryCache[modeId]
            ?: throw IllegalArgumentException("Mode not found: $modeId")

        // 去激活当前活跃模式
        val previousActive = memoryCache.values.firstOrNull { it.isActive }
        if (previousActive != null && previousActive.id != modeId) {
            val deactivated = previousActive.copy(isActive = false)
            memoryCache[previousActive.id] = deactivated
            persistMode(deactivated)
            changeFlow.emit(ModeChangeEvent.Deactivated(deactivated))
        }

        // 激活目标模式
        val activated = target.copy(isActive = true)
        memoryCache[modeId] = activated
        persistMode(activated)
        changeFlow.emit(ModeChangeEvent.Activated(activated, previousActive))
        // 记录模式激活历史
        ModeHistoryRepository.getOrInitialize().recordActivation(activated.name)
    }

    override suspend fun deactivateAll(): Unit = mutex.withLock {
        memoryCache.values.filter { it.isActive }.forEach { mode ->
            val deactivated = mode.copy(isActive = false)
            memoryCache[mode.id] = deactivated
            persistMode(deactivated)
            changeFlow.emit(ModeChangeEvent.Deactivated(deactivated))
        }
    }

    override suspend fun createMode(mode: Mode) {
        memoryCache[mode.id] = mode
        persistMode(mode)
        changeFlow.emit(ModeChangeEvent.Created(mode))
    }

    override suspend fun deleteMode(id: String): Boolean = mutex.withLock {
        val existing = memoryCache[id] ?: return@withLock false
        // 若该模式正在活跃，先去激活
        if (existing.isActive) {
            val deactivated = existing.copy(isActive = false)
            persistMode(deactivated)
            changeFlow.emit(ModeChangeEvent.Deactivated(deactivated))
        }
        memoryCache.remove(id)
        removePersistedMode(id)
        changeFlow.emit(ModeChangeEvent.Deleted(id))
        true
    }

    override suspend fun getAllModes(): List<Mode> = memoryCache.values.toList()

    override fun observeChanges(): Flow<ModeChangeEvent> = changeFlow.asSharedFlow()

    // 持久化到 DataStore
    private suspend fun persistMode(mode: Mode) {
        val key = stringPreferencesKey("mode_${mode.id}")
        context.modeDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(mode.toDto())
        }
    }

    // 从 DataStore 移除
    private suspend fun removePersistedMode(id: String) {
        val key = stringPreferencesKey("mode_$id")
        context.modeDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    // Mode ↔ ModeDto 转换
    private fun Mode.toDto() = ModeDto(id, name, icon, color, isActive)
    private fun ModeDto.toMode() = Mode(id, name, icon, color, isActive)
}
