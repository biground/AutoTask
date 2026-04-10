/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.GeofenceConstraint

/**
 * 位置条件注册表，注册地理围栏相关约束选项。
 */
class LocationCriterionRegistry(
    id: Int,
    private val locationProvider: () -> Pair<Double, Double>?
) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val geofenceInside = invertibleAppletOption(R.string.geofence_inside) {
        GeofenceConstraint.withAMap(locationProvider)
    }.withValueArgument<Double>(R.string.geofence_latitude)
        .withValueArgument<Double>(R.string.geofence_longitude)
        .withValueArgument<Float>(R.string.geofence_radius)
        .withValueArgument<String>(R.string.geofence_display_name)
}
