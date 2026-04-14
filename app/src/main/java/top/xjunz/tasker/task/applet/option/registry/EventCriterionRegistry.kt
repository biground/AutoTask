/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry


import top.xjunz.tasker.R
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.criterion.EventFilter
import top.xjunz.tasker.task.applet.criterion.FileEventCriterion
import top.xjunz.tasker.task.applet.flow.ref.BatteryReferent
import top.xjunz.tasker.task.applet.flow.ref.BluetoothReferent
import top.xjunz.tasker.task.applet.flow.ref.ComponentInfoWrapper
import top.xjunz.tasker.task.applet.flow.ref.GeofenceReferent
import top.xjunz.tasker.task.applet.flow.ref.IntentReferent
import top.xjunz.tasker.task.applet.flow.ref.ModeChangeReferent
import top.xjunz.tasker.task.applet.flow.ref.NotificationReferent
import top.xjunz.tasker.task.applet.flow.ref.PhoneCallReferent
import top.xjunz.tasker.task.applet.flow.ref.SmsReferent
import top.xjunz.tasker.task.applet.flow.ref.VariableChangeReferent
import top.xjunz.tasker.task.applet.value.VariantArgType

/**
 * @author xjunz 2022/08/12
 */
class EventCriterionRegistry(id: Int) : AppletOptionRegistry(id) {

    private fun eventFilterOption(event: Int, label: Int) = appletOption(label) {
        EventFilter(event)
    }

    @AppletOrdinal(0x0000)
    val pkgEntered = eventFilterOption(Event.EVENT_ON_PACKAGE_ENTERED, R.string.on_package_entered)
        .withResult<ComponentInfoWrapper>(R.string.app_entered)
        .withResult<String>(R.string.pkg_name_of_app_entered)
        .withResult<String>(R.string.name_of_app_entered)

    @AppletOrdinal(0x0001)
    val pkgExited = eventFilterOption(Event.EVENT_ON_PACKAGE_EXITED, R.string.on_package_left)
        .withResult<ComponentInfoWrapper>(R.string.app_left)
        .withResult<String>(R.string.pkg_name_of_app_exited)
        .withResult<String>(R.string.name_of_app_exited)

    @AppletOrdinal(0x0002)
    val contentChanged =
        eventFilterOption(Event.EVENT_ON_CONTENT_CHANGED, R.string.on_content_changed)
            .withResult<ComponentInfoWrapper>(R.string.current_app)

    @AppletOrdinal(0x0003)
    val notificationReceived =
        eventFilterOption(
            Event.EVENT_ON_NOTIFICATION_RECEIVED,
            R.string.on_status_bar_notification_received
        ).withResult<NotificationReferent>(R.string.notification_received)
            .withResult<String>(R.string.notification_content)
            .withResult<ComponentInfoWrapper>(R.string.notification_owner_app)
            .withResult<String>(R.string.notification_owner_app_name)

    @AppletOrdinal(0x0004)
    val toastReceived =
        eventFilterOption(Event.EVENT_ON_TOAST_RECEIVED, R.string.on_toast_notification_received)
            .withResult<NotificationReferent>(R.string.notification_received)
            .withResult<String>(R.string.notification_content)
            .withResult<ComponentInfoWrapper>(R.string.notification_owner_app)
            .withResult<String>(R.string.notification_owner_app_name)

    @AppletOrdinal(0x0005)
    val newWindow = eventFilterOption(Event.EVENT_ON_NEW_WINDOW, R.string.on_new_window)
        .withTitleModifier(R.string.tip_new_window)

    @AppletOrdinal(0x0006)
    val timeChanged = eventFilterOption(Event.EVENT_ON_TICK, R.string.on_tik_tok)
        .withTitleModifier(R.string.tip_on_tik_tok)

