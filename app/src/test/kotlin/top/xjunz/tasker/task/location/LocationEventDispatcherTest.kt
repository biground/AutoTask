/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.location

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * LocationEventDispatcher 单元测试
 * 因高德 SDK 类在纯 JUnit 环境不可用，仅验证 companion object 常量值。
 */
class LocationEventDispatcherTest {

    @Test
    fun `EXTRA_GEOFENCE_NAME 索引为 0`() {
        assertEquals(0, LocationEventDispatcher.EXTRA_GEOFENCE_NAME)
    }

    @Test
    fun `EXTRA_GEOFENCE_LAT 索引为 1`() {
        assertEquals(1, LocationEventDispatcher.EXTRA_GEOFENCE_LAT)
    }

    @Test
    fun `EXTRA_GEOFENCE_LNG 索引为 2`() {
        assertEquals(2, LocationEventDispatcher.EXTRA_GEOFENCE_LNG)
    }

    @Test
    fun `EXTRA_GEOFENCE_RADIUS 索引为 3`() {
        assertEquals(3, LocationEventDispatcher.EXTRA_GEOFENCE_RADIUS)
    }

    @Test
    fun `EXTRA_TRANSITION_TYPE 索引为 4`() {
        assertEquals(4, LocationEventDispatcher.EXTRA_TRANSITION_TYPE)
    }

    @Test
    fun `Extra 常量值互不重复`() {
        val values = setOf(
            LocationEventDispatcher.EXTRA_GEOFENCE_NAME,
            LocationEventDispatcher.EXTRA_GEOFENCE_LAT,
            LocationEventDispatcher.EXTRA_GEOFENCE_LNG,
            LocationEventDispatcher.EXTRA_GEOFENCE_RADIUS,
            LocationEventDispatcher.EXTRA_TRANSITION_TYPE
        )
        assertEquals(5, values.size)
    }
}
