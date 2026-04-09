/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.engine.variable.VariableRepository
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.CompareValuesConstraint
import top.xjunz.tasker.task.applet.criterion.VariableConstraint

/**
 * 变量条件注册表，注册 VariableConstraint 和 CompareValuesConstraint 约束选项。
 */
class VariableCriterionRegistry(
    id: Int,
    private val repositoryProvider: () -> VariableRepository
) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val checkVariableValue = invertibleAppletOption(R.string.check_variable_value) {
        VariableConstraint(repositoryProvider)
    }.withValueArgument<String>(R.string.variable_name)
        .withValueArgument<Int>(R.string.compare_operation)
        .withValueArgument<String>(R.string.expected_value)

    @AppletOrdinal(0x0001)
    val compareTwoValues = invertibleAppletOption(R.string.compare_two_values) {
        CompareValuesConstraint()
    }.withValueArgument<String>(R.string.left_operand)
        .withValueArgument<Int>(R.string.compare_operation)
        .withValueArgument<String>(R.string.right_operand)
}
