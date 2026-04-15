package top.xjunz.tasker.task.applet.action

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SetAlarmAction 参数校验单元测试。
 * 由于 apply() 依赖 Android Context，仅测试纯逻辑的参数校验。
 */
class SetAlarmActionTest {

    @Test
    fun `有效时间参数校验通过`() {
        assertTrue(SetAlarmAction.validateParams(0, 0))
        assertTrue(SetAlarmAction.validateParams(23, 59))
        assertTrue(SetAlarmAction.validateParams(12, 30))
    }

    @Test
    fun `小时超范围校验失败`() {
        assertFalse(SetAlarmAction.validateParams(-1, 30))
        assertFalse(SetAlarmAction.validateParams(24, 30))
        assertFalse(SetAlarmAction.validateParams(100, 0))
    }

    @Test
    fun `分钟超范围校验失败`() {
        assertFalse(SetAlarmAction.validateParams(12, -1))
        assertFalse(SetAlarmAction.validateParams(12, 60))
        assertFalse(SetAlarmAction.validateParams(0, 100))
    }

    @Test
    fun `边界值校验`() {
        assertTrue(SetAlarmAction.validateParams(0, 0))
        assertTrue(SetAlarmAction.validateParams(23, 59))
        assertFalse(SetAlarmAction.validateParams(24, 0))
        assertFalse(SetAlarmAction.validateParams(0, 60))
    }
}
