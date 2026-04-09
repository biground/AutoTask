/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

/**
 * 比较操作枚举：8 种比较操作 (C-SYS-006)
 */
enum class CompareOp {
    EQUAL,          // ==
    NOT_EQUAL,      // !=
    GREATER_THAN,   // >
    LESS_THAN,      // <
    GREATER_EQUAL,  // >=
    LESS_EQUAL,     // <=
    CONTAINS,       // 字符串包含
    MATCHES_REGEX   // 正则匹配
}
