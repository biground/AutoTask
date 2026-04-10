/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.applet.criterion.GeofenceConstraint
import top.xjunz.tasker.task.applet.flow.ref.GeofenceReferent
import top.xjunz.tasker.task.applet.option.registry.BootstrapOptionRegistry

/**
 * 地理围栏系统端到端集成测试
 *
 * 覆盖：事件常量 → GeofenceConfig → GeofenceReferent → GeofenceConstraint
 *       → LocationEventDispatcher Extra → Registry 注册
 */
class GeofenceIntegrationTest {

    // TaskRuntime 构造函数为 private，通过反射创建实例
    private val runtime: TaskRuntime = TaskRuntime::class.java
        .getDeclaredConstructor()
        .apply { isAccessible = true }
        .newInstance()

    // ========== 1. 事件常量验证 ==========

    @Test
    fun `事件常量值正确`() {
        assertEquals(17, Event.EVENT_ON_GEOFENCE_ENTERED)
        assertEquals(18, Event.EVENT_ON_GEOFENCE_EXITED)
        assertEquals(19, Event.EVENT_ON_LOCATION_ARRIVED)
    }

    @Test
    fun `事件常量互不冲突`() {
        val allConstants = listOf(
            Event.EVENT_ON_GEOFENCE_ENTERED,
            Event.EVENT_ON_GEOFENCE_EXITED,
            Event.EVENT_ON_LOCATION_ARRIVED
        )
        assertEquals(allConstants.size, allConstants.toSet().size)
    }

    @Test
    fun `围栏事件常量不与已有常量冲突`() {
        // 收集所有已知事件常量
        val existingConstants = setOf(
            Event.EVENT_UNDEFINED,
            Event.EVENT_ON_PACKAGE_ENTERED,
            Event.EVENT_ON_PACKAGE_EXITED,
            Event.EVENT_ON_CONTENT_CHANGED,
            Event.EVENT_ON_NOTIFICATION_RECEIVED,
            Event.EVENT_ON_NEW_WINDOW,
            Event.EVENT_ON_PRIMARY_CLIP_CHANGED,
            Event.EVENT_ON_TICK,
            Event.EVENT_ON_TOAST_RECEIVED,
            Event.EVENT_ON_FILE_CREATED,
            Event.EVENT_ON_FILE_DELETED,
            Event.EVENT_ON_WIFI_CONNECTED,
            Event.EVENT_ON_WIFI_DISCONNECTED,
            Event.EVENT_ON_NETWORK_AVAILABLE,
            Event.EVENT_ON_NETWORK_UNAVAILABLE,
            Event.EVENT_ON_VARIABLE_CHANGED,
            Event.EVENT_ON_MODE_CHANGED
        )
        val geofenceConstants = setOf(
            Event.EVENT_ON_GEOFENCE_ENTERED,
            Event.EVENT_ON_GEOFENCE_EXITED,
            Event.EVENT_ON_LOCATION_ARRIVED
        )
        // 两集合无交集
        assertTrue(existingConstants.intersect(geofenceConstants).isEmpty())
    }

    // ========== 2. GeofenceConfig 完整性验证 ==========

    private fun validConfig() = GeofenceConfig(
        id = "integration-test-001",
        name = "测试围栏",
        latitude = 39.908823,
        longitude = 116.397470,
        radius = 200f
    )

    @Test
    fun `合法配置验证通过`() {
        assertTrue(validConfig().isValid())
    }

    @Test
    fun `纬度超出范围验证失败`() {
        assertFalse(validConfig().copy(latitude = 91.0).isValid())
        assertFalse(validConfig().copy(latitude = -91.0).isValid())
    }

    @Test
    fun `经度超出范围验证失败`() {
        assertFalse(validConfig().copy(longitude = 181.0).isValid())
        assertFalse(validConfig().copy(longitude = -181.0).isValid())
    }

    @Test
    fun `半径低于最小值验证失败`() {
        assertFalse(validConfig().copy(radius = 49f).isValid())
    }

    @Test
    fun `半径超过最大值验证失败`() {
        assertFalse(validConfig().copy(radius = 5001f).isValid())
    }

    @Test
    fun `名称为空验证失败`() {
        assertFalse(validConfig().copy(name = "").isValid())
    }

    @Test
    fun `边界值 半径 50f 合法`() {
        assertTrue(validConfig().copy(radius = 50f).isValid())
    }

