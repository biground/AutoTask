package top.xjunz.tasker.task.applet.flow.ref

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * GeofenceReferent 单元测试
 */
class GeofenceReferentTest {

    // TaskRuntime 构造函数为 private，通过反射创建实例
    private val runtime: TaskRuntime = TaskRuntime::class.java
        .getDeclaredConstructor()
        .apply { isAccessible = true }
        .newInstance()

    private val referent = GeofenceReferent(
        name = "公司",
        lat = 39.908823,
        lng = 116.397470,
        transition = "ENTER"
    )

    // --- 索引 0: 引用本身 ---
    @Test
    fun `索引0返回引用本身`() {
        val result = referent.getReferredValue(0, runtime)
        assertTrue(result is GeofenceReferent)
        assertEquals(referent, result)
    }

    // --- 索引 1: 围栏名称 ---
    @Test
    fun `索引1返回围栏名称`() {
        val result = referent.getReferredValue(1, runtime)
        assertEquals("公司", result)
    }

    // --- 索引 2: 纬度 ---
    @Test
    fun `索引2返回纬度`() {
        val result = referent.getReferredValue(2, runtime)
        assertEquals(39.908823, result)
    }

    // --- 索引 3: 经度 ---
    @Test
    fun `索引3返回经度`() {
        val result = referent.getReferredValue(3, runtime)
        assertEquals(116.397470, result)
    }

    // --- 索引 4: 转场类型 ---
    @Test
    fun `索引4返回转场类型ENTER`() {
        val result = referent.getReferredValue(4, runtime)
        assertEquals("ENTER", result)
    }

    @Test
    fun `索引4返回转场类型EXIT`() {
        val exitReferent = GeofenceReferent(
            name = "家",
            lat = 31.230416,
            lng = 121.473701,
            transition = "EXIT"
        )
        val result = exitReferent.getReferredValue(4, runtime)
        assertEquals("EXIT", result)
    }

    // --- 越界索引 ---
    @Test
    fun `越界索引抛出NullPointerException`() {
        assertThrows(NullPointerException::class.java) {
            referent.getReferredValue(5, runtime)
        }
    }

    @Test
    fun `负数索引抛出NullPointerException`() {
        assertThrows(NullPointerException::class.java) {
            referent.getReferredValue(-1, runtime)
        }
    }
}
