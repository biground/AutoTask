/*
 * Copyright (c) 2024. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 地理围栏约束 (C-LOC-001)
 *
 * 判断当前位置是否在指定围栏内。
 *
 * 参数索引：
 * - values[0]: 围栏中心纬度 (Double)
 * - values[1]: 围栏中心经度 (Double)
 * - values[2]: 围栏半径（米）(Float)
 *
 * @param locationProvider 提供当前位置（纬度, 经度），无位置时返回 null
 * @param distanceCalculator 计算两点间距离（米）
 */
class GeofenceConstraint(
    private val locationProvider: () -> Pair<Double, Double>?,
    private val distanceCalculator: (Double, Double, Double, Double) -> Float
) : Applet() {

    companion object {
        /**
         * 使用高德 SDK 计算距离的工厂方法
         */
        fun withAMap(locationProvider: () -> Pair<Double, Double>?): GeofenceConstraint {
            return GeofenceConstraint(locationProvider) { lat1, lng1, lat2, lng2 ->
                AMapUtils.calculateLineDistance(
                    LatLng(lat1, lng1),
                    LatLng(lat2, lng2)
                )
            }
        }
    }

    /**
     * 直接检查当前位置是否在围栏内。
     * 供单元测试和外部调用使用。
     *
     * @return true 在围栏内，false 在围栏外或无位置数据
     */
    fun check(targetLat: Double, targetLng: Double, targetRadius: Float): Boolean {
        val currentLocation = locationProvider() ?: return false
        val (currentLat, currentLng) = currentLocation
        val distance = distanceCalculator(currentLat, currentLng, targetLat, targetLng)
        return distance <= targetRadius
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val targetLat = (values[0] as? Number)?.toDouble() ?: return AppletResult.EMPTY_FAILURE
        val targetLng = (values[1] as? Number)?.toDouble() ?: return AppletResult.EMPTY_FAILURE
        val targetRadius = (values[2] as? Number)?.toFloat() ?: return AppletResult.EMPTY_FAILURE

        val matched = check(targetLat, targetLng, targetRadius)

        return if (isInverted) {
            AppletResult.emptyResult(!matched)
        } else {
            AppletResult.emptyResult(matched)
        }
    }
}
