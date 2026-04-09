/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.mode.InMemoryModeRepository
import top.xjunz.tasker.engine.mode.Mode

/**
 * ModeConstraint 单元测试
 */
class ModeConstraintTest {

    private lateinit var repo: InMemoryModeRepository

    @Before
    fun setUp() {
        repo = InMemoryModeRepository()
    }

    // ========== 模式激活 + expectedActive=true → 通过 ==========

    @Test
    fun testActiveMode_expectActive_returnsTrue() = runBlocking {
        repo.createMode(Mode(id = "m1", name = "睡眠模式"))
        repo.setActiveMode("m1")

        val constraint = ModeConstraint { repo }
        val result = constraint.check("睡眠模式", expectedActive = true)
        assertTrue(result)
    }

    // ========== 模式激活 + expectedActive=false → 不通过 ==========

    @Test
    fun testActiveMode_expectInactive_returnsFalse() = runBlocking {
        repo.createMode(Mode(id = "m1", name = "睡眠模式"))
        repo.setActiveMode("m1")

        val constraint = ModeConstraint { repo }
        val result = constraint.check("睡眠模式", expectedActive = false)
        assertFalse(result)
    }

    // ========== 模式未激活 + expectedActive=true → 不通过 ==========

    @Test
    fun testInactiveMode_expectActive_returnsFalse() = runBlocking {
        repo.createMode(Mode(id = "m1", name = "睡眠模式", isActive = false))

        val constraint = ModeConstraint { repo }
        val result = constraint.check("睡眠模式", expectedActive = true)
        assertFalse(result)
    }

    // ========== 模式未激活 + expectedActive=false → 通过 ==========

    @Test
    fun testInactiveMode_expectInactive_returnsTrue() = runBlocking {
        repo.createMode(Mode(id = "m1", name = "睡眠模式", isActive = false))

        val constraint = ModeConstraint { repo }
        val result = constraint.check("睡眠模式", expectedActive = false)
        assertTrue(result)
    }

    // ========== 模式不存在 + expectedActive=false → 通过（视为未激活） ==========

    @Test
    fun testNonExistentMode_expectInactive_returnsTrue() = runBlocking {
        val constraint = ModeConstraint { repo }
        val result = constraint.check("不存在的模式", expectedActive = false)
        assertTrue(result)
    }

    // ========== 模式不存在 + expectedActive=true → 不通过 ==========

    @Test
    fun testNonExistentMode_expectActive_returnsFalse() = runBlocking {
        val constraint = ModeConstraint { repo }
        val result = constraint.check("不存在的模式", expectedActive = true)
        assertFalse(result)
    }
}
