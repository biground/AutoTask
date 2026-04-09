package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.mode.InMemoryModeRepository
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * SetModeAction 单元测试
 */
class SetModeActionTest {

    private lateinit var repo: InMemoryModeRepository
    private lateinit var action: SetModeAction
    private lateinit var runtime: TaskRuntime

    @Before
    fun setUp() {
        repo = InMemoryModeRepository()
        action = SetModeAction { repo }
        // 通过反射创建 TaskRuntime（私有构造器），doAction 不依赖 runtime 状态
        runtime = TaskRuntime::class.java.getDeclaredConstructor()
            .apply { isAccessible = true }
            .newInstance()
    }

    // ========== activate ==========

    @Test
    fun `activate 模式成功`() = runTest {
        repo.createMode(Mode(id = "m1", name = "夜间模式"))
        val result = action.doAction(arrayOf("夜间模式", "activate"), runtime)
        assertTrue(result.isSuccessful)
        // 验证模式已激活
        val mode = repo.getModeByName("夜间模式")!!
        assertTrue(mode.isActive)
    }

    // ========== deactivate ==========

    @Test
    fun `deactivate 模式成功`() = runTest {
        repo.createMode(Mode(id = "m1", name = "勿扰模式"))
        repo.setActiveMode("m1")
        assertTrue(repo.getModeByName("勿扰模式")!!.isActive)

        val result = action.doAction(arrayOf("勿扰模式", "deactivate"), runtime)
        assertTrue(result.isSuccessful)
        assertFalse(repo.getModeByName("勿扰模式")!!.isActive)
    }

    // ========== toggle ==========

    @Test
    fun `toggle 模式 - 从停用到激活`() = runTest {
        repo.createMode(Mode(id = "m1", name = "静音模式"))
        assertFalse(repo.getModeByName("静音模式")!!.isActive)

        val result = action.doAction(arrayOf("静音模式", "toggle"), runtime)
        assertTrue(result.isSuccessful)
        assertTrue(repo.getModeByName("静音模式")!!.isActive)
    }

    @Test
    fun `toggle 模式 - 从激活到停用`() = runTest {
        repo.createMode(Mode(id = "m1", name = "静音模式"))
        repo.setActiveMode("m1")
        assertTrue(repo.getModeByName("静音模式")!!.isActive)

        val result = action.doAction(arrayOf("静音模式", "toggle"), runtime)
        assertTrue(result.isSuccessful)
        assertFalse(repo.getModeByName("静音模式")!!.isActive)
    }

    // ========== 异常场景 ==========

    @Test
    fun `操作不存在的模式返回失败`() = runTest {
        val result = action.doAction(arrayOf("不存在模式", "activate"), runtime)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `空模式名返回失败`() = runTest {
        val result = action.doAction(arrayOf("", "activate"), runtime)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `空白模式名返回失败`() = runTest {
        val result = action.doAction(arrayOf("   ", "toggle"), runtime)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `无效操作类型返回失败`() = runTest {
        repo.createMode(Mode(id = "m1", name = "测试模式"))
        val result = action.doAction(arrayOf("测试模式", "invalid_op"), runtime)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `参数为 null 时返回失败`() = runTest {
        val result = action.doAction(arrayOf(null, "activate"), runtime)
        assertFalse(result.isSuccessful)
    }
}
