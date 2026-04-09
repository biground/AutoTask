package top.xjunz.tasker.task.variable

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableChangeEvent
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType
import java.util.concurrent.ConcurrentHashMap

// DataStore 扩展属性
private val Context.variableDataStore by preferencesDataStore(name = "autopilot_variables")

// 基于 DataStore Preferences 的变量仓库实现
class DataStoreVariableRepository(
    private val context: Context
) : VariableRepository {

    // 内存缓存：所有变量（含非持久化的局部变量）
    private val memoryCache = ConcurrentHashMap<String, Variable>()

    // 变化事件流
    private val changeFlow = MutableSharedFlow<VariableChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    // 用于 DataStore 序列化的 DTO
    @Serializable
    internal data class VariableDto(
        val id: String,
        val name: String,
        val type: String,
        val value: String?,
        val scope: String
    )

    internal val json = Json { ignoreUnknownKeys = true }

    // 初始化：从 DataStore 加载持久化变量
    suspend fun initialize() {
        val prefs = context.variableDataStore.data.first()
        for ((key, value) in prefs.asMap()) {
            if (key.name.startsWith("var_") && value is String) {
                try {
                    val dto = json.decodeFromString<VariableDto>(value)
                    val variable = dtoToVariable(dto)
                    memoryCache[variable.name] = variable
                } catch (_: Exception) {
                    // 忽略损坏的数据
                }
            }
        }
    }

    override suspend fun getVariable(name: String): Variable? = memoryCache[name]

    override suspend fun setVariable(variable: Variable) {
        val oldValue = memoryCache[variable.name]?.value
        memoryCache[variable.name] = variable

        // 仅持久化 persisted=true 的变量
        if (variable.persisted) {
            persistVariable(variable)
        }

        changeFlow.emit(VariableChangeEvent.Set(variable, oldValue))
    }

    override suspend fun deleteVariable(name: String): Boolean {
        val removed = memoryCache.remove(name) ?: return false

        if (removed.persisted) {
            removePersistedVariable(name)
        }

        changeFlow.emit(VariableChangeEvent.Deleted(name))
        return true
    }

    override suspend fun getAllVariables(): List<Variable> = memoryCache.values.toList()

    override suspend fun getVariablesByScope(scope: VariableScope): List<Variable> =
        memoryCache.values.filter { it.scope == scope }

    override fun observeChanges(): Flow<VariableChangeEvent> = changeFlow.asSharedFlow()

    // 持久化到 DataStore
    private suspend fun persistVariable(variable: Variable) {
        val dto = variableToDto(variable)
        val key = stringPreferencesKey("var_${variable.name}")
        context.variableDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(dto)
        }
    }

    // 从 DataStore 移除
    private suspend fun removePersistedVariable(name: String) {
        val key = stringPreferencesKey("var_$name")
        context.variableDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    companion object Serialization {

        // 值序列化：Any? → String?
        internal fun serializeValue(value: Any?, type: VariableType): String? {
            if (value == null) return null
            return when (type) {
                VariableType.BOOLEAN -> (value as Boolean).toString()
                VariableType.INTEGER -> (value as Long).toString()
                VariableType.DECIMAL -> (value as Double).toString()
                VariableType.STRING -> value as String
            }
        }

        // 值反序列化：String? → Any?
        internal fun deserializeValue(raw: String?, type: VariableType): Any? {
            if (raw == null) return null
            return when (type) {
                VariableType.BOOLEAN -> raw.toBooleanStrict()
                VariableType.INTEGER -> raw.toLong()
                VariableType.DECIMAL -> raw.toDouble()
                VariableType.STRING -> raw
            }
        }

        // Variable → DTO
        internal fun variableToDto(variable: Variable) = VariableDto(
            id = variable.id,
            name = variable.name,
            type = variable.type.name,
            value = serializeValue(variable.value, variable.type),
            scope = variable.scope.name
        )

        // DTO → Variable（从 DataStore 恢复的一定是 persisted）
        internal fun dtoToVariable(dto: VariableDto): Variable {
            val type = VariableType.valueOf(dto.type)
            return Variable(
                id = dto.id,
                name = dto.name,
                type = type,
                value = deserializeValue(dto.value, type),
                scope = VariableScope.valueOf(dto.scope),
                persisted = true
            )
        }
    }
}
