package top.xjunz.tasker.task.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.mode.InMemoryModeRepository
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.engine.task.EventDispatcher
import top.xjunz.tasker.engine.variable.InMemoryVariableRepository
import top.xjunz.tasker.engine.variable.MagicVariableProvider
import top.xjunz.tasker.engine.variable.MagicVariableRegistry
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType
import top.xjunz.tasker.task.applet.action.DeleteVariableAction
import top.xjunz.tasker.task.applet.action.SetModeAction
import top.xjunz.tasker.task.applet.action.SetVariableAction
import top.xjunz.tasker.task.applet.criterion.CompareOp
import top.xjunz.tasker.task.applet.criterion.CompareValuesConstraint
import top.xjunz.tasker.task.applet.criterion.ModeConstraint
import top.xjunz.tasker.task.applet.criterion.VariableConstraint
import top.xjunz.tasker.task.applet.util.ExpressionHelper
import top.xjunz.tasker.task.mode.ModeChangeEventDispatcher
import top.xjunz.tasker.task.variable.VariableChangeEventDispatcher

/**
 * 变量系统 + 模式系统 端到端集成测试
 *
 * 覆盖 9 个场景：
 * A-D 变量系统（自增、比较、删除、魔术变量）
 * E-F 模式系统（激活/约束、互斥）
 * G-H 跨系统事件（变量/模式变化事件分发）
 * I   序列化兼容性（变量存取一致性）
 *
 * 注意：Event.extras 基于 android.util.SparseArray，纯 JVM 下为 stub 实现
 * （returnDefaultValues=true），因此仅验证事件数量和类型，不验证 extras 内容。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VariableModeIntegrationTest {

    private lateinit var variableRepo: InMemoryVariableRepository
    private lateinit var modeRepo: InMemoryModeRepository
    private lateinit var mockRuntime: TaskRuntime

    @Before
    fun setUp() {
        variableRepo = InMemoryVariableRepository()
        modeRepo = InMemoryModeRepository()
        mockRuntime = createMockRuntime()
    }

    /**
     * 反射创建 TaskRuntime（私有构造器），配合 returnDefaultValues=true
     */
    private fun createMockRuntime(): TaskRuntime {
        val ctor = TaskRuntime::class.java.getDeclaredConstructor()
        ctor.isAccessible = true
        return ctor.newInstance()
    }

    // ========== 场景 A: 变量自增 ==========

    @Test
    fun `场景A - 变量自增 SetVariable后通过表达式自增并验证约束`() = runTest {
        val setAction = SetVariableAction { variableRepo }

        // 1. 设置 count = 0
        val initResult = setAction.doAction(
            arrayOf("count", VariableType.INTEGER.ordinal, "0", VariableScope.GLOBAL.ordinal, false),
            mockRuntime
        )
        assertTrue("初始设置应成功", initResult.isSuccessful)
        assertEquals(0L, variableRepo.getVariable("count")?.value)

        // 2. 自增：count = ${count} + 1（表达式引擎解析 ${count} → 0，计算 0+1=1）
        val incrResult = setAction.doAction(
            arrayOf("count", VariableType.INTEGER.ordinal, "\${count} + 1", VariableScope.GLOBAL.ordinal, false),
            mockRuntime
        )
        assertTrue("自增应成功", incrResult.isSuccessful)

        // 3. VariableConstraint 验证 count == 1
        val constraint = VariableConstraint { variableRepo }
        constraint.values = mapOf(0 to "count", 1 to CompareOp.EQUAL.ordinal, 2 to 1L)
        val constraintResult = constraint.apply(mockRuntime)
        assertTrue("约束检查应通过 count==1", constraintResult.isSuccessful)
    }

    // ========== 场景 B: 字符串变量比较 ==========

    @Test
    fun `场景B - 字符串变量比较 SetVariable后CompareValues验证相等`() = runTest {
        val setAction = SetVariableAction { variableRepo }

        // 设置 name = "AutoPilot"
        setAction.doAction(
            arrayOf("name", VariableType.STRING.ordinal, "AutoPilot", VariableScope.GLOBAL.ordinal, false),
            mockRuntime
        )

        // 通过表达式引擎解析 ${name} → "AutoPilot"
        val resolved = ExpressionHelper.evaluateExpression("\${name}", variableRepo)
        assertEquals("AutoPilot", resolved)

        // CompareValuesConstraint 比较解析结果与期望值
        val compare = CompareValuesConstraint()
        compare.values = mapOf(0 to (resolved ?: ""), 1 to CompareOp.EQUAL.ordinal, 2 to "AutoPilot")
        val result = compare.apply(mockRuntime)
        assertTrue("字符串比较应通过", result.isSuccessful)
    }

    // ========== 场景 C: 删除变量 ==========

    @Test
    fun `场景C - 删除变量 SetVariable后DeleteVariable再验证为null`() = runTest {
        val setAction = SetVariableAction { variableRepo }
        val deleteAction = DeleteVariableAction { variableRepo }

        // 设置 temp = "hello"
        setAction.doAction(
            arrayOf("temp", VariableType.STRING.ordinal, "hello", VariableScope.GLOBAL.ordinal, false),
            mockRuntime
        )
        assertNotNull("变量应已创建", variableRepo.getVariable("temp"))

        // 删除 temp
        val delResult = deleteAction.doAction(arrayOf("temp"), mockRuntime)
        assertTrue("删除应成功", delResult.isSuccessful)

        // 验证 temp 为 null
        assertNull("temp 应已被删除", variableRepo.getVariable("temp"))
    }

    // ========== 场景 D: 魔术变量在表达式中可用 ==========

    @Test
    fun `场景D - 魔术变量__current_time__在表达式中返回合理时间戳`() = runTest {
        val registry = MagicVariableRegistry()
        registry.register(object : MagicVariableProvider {
            override val name = "__current_time__"
            override val type = VariableType.INTEGER
            override fun getCurrentValue(): Any = System.currentTimeMillis()
        })

        val before = System.currentTimeMillis()
        val result = ExpressionHelper.evaluateExpression(
            "\${__current_time__}", variableRepo, registry
        )
        val after = System.currentTimeMillis()

        assertNotNull("表达式结果不应为 null", result)
        assertTrue("结果应为数值", result is Number)
        val ts = (result as Number).toLong()
        assertTrue("时间戳应在合理范围内 [$before, $after]", ts in before..after)
    }

    // ========== 场景 E: 激活并检查模式 ==========

    @Test
    fun `场景E - 激活模式后ModeConstraint验证通过`() = runTest {
        modeRepo.createMode(Mode("work_id", "工作模式"))

        // SetModeAction 激活
        val setMode = SetModeAction { modeRepo }
        val result = setMode.doAction(
            arrayOf("工作模式", SetModeAction.ACTION_ACTIVATE), mockRuntime
        )
        assertTrue("激活应成功", result.isSuccessful)

        // ModeConstraint 检查
        val constraint = ModeConstraint { modeRepo }
        assertTrue("工作模式应处于激活状态", constraint.check("工作模式", true))
    }

    // ========== 场景 F: 模式互斥 ==========

    @Test
    fun `场景F - 模式互斥 激活睡眠模式后工作模式应不活跃`() = runTest {
        modeRepo.createMode(Mode("work_id", "工作模式"))
        modeRepo.createMode(Mode("sleep_id", "睡眠模式"))

        val setMode = SetModeAction { modeRepo }
        // 先激活工作模式
        setMode.doAction(arrayOf("工作模式", SetModeAction.ACTION_ACTIVATE), mockRuntime)
        // 再激活睡眠模式 → 互斥：工作模式被去激活
        setMode.doAction(arrayOf("睡眠模式", SetModeAction.ACTION_ACTIVATE), mockRuntime)

        val constraint = ModeConstraint { modeRepo }
        assertFalse("工作模式应已被去激活", constraint.check("工作模式", true))
        assertTrue("睡眠模式应处于激活状态", constraint.check("睡眠模式", true))
    }

    // ========== 场景 G: SetVariable 触发变量变化事件 ==========

    @Test
    fun `场景G - SetVariable触发EVENT_ON_VARIABLE_CHANGED事件`() = runTest(UnconfinedTestDispatcher()) {
        val receivedEvents = mutableListOf<Event>()

        val dispatcher = VariableChangeEventDispatcher(variableRepo, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events -> receivedEvents.addAll(events) })
        dispatcher.onRegistered()
        advanceUntilIdle()

        // 通过 SetVariableAction 设置变量
        val setAction = SetVariableAction { variableRepo }
        setAction.doAction(
            arrayOf("x", VariableType.INTEGER.ordinal, "42", VariableScope.GLOBAL.ordinal, false),
            mockRuntime
        )
        advanceUntilIdle()

        assertTrue("应收到变量变化事件", receivedEvents.isNotEmpty())
        assertTrue(
            "事件类型应为 EVENT_ON_VARIABLE_CHANGED",
            receivedEvents.all { it.type == Event.EVENT_ON_VARIABLE_CHANGED }
        )

        dispatcher.destroy()
    }

    // ========== 场景 H: SetMode 触发模式变化事件 ==========

    @Test
    fun `场景H - SetMode触发EVENT_ON_MODE_CHANGED事件`() = runTest(UnconfinedTestDispatcher()) {
        val receivedEvents = mutableListOf<Event>()

        val dispatcher = ModeChangeEventDispatcher(modeRepo, backgroundScope)
        dispatcher.addCallback(EventDispatcher.Callback { events -> receivedEvents.addAll(events) })
        dispatcher.onRegistered()
        advanceUntilIdle()

        modeRepo.createMode(Mode("test_id", "testMode"))
        advanceUntilIdle()
        // 清除 Created 事件（dispatcher 不监听 Created，但清一下更安全）
        receivedEvents.clear()

        val setMode = SetModeAction { modeRepo }
        setMode.doAction(arrayOf("testMode", SetModeAction.ACTION_ACTIVATE), mockRuntime)
        advanceUntilIdle()

        assertTrue("应收到模式变化事件", receivedEvents.isNotEmpty())
        assertTrue(
            "事件类型应为 EVENT_ON_MODE_CHANGED",
            receivedEvents.all { it.type == Event.EVENT_ON_MODE_CHANGED }
        )

        dispatcher.destroy()
    }

    // ========== 场景 I: 变量持久化一致性 ==========

    @Test
    fun `场景I - 变量存取一致性 多个变量存入仓库后全部取出验证一致`() = runTest {
        val variables = listOf(
            Variable("id1", "boolVar", VariableType.BOOLEAN, true, VariableScope.GLOBAL),
            Variable("id2", "intVar", VariableType.INTEGER, 42L, VariableScope.GLOBAL),
            Variable("id3", "decVar", VariableType.DECIMAL, 3.14, VariableScope.LOCAL),
            Variable("id4", "strVar", VariableType.STRING, "hello", VariableScope.LOCAL)
        )

        // 存入
        for (v in variables) {
            variableRepo.setVariable(v)
        }

        // 全部取出并验证数量
        val all = variableRepo.getAllVariables()
        assertEquals("变量数量应一致", variables.size, all.size)

        // 逐一验证值、类型、作用域
        for (expected in variables) {
            val actual = variableRepo.getVariable(expected.name)
            assertNotNull("变量 ${expected.name} 应存在", actual)
            assertEquals("${expected.name} 类型一致", expected.type, actual!!.type)
            assertEquals("${expected.name} 值一致", expected.value, actual.value)
            assertEquals("${expected.name} 作用域一致", expected.scope, actual.scope)
        }

        // 按作用域查询
        val globals = variableRepo.getVariablesByScope(VariableScope.GLOBAL)
        assertEquals("GLOBAL 变量应有 2 个", 2, globals.size)
        val locals = variableRepo.getVariablesByScope(VariableScope.LOCAL)
        assertEquals("LOCAL 变量应有 2 个", 2, locals.size)
    }
}
