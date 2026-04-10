/*
 * Copyright (c) 2026 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceListener
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.DPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.task.EventDispatcher

/**
 * 地理围栏事件分发器，封装高德 GeoFenceClient，监听围栏进出事件并分发引擎 Event。
 *
 * @author AutoPilot 2026/04/10
 */
class LocationEventDispatcher(
    private val apiKeyManager: AmapApiKeyManager,
    private val configRepository: GeofenceConfigRepository,
    private val scope: CoroutineScope
) : EventDispatcher() {

    companion object {
        private const val TAG = "LocationEventDispatcher"

        /** 围栏广播 Action */
        private const val GEOFENCE_BROADCAST_ACTION =
            "top.xjunz.tasker.task.location.GEOFENCE_BROADCAST"

        /** Extra 索引：围栏名称 */
        const val EXTRA_GEOFENCE_NAME = 0
        /** Extra 索引：围栏纬度 */
        const val EXTRA_GEOFENCE_LAT = 1
        /** Extra 索引：围栏经度 */
        const val EXTRA_GEOFENCE_LNG = 2
        /** Extra 索引：围栏半径 */
        const val EXTRA_GEOFENCE_RADIUS = 3
        /** Extra 索引：转场类型 (STATUS_IN / STATUS_OUT) */
        const val EXTRA_TRANSITION_TYPE = 4
    }

    private var geoFenceClient: GeoFenceClient? = null
    private var receiverRegistered = false

    /** 围栏 customId → GeofenceConfig 映射，用于事件发生时还原配置信息 */
    private val configMap = mutableMapOf<String, GeofenceConfig>()

    /**
     * 围栏创建回调：记录日志
     */
    private val geoFenceListener = GeoFenceListener { _, errorCode, _ ->
        if (errorCode != GeoFence.ADDGEOFENCE_SUCCESS) {
            Log.w(TAG, "围栏创建失败, errorCode=$errorCode")
        }
    }

    /**
     * 围栏事件广播接收器
     */
    private val geofenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != GEOFENCE_BROADCAST_ACTION) return
            val bundle = intent.extras ?: return

            val customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID) ?: return
            val status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)
            val config = configMap[customId] ?: return

            handleGeofenceEvent(status, config)
        }
    }

    override fun onRegistered() {
        scope.launch {
            // 1. 检查 API Key
            val apiKey = apiKeyManager.getApiKey()
            if (apiKey.isNullOrBlank()) {
                Log.w(TAG, "高德 API Key 未设置，跳过围栏初始化")
                return@launch
            }

            // 2. 设置 API Key
            AMapLocationClient.setApiKey(apiKey)

            val context = ContextBridge.getContext()

            // 3. 初始化 GeoFenceClient
            val client = GeoFenceClient(context)
            client.setActivateAction(
                GeoFenceClient.GEOFENCE_IN or GeoFenceClient.GEOFENCE_OUT
            )
            client.createPendingIntent(GEOFENCE_BROADCAST_ACTION)
            client.setGeoFenceListener(geoFenceListener)
            geoFenceClient = client

            // 4. 注册广播接收器
            val filter = IntentFilter(GEOFENCE_BROADCAST_ACTION)
            context.registerReceiver(geofenceReceiver, filter)
            receiverRegistered = true

            // 5. 加载已保存围栏并注册到 SDK
            val configs = configRepository.getAllGeofences()
            for (config in configs) {
                if (!config.enabled) continue
                configMap[config.id] = config
                client.addGeoFence(
                    DPoint(config.latitude, config.longitude),
                    config.radius,
                    config.id  // customId
                )
            }
        }
    }

    /**
     * 处理围栏事件，根据 status 分发 Event
     */
    private fun handleGeofenceEvent(status: Int, config: GeofenceConfig) {
        val eventType = when (status) {
            GeoFence.STATUS_IN -> Event.EVENT_ON_GEOFENCE_ENTERED
            GeoFence.STATUS_OUT -> Event.EVENT_ON_GEOFENCE_EXITED
            else -> return
        }

        val event = Event.obtain(eventType).apply {
            putExtra(EXTRA_GEOFENCE_NAME, config.name)
            putExtra(EXTRA_GEOFENCE_LAT, config.latitude)
            putExtra(EXTRA_GEOFENCE_LNG, config.longitude)
            putExtra(EXTRA_GEOFENCE_RADIUS, config.radius)
            putExtra(EXTRA_TRANSITION_TYPE, status)
        }
        dispatchEvents(event)
    }

    override fun destroy() {
        scope.cancel()
        geoFenceClient?.removeGeoFence()
        geoFenceClient = null
        configMap.clear()
        if (receiverRegistered) {
            try {
                ContextBridge.getContext().unregisterReceiver(geofenceReceiver)
            } catch (e: Exception) {
                Log.w(TAG, "注销广播接收器失败", e)
            }
            receiverRegistered = false
        }
    }
}
