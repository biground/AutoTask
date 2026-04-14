/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.engine.runtime

import android.util.SparseArray
import androidx.core.util.forEach
import top.xjunz.shared.ktx.casted

/**
 * @author xjunz 2022/10/30
 */
class Event private constructor(
    private var _type: Int,
    pkgName: String? = null,
    actName: String? = null,
    paneTitle: String? = null
) {

    val type get() = _type

    val componentInfo = ComponentInfo().also {
        it.packageName = pkgName
        it.activityName = actName
        it.paneTitle = paneTitle
    }

    private val extras by lazy { SparseArray<Any>() }

    companion object {
        /**
         * The undefined event. Does not match any event even itself.
         */
        const val EVENT_UNDEFINED = -1

        /**
         * The event when a new package is entered. Always be accompanied with [EVENT_ON_PACKAGE_EXITED].
         */
        const val EVENT_ON_PACKAGE_ENTERED = 1

        /**
         * The event when the current package is left. Always be accompanied with [EVENT_ON_PACKAGE_ENTERED].
         */
        const val EVENT_ON_PACKAGE_EXITED = 2

        /**
         * The event when any content is changed in current window.
         */
        const val EVENT_ON_CONTENT_CHANGED = 3

        /**
         * When a status bar notification is received.
         */
        const val EVENT_ON_NOTIFICATION_RECEIVED = 4

        const val EVENT_ON_NEW_WINDOW = 5

        const val EVENT_ON_PRIMARY_CLIP_CHANGED = 6

        const val EVENT_ON_TICK = 7

        const val EVENT_ON_TOAST_RECEIVED = 8

        const val EVENT_ON_FILE_CREATED = 9

        const val EVENT_ON_FILE_DELETED = 10

        const val EVENT_ON_WIFI_CONNECTED = 11

        const val EVENT_ON_WIFI_DISCONNECTED = 12

        const val EVENT_ON_NETWORK_AVAILABLE = 13

        const val EVENT_ON_NETWORK_UNAVAILABLE = 14

        const val EVENT_ON_VARIABLE_CHANGED = 15

        const val EVENT_ON_MODE_CHANGED = 16

        const val EVENT_ON_GEOFENCE_ENTERED = 17

        const val EVENT_ON_GEOFENCE_EXITED = 18

        const val EVENT_ON_LOCATION_ARRIVED = 19

        const val EVENT_ON_ALARM_FIRED = 20
        const val EVENT_ON_DEVICE_BOOTED = 21
        const val EVENT_ON_SCREEN_ON = 22
        const val EVENT_ON_SCREEN_OFF = 23
        const val EVENT_ON_SCREEN_UNLOCKED = 24
        const val EVENT_ON_BT_STATE_CHANGED = 25
        const val EVENT_ON_BT_DEVICE_CONNECTED = 26
        const val EVENT_ON_BT_DEVICE_DISCONNECTED = 27
        const val EVENT_ON_CALL_STATE_CHANGED = 28
        const val EVENT_ON_SMS_RECEIVED = 29
        const val EVENT_ON_POWER_CONNECTED = 30
        const val EVENT_ON_POWER_DISCONNECTED = 31
        const val EVENT_ON_HEADSET_PLUGGED = 32
        const val EVENT_ON_HEADSET_UNPLUGGED = 33
        const val EVENT_ON_INTENT_RECEIVED = 34
        const val EVENT_ON_MANUAL_TRIGGER = 35
        const val EVENT_ON_BATTERY_LEVEL_CHANGED = 36

        fun obtain(
            eventType: Int,
            pkgName: String? = null,
            actName: String? = null,
            paneTitle: String? = null
        ): Event {
            return Event(eventType, pkgName, actName, paneTitle)
        }
    }

    fun <V> getExtra(key: Int): V {
        return extras[key].casted()
    }

    override fun toString(): String {
        val typeName = when (type) {
            EVENT_ON_CONTENT_CHANGED -> "contentChanged"
            EVENT_ON_PACKAGE_ENTERED -> "pkgEntered"
            EVENT_ON_PACKAGE_EXITED -> "pkgExited"
            EVENT_ON_NOTIFICATION_RECEIVED -> "statusBarNotificationReceived"
            EVENT_ON_NEW_WINDOW -> "newWindow"
            EVENT_ON_PRIMARY_CLIP_CHANGED -> "primaryClipChanged"
            EVENT_ON_TICK -> "tick"
            EVENT_ON_TOAST_RECEIVED -> "toastNotificationReceived"
            EVENT_ON_VARIABLE_CHANGED -> "variableChanged"
            EVENT_ON_MODE_CHANGED -> "modeChanged"
            EVENT_ON_GEOFENCE_ENTERED -> "geofenceEntered"
            EVENT_ON_GEOFENCE_EXITED -> "geofenceExited"
            EVENT_ON_LOCATION_ARRIVED -> "locationArrived"
            else -> "undefined"
        }
        return "Event(type=$typeName, compInfo=$componentInfo)"
    }

    fun putExtra(key: Int, value: Any) {
        extras.put(key, value)
    }

    private fun extrasHashcode(): Int {
        var hash = 0
        extras.forEach { key, value ->
            hash += 31 * hash + key + value.hashCode()
        }
        return hash
    }

    override fun hashCode(): Int {
        var result = _type
        result = 31 * result + componentInfo.hashCode()
        result = 31 * result + extrasHashcode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Event) return false

        if (_type != other._type) return false
        if (componentInfo != other.componentInfo) return false
        if (extrasHashcode() != other.extrasHashcode()) return false

        return true
    }


}