package top.xjunz.tasker.engine.mode

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class InMemoryModeRepository : ModeRepository {

    private val modes = ConcurrentHashMap<String, Mode>()
    private val mutex = Mutex()
    private val _changes = MutableSharedFlow<ModeChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    override suspend fun getMode(id: String): Mode? = modes[id]

    override suspend fun getModeByName(name: String): Mode? =
        modes.values.firstOrNull { it.name == name }

    override suspend fun getActiveMode(): Mode? =
        modes.values.firstOrNull { it.isActive }

    override suspend fun setActiveMode(modeId: String): Unit = mutex.withLock {
        val target = modes[modeId]
            ?: throw IllegalArgumentException("Mode not found: $modeId")

        // 去激活当前活跃模式
        val previousActive = modes.values.firstOrNull { it.isActive }
        if (previousActive != null && previousActive.id != modeId) {
            val deactivated = previousActive.copy(isActive = false)
            modes[previousActive.id] = deactivated
            _changes.tryEmit(ModeChangeEvent.Deactivated(deactivated))
        }

        // 激活目标模式
        val activated = target.copy(isActive = true)
        modes[modeId] = activated
        _changes.tryEmit(ModeChangeEvent.Activated(activated, previousActive))
    }

    override suspend fun deactivateAll(): Unit = mutex.withLock {
        modes.values.filter { it.isActive }.forEach { mode ->
            val deactivated = mode.copy(isActive = false)
            modes[mode.id] = deactivated
            _changes.tryEmit(ModeChangeEvent.Deactivated(deactivated))
        }
    }

    override suspend fun createMode(mode: Mode) {
        modes[mode.id] = mode
        _changes.tryEmit(ModeChangeEvent.Created(mode))
    }

    override suspend fun deleteMode(id: String): Boolean = mutex.withLock {
        val existing = modes[id] ?: return@withLock false
        // 若该模式正在活跃，先去激活
        if (existing.isActive) {
            val deactivated = existing.copy(isActive = false)
            _changes.tryEmit(ModeChangeEvent.Deactivated(deactivated))
        }
        modes.remove(id)
        _changes.tryEmit(ModeChangeEvent.Deleted(id))
        true
    }

    override suspend fun getAllModes(): List<Mode> = modes.values.toList()

    override fun observeChanges(): Flow<ModeChangeEvent> = _changes.asSharedFlow()
}
