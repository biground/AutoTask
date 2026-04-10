package top.xjunz.tasker.task.location

import kotlinx.serialization.Serializable

/**
 * 地理围栏转场类型
 */
enum class GeofenceTransition {
    ENTER,  // 进入围栏
    EXIT    // 离开围栏
}

/**
 * 地理围栏配置数据类
 * 坐标系：GCJ-02（高德地图加密坐标系）
 */
@Serializable
data class GeofenceConfig(
    val id: String,              // UUID
    val name: String,            // 用户自定义围栏名称
    val latitude: Double,        // 纬度（-90 ~ 90）
    val longitude: Double,       // 经度（-180 ~ 180）
    val radius: Float,           // 半径（米），范围 50 ~ 5000
    val enabled: Boolean = true,
    val address: String? = null  // 可选地址描述
)

/**
 * 验证围栏配置合法性
 */
fun GeofenceConfig.isValid(): Boolean {
    return name.isNotBlank()
        && latitude in -90.0..90.0
        && longitude in -180.0..180.0
        && radius in 50f..5000f
}