    @AppletOrdinal(0x0007)
    val fileCreated = appletOption(R.string.on_file_created) {
        FileEventCriterion(Event.EVENT_ON_FILE_CREATED)
    }.withValueArgument<String>(R.string.file_path, VariantArgType.TEXT_FILE_PATH)
        .withResult<String>(R.string.file_path, VariantArgType.TEXT_FILE_PATH)
        .shizukuOnly()

    @AppletOrdinal(0x0008)
    val fileDeleted = appletOption(R.string.on_file_deleted) {
        FileEventCriterion(Event.EVENT_ON_FILE_DELETED)
    }.withValueArgument<String>(R.string.file_path, VariantArgType.TEXT_FILE_PATH)
        .withResult<String>(R.string.file_path, VariantArgType.TEXT_FILE_PATH)
        .shizukuOnly()

    @AppletOrdinal(0x0009)
    val wifiConnected =
        eventFilterOption(Event.EVENT_ON_WIFI_CONNECTED, R.string.on_wifi_connected)
            .withResult<String>(R.string.connected_wifi_ssid)

    @AppletOrdinal(0x0010)
    val wifiDisconnected =
        eventFilterOption(Event.EVENT_ON_WIFI_DISCONNECTED, R.string.on_wifi_disconnected)
            .withResult<String>(R.string.disconnected_wifi_ssid)

    @AppletOrdinal(0X0011)
    val networkAvailable =
        eventFilterOption(Event.EVENT_ON_NETWORK_AVAILABLE, R.string.on_network_available)

    @AppletOrdinal(0X0012)
    val networkUnavailable =
        eventFilterOption(Event.EVENT_ON_NETWORK_AVAILABLE, R.string.on_network_unavailable)

    @AppletOrdinal(0x0013)
    val variableChanged =
        eventFilterOption(Event.EVENT_ON_VARIABLE_CHANGED, R.string.on_variable_changed)
            .withResult<VariableChangeReferent>(R.string.on_variable_changed)
            .withResult<String>(R.string.changed_variable_name)
            .withResult<String>(R.string.variable_old_value)
            .withResult<String>(R.string.variable_new_value)

    @AppletOrdinal(0x0014)
    val modeChanged =
        eventFilterOption(Event.EVENT_ON_MODE_CHANGED, R.string.on_mode_changed)
            .withResult<ModeChangeReferent>(R.string.on_mode_changed)
            .withResult<String>(R.string.changed_mode_name)
            .withResult<String>(R.string.mode_change_type)
            .withResult<String>(R.string.previous_mode_name)

    @AppletOrdinal(0x0015)
    val geofenceEntered =
        eventFilterOption(Event.EVENT_ON_GEOFENCE_ENTERED, R.string.on_geofence_entered)
            .withResult<GeofenceReferent>(R.string.geofence_event_details)
            .withResult<String>(R.string.geofence_name)

    @AppletOrdinal(0x0016)
    val geofenceExited =
        eventFilterOption(Event.EVENT_ON_GEOFENCE_EXITED, R.string.on_geofence_exited)
            .withResult<GeofenceReferent>(R.string.geofence_event_details)
            .withResult<String>(R.string.geofence_name)

    @AppletOrdinal(0x0017)
    val locationArrived =
        eventFilterOption(Event.EVENT_ON_LOCATION_ARRIVED, R.string.on_location_arrived)
            .withResult<GeofenceReferent>(R.string.location_event_details)

    @AppletOrdinal(0x0018)
    val primaryClipChanged =
        eventFilterOption(Event.EVENT_ON_PRIMARY_CLIP_CHANGED, R.string.on_primary_clip_changed)
            .withResult<String>(R.string.current_primary_clip_text)

    @AppletOrdinal(0x0019)
    val manualTrigger =
        eventFilterOption(Event.EVENT_ON_MANUAL_TRIGGER, R.string.on_manual_trigger)

    @AppletOrdinal(0x001A)
    val deviceBooted =
        eventFilterOption(Event.EVENT_ON_DEVICE_BOOTED, R.string.on_device_booted)

