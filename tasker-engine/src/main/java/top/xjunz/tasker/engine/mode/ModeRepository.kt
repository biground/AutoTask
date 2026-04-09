package top.xjunz.tasker.engine.mode

import kotlinx.coroutines.flow.Flow

interface ModeRepository {
    suspend fun getMode(id: String): Mode?
    suspend fun getModeByName(name: String): Mode?
    suspend fun getActiveMode(): Mode?
    suspend fun setActiveMode(modeId: String)
    suspend fun deactivateAll()
    suspend fun createMode(mode: Mode)
    suspend fun deleteMode(id: String): Boolean
    suspend fun getAllModes(): List<Mode>
    fun observeChanges(): Flow<ModeChangeEvent>
}
