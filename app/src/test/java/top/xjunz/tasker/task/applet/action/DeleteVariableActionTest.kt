package top.xjunz.tasker.task.applet.action

import org.junit.Test
import top.xjunz.tasker.engine.variable.InMemoryVariableRepository

/**
 * DeleteVariableAction 编译验证测试
 * 完整集成测试依赖 TaskRuntime mock，此处仅验证类可实例化
 */
class DeleteVariableActionTest {

    @Test
    fun `DeleteVariableAction 可正常实例化`() {
        val repo = InMemoryVariableRepository()
        val action = DeleteVariableAction { repo }
        // 验证实例化成功，不抛异常
        assert(action != null)
    }

    @Test
    fun `SetVariableAction 可正常实例化`() {
        val repo = InMemoryVariableRepository()
        val action = SetVariableAction { repo }
        assert(action != null)
    }
}
