/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.variable.VariableRepository

/**
 * 变量值约束 (C-INT-001)
 *
 * 检查指定变量的当前值是否满足比较条件。
 *
 * 参数索引：
 * - values[0]: 变量名 (String)
 * - values[1]: 比较操作 (CompareOp 的 ordinal)
 * - values[2]: 期望值 (Any?)
 */
class VariableConstraint(
    private val repositoryProvider: () -> VariableRepository
) : Applet() {

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val varName = values[0] as? String ?: return AppletResult.EMPTY_FAILURE
        val opOrdinal = (values[1] as? Number)?.toInt() ?: return AppletResult.EMPTY_FAILURE
        val op = CompareOp.values().getOrNull(opOrdinal) ?: return AppletResult.EMPTY_FAILURE
        val expectedValue = values[2]

        val variable = repositoryProvider().getVariable(varName)
        val currentValue = variable?.value

        val matched = CompareValuesConstraint.compare(currentValue, expectedValue, op)

        return if (isInverted) {
            AppletResult.emptyResult(!matched)
        } else {
            AppletResult.emptyResult(matched)
        }
    }
}
