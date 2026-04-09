/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 比较两个值的约束 (C-SYS-006)
 *
 * 支持 8 种比较操作，见 [CompareOp]。
 * 可比较变量值、字面量或表达式结果。
 *
 * 参数索引：
 * - values[0]: 左操作数 (Any?)
 * - values[1]: 比较操作 (CompareOp 的 ordinal)
 * - values[2]: 右操作数 (Any?)
 */
class CompareValuesConstraint : Applet() {

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val left = values[0]
        val opOrdinal = (values[1] as? Number)?.toInt() ?: return AppletResult.EMPTY_FAILURE
        val op = CompareOp.values().getOrNull(opOrdinal) ?: return AppletResult.EMPTY_FAILURE
        val right = values[2]
        val result = compare(left, right, op)
        return if (isInverted) {
            AppletResult.emptyResult(!result)
        } else {
            AppletResult.emptyResult(result)
        }
    }

    companion object {
        /**
         * 比较两个值。
         * - 数值比较：统一转为 Double 再比较
         * - 字符串操作（CONTAINS, MATCHES_REGEX）：转为 String 再操作
         * - 等值/不等值：支持 null 比较
         */
        fun compare(left: Any?, right: Any?, op: CompareOp): Boolean {
            // null 处理
            if (left == null && right == null) {
                return op == CompareOp.EQUAL || op == CompareOp.GREATER_EQUAL || op == CompareOp.LESS_EQUAL
            }
            if (left == null || right == null) {
                return op == CompareOp.NOT_EQUAL
            }

            return when (op) {
                CompareOp.EQUAL -> compareNumericOrEquals(left, right) == 0
                CompareOp.NOT_EQUAL -> compareNumericOrEquals(left, right) != 0
                CompareOp.GREATER_THAN -> compareNumeric(left, right)?.let { it > 0 } ?: false
                CompareOp.LESS_THAN -> compareNumeric(left, right)?.let { it < 0 } ?: false
                CompareOp.GREATER_EQUAL -> compareNumeric(left, right)?.let { it >= 0 } ?: false
                CompareOp.LESS_EQUAL -> compareNumeric(left, right)?.let { it <= 0 } ?: false
                CompareOp.CONTAINS -> left.toString().contains(right.toString())
                CompareOp.MATCHES_REGEX -> {
                    try {
                        Regex(right.toString()).containsMatchIn(left.toString())
                    } catch (_: Exception) {
                        false
                    }
                }
            }
        }

        /**
         * 数值比较。如果两个值都能转为数值则比较，否则返回 null。
         */
        private fun compareNumeric(left: Any, right: Any): Int? {
            val l = toDouble(left) ?: return null
            val r = toDouble(right) ?: return null
            return l.compareTo(r)
        }

        /**
         * 数值比较，如果不是数值则回退到 equals。
         */
        private fun compareNumericOrEquals(left: Any, right: Any): Int {
            val numResult = compareNumeric(left, right)
            if (numResult != null) return numResult
            return if (left == right || left.toString() == right.toString()) 0 else 1
        }

        private fun toDouble(value: Any): Double? {
            return when (value) {
                is Double -> value
                is Float -> value.toDouble()
                is Long -> value.toDouble()
                is Int -> value.toDouble()
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }
        }
    }
}