    @AppletOrdinal(0x001B)
    val screenOn =
        eventFilterOption(Event.EVENT_ON_SCREEN_ON, R.string.on_screen_on)

    @AppletOrdinal(0x001C)
    val screenOff =
        eventFilterOption(Event.EVENT_ON_SCREEN_OFF, R.string.on_screen_off)

    @AppletOrdinal(0x001D)
    val screenUnlocked =
        eventFilterOption(Event.EVENT_ON_SCREEN_UNLOCKED, R.string.on_screen_unlocked)

    @AppletOrdinal(0x001E)
    val powerConnected =
        eventFilterOption(Event.EVENT_ON_POWER_CONNECTED, R.string.on_power_connected)

    @AppletOrdinal(0x001F)
    val powerDisconnected =
        eventFilterOption(Event.EVENT_ON_POWER_DISCONNECTED, R.string.on_power_disconnected)

    @AppletOrdinal(0x0020)
    val headsetPlugged =
        eventFilterOption(Event.EVENT_ON_HEADSET_PLUGGED, R.string.on_headset_plugged)

    @AppletOrdinal(0x0021)
    val headsetUnplugged =
        eventFilterOption(Event.EVENT_ON_HEADSET_UNPLUGGED, R.string.on_headset_unplugged)

    @AppletOrdinal(0x0022)
    val btStateChanged =
        eventFilterOption(Event.EVENT_ON_BT_STATE_CHANGED, R.string.on_bt_state_changed)
            .withResult<BluetoothReferent>(R.string.on_bt_state_changed)
            .withResult<String>(R.string.bt_device_name)
            .withResult<String>(R.string.bt_mac_address)

    @AppletOrdinal(0x0023)
    val btDeviceConnected =
        eventFilterOption(Event.EVENT_ON_BT_DEVICE_CONNECTED, R.string.on_bt_device_connected)
            .withResult<BluetoothReferent>(R.string.on_bt_device_connected)
            .withResult<String>(R.string.bt_device_name)
            .withResult<String>(R.string.bt_mac_address)

    @AppletOrdinal(0x0024)
    val btDeviceDisconnected =
        eventFilterOption(Event.EVENT_ON_BT_DEVICE_DISCONNECTED, R.string.on_bt_device_disconnected)
            .withResult<BluetoothReferent>(R.string.on_bt_device_disconnected)
            .withResult<String>(R.string.bt_device_name)
            .withResult<String>(R.string.bt_mac_address)

    @AppletOrdinal(0x0025)
    val alarmFired =
        eventFilterOption(Event.EVENT_ON_ALARM_FIRED, R.string.on_alarm_fired)

    @AppletOrdinal(0x0026)
    val callStateChanged =
        eventFilterOption(Event.EVENT_ON_CALL_STATE_CHANGED, R.string.on_call_state_changed)
            .withResult<PhoneCallReferent>(R.string.on_call_state_changed)
            .withResult<String>(R.string.phone_number)
            .withResult<Int>(R.string.call_state)

    @AppletOrdinal(0x0027)
    val smsReceived =
        eventFilterOption(Event.EVENT_ON_SMS_RECEIVED, R.string.on_sms_received)
            .withResult<SmsReferent>(R.string.on_sms_received)
            .withResult<String>(R.string.sms_sender)
            .withResult<String>(R.string.sms_body)

    @AppletOrdinal(0x0028)
    val intentReceived =
        eventFilterOption(Event.EVENT_ON_INTENT_RECEIVED, R.string.on_intent_received)
            .withResult<IntentReferent>(R.string.on_intent_received)
            .withResult<String>(R.string.intent_action)
            .withResult<String>(R.string.intent_data_uri)

    @AppletOrdinal(0x0029)
    val batteryLevelChanged =
        eventFilterOption(Event.EVENT_ON_BATTERY_LEVEL_CHANGED, R.string.on_battery_level_changed)
            .withResult<BatteryReferent>(R.string.on_battery_level_changed)
            .withResult<Int>(R.string.battery_level)
}