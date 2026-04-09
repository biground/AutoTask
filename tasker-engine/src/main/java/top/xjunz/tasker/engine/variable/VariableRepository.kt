package top.xjunz.tasker.engine.variable

import kotlinx.coroutines.flow.Flow

// 变量仓库接口
interface VariableRepository {
    suspend fun getVariable(name: String): Variable?
    suspend fun setVariable(variable: Variable)
    suspend fun deleteVariable(name: String): Boolean
    suspend fun getAllVariables(): List<Variable>
    suspend fun getVariablesByScope(scope: VariableScope): List<Variable>
    fun observeChanges(): Flow<VariableChangeEvent>
}
