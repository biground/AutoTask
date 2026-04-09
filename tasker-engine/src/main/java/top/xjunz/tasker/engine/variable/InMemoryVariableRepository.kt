package top.xjunz.tasker.engine.variable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

// 基于内存的变量仓库实现
open class InMemoryVariableRepository : VariableRepository {

    private val store = ConcurrentHashMap<String, Variable>()

    private val changeFlow = MutableSharedFlow<VariableChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    override suspend fun getVariable(name: String): Variable? {
        return store[name]
    }

    override suspend fun setVariable(variable: Variable) {
        val old = store.put(variable.name, variable)
        changeFlow.emit(VariableChangeEvent.Set(variable, old?.value))
    }

    override suspend fun deleteVariable(name: String): Boolean {
        val removed = store.remove(name)
        if (removed != null) {
            changeFlow.emit(VariableChangeEvent.Deleted(name))
            return true
        }
        return false
    }

    override suspend fun getAllVariables(): List<Variable> {
        return store.values.toList()
    }

    override suspend fun getVariablesByScope(scope: VariableScope): List<Variable> {
        return store.values.filter { it.scope == scope }
    }

    override fun observeChanges(): Flow<VariableChangeEvent> {
        return changeFlow.asSharedFlow()
    }
}
