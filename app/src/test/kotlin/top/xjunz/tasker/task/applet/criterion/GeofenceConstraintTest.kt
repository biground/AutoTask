/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GeofenceConstraint 单元测试
 *
 * 三种场景：在围栏内 → SUCCESS，在围栏外 → FAILURE，无位置 → FAILURE
 */
class GeofenceConstraintTest {

    // 天安门坐标（围栏中心）
    private val centerLat = 39.908823
    private val centerLng = 116.397470
    private val radius = 500f // 米

    // 距离天安门约 200m（围栏内）
    private val nearbyLat = 39.910500
    private val nearbyLng = 116.397470

    // 距离天安门约 5km（围栏外）
    private val farLat = 39.960000
    private val farLng = 116.397470

    /**
     * Haversine 公式计算两点间距离（米），替代测试环境不可用的 AMapUtils
     */
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

    /**
     * 创建约束实例，注入位置提供器和距离计算器
     */
    private fun createConstraint(
        currentLat: Double? = null,
        currentLng: Double? = null
    ): GeofenceConstraint {
        val locationProvider: () -> Pair<Double, Double>? = {
            if (currentLat != null && currentLng != null) {
                Pair(currentLat, currentLng)
            } else {
                null
            }
        }
        val distanceCalculator: (Double, Double, Double, Double) -> Float = { lat1, lng1, lat2, lng2 ->
            haversineDistance(lat1, lng1, lat2, lng2)
        }
        return GeofenceConstraint(locationProvider, distanceCalculator)
    }

    // ========== 在围栏内 → true ==========

    @Test
    fun `在围栏内返回 true`() = runBlocking {
        val constraint = createConstraint(nearbyLat, nearbyLng)
        val result = constraint.check(centerLat, centerLng, radius)
        assertTrue("距离约200m应在500m围栏内", result)
    }

    // ========== 在围栏外 → false ==========

    @Test
    fun `在围栏外返回 false`() = runBlocking {
        val constraint = createConstraint(farLat, farLng)
        val result = constraint.check(centerLat, centerLng, radius)
        assertFalse("距离约5km应在500m围栏外", result)
    }

    // ========== 无位置数据 → false ==========

    @Test
    fun `无位置数据返回 false`() = runBlocking {
        val constraint = createConstraint(null, null)
        val result = constraint.check(centerLat, centerLng, radius)
        assertFalse("无位置数据应返回 false", result)
    }

    // ========== 边界：恰好在围栏边界上 → true ==========

    @Test
    fun `恰好在围栏半径边界上返回 true`() = runBlocking {
        val locationProvider: () -> Pair<Double, Double>? = { Pair(39.91, 116.40) }
        // 直接返回刚好等于 radius 的距离
        val distanceCalculator: (Double, Double, Double, Double) -> Float = { _, _, _, _ -> radius }
        val constraint = GeofenceConstraint(locationProvider, distanceCalculator)
        val result = constraint.check(centerLat, centerLng, radius)
        assertTrue("恰好在边界上（距离=半径）应返回 true", result)
    }

    // ========== 取反支持验证 ==========

    @Test
    fun `isInvertible 默认为 true`() {
        val constraint = createConstraint()
        assertTrue(constraint.isInvertible)
    }
}