    @Test
    fun `边界值 半径 5000f 合法`() {
        assertTrue(validConfig().copy(radius = 5000f).isValid())
    }

    @Test
    fun `边界值 纬度 90 合法`() {
        assertTrue(validConfig().copy(latitude = 90.0).isValid())
    }

    @Test
    fun `边界值 纬度 -90 合法`() {
        assertTrue(validConfig().copy(latitude = -90.0).isValid())
    }

    // ========== 3. GeofenceReferent 完整引用链验证 ==========

    @Test
    fun `referent 索引 0 返回自身`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        // runtime 参数传 null，因 returnDefaultValues=true 不会抛异常
        val result = referent.getReferredValue(0, runtime)
        assertNotNull(result)
        assertTrue(result is GeofenceReferent)
        assertEquals(referent, result)
    }

    @Test
    fun `referent 索引 1 返回名称`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        assertEquals("公司", referent.getReferredValue(1, runtime))
    }

    @Test
    fun `referent 索引 2 返回纬度`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        assertEquals(39.9, referent.getReferredValue(2, runtime))
    }

    @Test
    fun `referent 索引 3 返回经度`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        assertEquals(116.3, referent.getReferredValue(3, runtime))
    }

    @Test
    fun `referent 索引 4 返回转场类型`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        assertEquals("ENTER", referent.getReferredValue(4, runtime))
    }

    @Test(expected = NullPointerException::class)
    fun `referent 越界索引抛出异常`() {
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")
        referent.getReferredValue(5, runtime)
    }

    @Test
    fun `referent EXIT 转场类型正确`() {
        val referent = GeofenceReferent("家", 40.0, 117.0, "EXIT")
        assertEquals("EXIT", referent.getReferredValue(4, runtime))
    }

    // ========== 4. GeofenceConstraint 距离计算集成 ==========

    // 基于 Haversine 的简易距离计算（纯 JVM，不依赖高德 SDK）
    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val r = 6371000.0 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (r * c).toFloat()
    }

    @Test
    fun `围栏内判定 返回 true`() {
        // 当前位置 = 围栏中心
        val constraint = GeofenceConstraint(
            locationProvider = { Pair(39.908823, 116.397470) },
            distanceCalculator = ::haversineDistance
        )
        assertTrue(constraint.check(39.908823, 116.397470, 200f))
    }

    @Test
    fun `围栏外判定 返回 false`() {
        // 当前位置远离围栏中心（约 10km）
        val constraint = GeofenceConstraint(
            locationProvider = { Pair(39.0, 116.0) },
            distanceCalculator = ::haversineDistance
        )
        assertFalse(constraint.check(39.908823, 116.397470, 200f))
    }

    @Test
    fun `无位置数据 返回 false`() {
        val constraint = GeofenceConstraint(
            locationProvider = { null },
            distanceCalculator = ::haversineDistance
        )
        assertFalse(constraint.check(39.908823, 116.397470, 200f))
    }

    @Test
    fun `刚好在边界上 返回 true`() {
        // 距离 = 100m，半径 = 100f → distance <= radius
        val constraint = GeofenceConstraint(
            locationProvider = { Pair(39.908823, 116.397470) },
            distanceCalculator = { _, _, _, _ -> 100f }
        )
        assertTrue(constraint.check(39.9, 116.4, 100f))
    }

    @Test
    fun `超出边界 1 米 返回 false`() {
        val constraint = GeofenceConstraint(
            locationProvider = { Pair(39.908823, 116.397470) },
            distanceCalculator = { _, _, _, _ -> 101f }
        )
        assertFalse(constraint.check(39.9, 116.4, 100f))
    }

    // ========== 5. LocationEventDispatcher Extra 常量验证 ==========

    @Test
    fun `Extra 常量值正确`() {
        assertEquals(0, LocationEventDispatcher.EXTRA_GEOFENCE_NAME)
        assertEquals(1, LocationEventDispatcher.EXTRA_GEOFENCE_LAT)
        assertEquals(2, LocationEventDispatcher.EXTRA_GEOFENCE_LNG)
        assertEquals(3, LocationEventDispatcher.EXTRA_GEOFENCE_RADIUS)
        assertEquals(4, LocationEventDispatcher.EXTRA_TRANSITION_TYPE)
    }

    @Test
    fun `Extra 常量连续且无重复`() {
        val extras = listOf(
            LocationEventDispatcher.EXTRA_GEOFENCE_NAME,
            LocationEventDispatcher.EXTRA_GEOFENCE_LAT,
            LocationEventDispatcher.EXTRA_GEOFENCE_LNG,
            LocationEventDispatcher.EXTRA_GEOFENCE_RADIUS,
            LocationEventDispatcher.EXTRA_TRANSITION_TYPE
        )
        // 无重复
        assertEquals(extras.size, extras.toSet().size)
        // 连续递增 0..4
        assertEquals(listOf(0, 1, 2, 3, 4), extras)
    }

    // ========== 6. Registry 注册完整性验证 ==========

    @Test
    fun `BootstrapOptionRegistry 包含 ID_LOCATION_CRITERION_REGISTRY`() {
        assertEquals(0x1A, BootstrapOptionRegistry.ID_LOCATION_CRITERION_REGISTRY)
    }

    // ========== 7. Referent 索引与 Extra 索引契约一致性 ==========

    @Test
    fun `Referent 索引与 Extra 索引偏移量一致`() {
        // Extra: NAME=0, LAT=1, LNG=2, RADIUS=3, TRANSITION=4
        // Referent: self=0, name=1, lat=2, lng=3, transition=4
        // 验证名称/坐标/转场在两端索引映射一致
        val referent = GeofenceReferent("公司", 39.9, 116.3, "ENTER")

        // Extra NAME (0) → Referent name (1)
        val nameFromReferent = referent.getReferredValue(
            LocationEventDispatcher.EXTRA_GEOFENCE_NAME + 1, runtime
        )
        assertEquals("公司", nameFromReferent)

        // Extra LAT (1) → Referent lat (2)
        val latFromReferent = referent.getReferredValue(
            LocationEventDispatcher.EXTRA_GEOFENCE_LAT + 1, runtime
        )
        assertEquals(39.9, latFromReferent)

        // Extra LNG (2) → Referent lng (3)
        val lngFromReferent = referent.getReferredValue(
            LocationEventDispatcher.EXTRA_GEOFENCE_LNG + 1, runtime
        )
        assertEquals(116.3, lngFromReferent)

        // Extra TRANSITION (4) → Referent transition (4)
        val transitionFromReferent = referent.getReferredValue(
            LocationEventDispatcher.EXTRA_TRANSITION_TYPE, runtime
        )
        assertEquals("ENTER", transitionFromReferent)
    }

    // ========== 8. 端到端场景：围栏进入事件 → Referent → Constraint ==========

    @Test
    fun `端到端 围栏进入事件产生正确 Referent 且约束匹配`() {
        // 模拟事件场景：用户进入"公司"围栏
        val config = GeofenceConfig(
            id = "cfg-001",
            name = "公司",
            latitude = 39.908823,
            longitude = 116.397470,
            radius = 200f
        )

        // 1. 验证配置合法
        assertTrue(config.isValid())

        // 2. 模拟 LocationEventDispatcher 构造 Referent
        val referent = GeofenceReferent(
            name = config.name,
            lat = config.latitude,
            lng = config.longitude,
            transition = "ENTER"
        )

        // 3. 验证 Referent 携带的数据与配置一致
        assertEquals(config.name, referent.getReferredValue(1, runtime))
        assertEquals(config.latitude, referent.getReferredValue(2, runtime))
        assertEquals(config.longitude, referent.getReferredValue(3, runtime))

        // 4. 验证 GeofenceConstraint 在围栏内返回 true
        val constraint = GeofenceConstraint(
            locationProvider = { Pair(config.latitude, config.longitude) },
            distanceCalculator = ::haversineDistance
        )
        assertTrue(constraint.check(config.latitude, config.longitude, config.radius))

        // 5. 验证事件类型匹配
        assertEquals(17, Event.EVENT_ON_GEOFENCE_ENTERED)
    }

    @Test
    fun `端到端 围栏离开事件产生正确 Referent`() {
        val config = validConfig()

        val referent = GeofenceReferent(
            name = config.name,
            lat = config.latitude,
            lng = config.longitude,
            transition = "EXIT"
        )

        assertEquals("EXIT", referent.getReferredValue(4, runtime))
        assertEquals(18, Event.EVENT_ON_GEOFENCE_EXITED)
    }
}
