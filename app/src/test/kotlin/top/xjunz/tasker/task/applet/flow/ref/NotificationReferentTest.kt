/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow.ref

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import top.xjunz.tasker.engine.runtime.TaskRuntime

class NotificationReferentTest {

    private lateinit var componentInfo: ComponentInfoWrapper
    private lateinit var runtime: TaskRuntime

    @Before
    fun setUp() {
        componentInfo = ComponentInfoWrapper(
            packageName = "com.example.test",
            activityName = "MainActivity",
            paneTitle = "TestPane"
        )
        // TaskRuntime 的构造函数为 private，通过反射创建实例（测试中不会使用其内部状态）
        runtime = TaskRuntime::class.java.getDeclaredConstructor().apply {
            isAccessible = true
        }.newInstance()
    }

    // ---- which=0~2 向后兼容 ----

    @Test
    fun `which=0 返回 referent 本身`() {
        val referent = NotificationReferent(componentInfo)
        val result = referent.getReferredValue(0, runtime)
        assertEquals(referent, result)
    }

    @Test
    fun `which=1 返回 componentInfo paneTitle`() {
        val referent = NotificationReferent(componentInfo)
        val result = referent.getReferredValue(1, runtime)
        assertEquals("TestPane", result)
    }

    @Test
    fun `which=2 返回 componentInfo 对象`() {
        val referent = NotificationReferent(componentInfo)
        val result = referent.getReferredValue(2, runtime)
        assertEquals(componentInfo, result)
    }

    // which=3 (componentInfo.label) 依赖 PackageManagerBridge，无法在 JVM 单元测试中运行，跳过

    // ---- which=4~7 新增通知详情字段 ----

    @Test
    fun `which=4 返回 title`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "通知标题",
            text = "通知内容",
            subText = "副标题",
            postTime = 1700000000000L
        )
        assertEquals("通知标题", referent.getReferredValue(4, runtime))
    }

    @Test
    fun `which=5 返回 text`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "通知标题",
            text = "通知内容",
            subText = "副标题",
            postTime = 1700000000000L
        )
        assertEquals("通知内容", referent.getReferredValue(5, runtime))
    }

    @Test
    fun `which=6 返回 subText`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "通知标题",
            text = "通知内容",
            subText = "副标题",
            postTime = 1700000000000L
        )
        assertEquals("副标题", referent.getReferredValue(6, runtime))
    }

    @Test
    fun `which=7 返回 postTime`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "通知标题",
            text = "通知内容",
            subText = "副标题",
            postTime = 1700000000000L
        )
        assertEquals(1700000000000L, referent.getReferredValue(7, runtime))
    }

    // ---- 兼容模式：仅传 componentInfo，新字段使用默认值 ----

    @Test
    fun `默认参数构造时 which=4 返回 null`() {
        val referent = NotificationReferent(componentInfo)
        assertNull(referent.getReferredValue(4, runtime))
    }

    @Test
    fun `默认参数构造时 which=5 返回 null`() {
        val referent = NotificationReferent(componentInfo)
        assertNull(referent.getReferredValue(5, runtime))
    }

    @Test
    fun `默认参数构造时 which=6 返回 null`() {
        val referent = NotificationReferent(componentInfo)
        assertNull(referent.getReferredValue(6, runtime))
    }

    @Test
    fun `默认参数构造时 which=7 返回 0L`() {
        val referent = NotificationReferent(componentInfo)
        assertEquals(0L, referent.getReferredValue(7, runtime))
    }

    // ---- toString 脱敏 ----

    @Test
    fun `toString 对 title 和 text 截断到前10字符`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "这是一个非常非常长的通知标题内容",
            text = "这是一段非常非常长的通知正文内容详情",
            subText = "副标题",
            postTime = 1700000000000L
        )
        val str = referent.toString()
        // title 截断到前10字符: "这是一个非常非常长的"
        assertTrue("title 应被截断", str.contains("这是一个非常非常长的..."))
        // text 截断到前10字符: "这是一段非常非常长的"
        assertTrue("text 应被截断", str.contains("这是一段非常非常长的..."))
        // 不应包含完整内容
        assertTrue("不应包含完整 title", !str.contains("这是一个非常非常长的通知标题内容"))
    }

    @Test
    fun `toString 对短 title 和 text 不截断`() {
        val referent = NotificationReferent(
            componentInfo,
            title = "短标题",
            text = "短内容",
            subText = "副",
            postTime = 0L
        )
        val str = referent.toString()
        assertTrue(str.contains("短标题"))
        assertTrue(str.contains("短内容"))
    }

    @Test
    fun `toString 对 null title 和 text 正常显示`() {
        val referent = NotificationReferent(componentInfo)
        val str = referent.toString()
        assertTrue(str.contains("title=null"))
        assertTrue(str.contains("text=null"))
    }
}
