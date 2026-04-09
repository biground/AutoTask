/*
 * Copyright (c) 2024 ��. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import top.xjunz.tasker.engine.applet.action.ArgumentAction
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType

/**
 * 设置变量值的动作 (A-DATA-006)
 *
 * 参数索引：
 * - values[0]: 目标变量名 (String)
 * - values[1]: 变量类型 (VariableType 的 ordinal)
 * - values[2]: 变量值 (根据类型解析)
 * - values[3]: 变量作用域 (VariableScope 的 ordinal，可选，默认 GLOBAL)
 * - values[4]: 是否持久化 (Boolean，可选，默认 false)
 */
class SetVariableAction(
    private val repositoryProvider: () -> VariableRepository
) : ArgumentAction() {

    override suspend fun doAction(args: Array<Any?>, runtime: TaskRuntime): AppletResult {
        val name = args[0] as? String ?: return AppletResult.EMPTY_FAILURE

        // 变量名验证：非空、不以 __ 开头结尾、不含 ${}、长度 ≤128
        if (!isValidVariableName(name)) return AppletResult.EMPTY_FAILURE

        val typeOrdinal = (args[1] as? Number)?.toInt() ?: return AppletResult.EMPTY_FAILURE
        val type = VariableType.values().getOrNull(typeOrdinal) ?: return AppletResult.EMPTY_FAILURE
        val rawValue = args[2]
        val scopeOrdinal = (args.getOrNull(3) as? Number)?.toInt() ?: VariableScope.GLOBAL.ordinal
        val scope = VariableScope.values().getOrNull(scopeOrdinal) ?: VariableScope.GLOBAL
        val persisted = args.getOrNull(4) as? Boolean ?: false

        // 类型转换/校验
        val typedValue = coerceValue(rawValue, type) ?: return AppletResult.EMPTY_FAILURE

        val existingVar = repositoryProvider().getVariable(name)
        val variable = Variable(
            id = existingVar?.id ?: java.util.UUID.randomUUID().toString(),
            name = name,
            type = type,
            value = typedValue,
            scope = scope,
            persisted = persisted
        )

        repositoryProvider().setVariable(variable)
        return AppletResult.EMPTY_SUCCESS
    }

    companion object {
        /**
         * 校验变量名是否合法：
         * - 非空白
         * - 长度 ≤128
         * - 不以 __ 开头且以 __ 结尾（系统保留）
         * - 不含 ${ 或 }（防止模板注入）
         */
        fun isValidVariableName(name: String): Boolean {
            if (name.isBlank() || name.length > 128) return false
            if (name.startsWith("__") && name.endsWith("__")) return false
            if (name.contains("\${") || name.contains("}")) return false
            return true
        }

        /**
         * 将原始值强转为目标类型，转换失败返回 null
         */
        fun coerceValue(raw: Any?, type: VariableType): Any? {
            if (raw == null) return null
            return when (type) {
                VariableType.BOOLEAN -> when (raw) {
                    is Boolean -> raw
                    is String -> raw.toBooleanStrictOrNull()
                    else -> null
                }
                VariableType.INTEGER -> when (raw) {
                    is Long -> raw
                    is Int -> raw.toLong()
                    is Number -> raw.toLong()
                    is String -> raw.toLongOrNull()
                    else -> null
                }
                VariableType.DECIMAL -> when (raw) {
                    is Double -> raw
                    is Float -> raw.toDouble()
                    is Number -> raw.toDouble()
                    is String -> raw.toDoubleOrNull()
                    else -> null
                }
                VariableType.STRING -> raw.toString()
            }
        }
    }
}
