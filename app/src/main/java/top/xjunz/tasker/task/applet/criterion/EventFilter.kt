/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.criterion

import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.Event
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.task.applet.flow.ref.BluetoothReferent
import top.xjunz.tasker.task.applet.flow.ref.ComponentInfoWrapper
import top.xjunz.tasker.task.applet.flow.ref.GeofenceReferent
import top.xjunz.tasker.task.applet.flow.ref.IntentReferent
import top.xjunz.tasker.task.applet.flow.ref.ModeChangeReferent
import top.xjunz.tasker.task.applet.flow.ref.NotificationReferent
import top.xjunz.tasker.task.applet.flow.ref.PhoneCallReferent
import top.xjunz.tasker.task.applet.flow.ref.SmsReferent
import top.xjunz.tasker.task.applet.flow.ref.VariableChangeReferent
import top.xjunz.tasker.task.event.BluetoothEventDispatcher
import top.xjunz.tasker.task.event.ClipboardEventDispatcher
import top.xjunz.tasker.task.event.IntentEventDispatcher
import top.xjunz.tasker.task.event.NetworkEventDispatcher
import top.xjunz.tasker.task.event.PhoneCallEventDispatcher
import top.xjunz.tasker.task.event.SmsEventDispatcher
import top.xjunz.tasker.task.location.LocationEventDispatcher
import top.xjunz.tasker.task.mode.ModeChangeEventDispatcher
import top.xjunz.tasker.task.variable.VariableChangeEventDispatcher

/**
 * @author xjunz 2022/08/25
 */
class EventFilter(private val eventType: Int) : Applet() {

    @Deprecated("Only for compatibility use.")
    override val isValueInnate: Boolean = true

    override var relation: Int = REL_OR

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val hit = runtime.events?.find {
            it.type == eventType
        }
        return if (hit == null) {
            AppletResult.EMPTY_FAILURE
        } else {
            when (hit.type) {
                Event.EVENT_ON_NOTIFICATION_RECEIVED, Event.EVENT_ON_TOAST_RECEIVED -> {
                    NotificationReferent(ComponentInfoWrapper.wrap(hit.componentInfo)).asResult()
                }

                Event.EVENT_ON_PRIMARY_CLIP_CHANGED -> {
                    AppletResult.succeeded(hit.getExtra(ClipboardEventDispatcher.EXTRA_PRIMARY_CLIP_TEXT))
                }

                Event.EVENT_ON_TICK -> AppletResult.EMPTY_SUCCESS

                Event.EVENT_ON_DEVICE_BOOTED, Event.EVENT_ON_MANUAL_TRIGGER -> AppletResult.EMPTY_SUCCESS

                Event.EVENT_ON_SCREEN_ON,
                Event.EVENT_ON_SCREEN_OFF,
                Event.EVENT_ON_SCREEN_UNLOCKED,
                Event.EVENT_ON_POWER_CONNECTED,
                Event.EVENT_ON_POWER_DISCONNECTED,
                Event.EVENT_ON_HEADSET_PLUGGED,
                Event.EVENT_ON_HEADSET_UNPLUGGED,
                Event.EVENT_ON_ALARM_FIRED -> AppletResult.EMPTY_SUCCESS

                Event.EVENT_ON_BT_STATE_CHANGED,
                Event.EVENT_ON_BT_DEVICE_CONNECTED,
                Event.EVENT_ON_BT_DEVICE_DISCONNECTED -> {
                    BluetoothReferent(
                        hit.getExtra(BluetoothEventDispatcher.EXTRA_BT_DEVICE_NAME),
                        hit.getExtra(BluetoothEventDispatcher.EXTRA_BT_MAC_ADDRESS)
                    ).asResult()
                }

                Event.EVENT_ON_CALL_STATE_CHANGED -> {
                    PhoneCallReferent(
                        hit.getExtra(PhoneCallEventDispatcher.EXTRA_PHONE_NUMBER),
                        hit.getExtra(PhoneCallEventDispatcher.EXTRA_CALL_STATE)
                    ).asResult()
                }

                Event.EVENT_ON_SMS_RECEIVED -> {
                    SmsReferent(
                        hit.getExtra(SmsEventDispatcher.EXTRA_SMS_SENDER),
                        hit.getExtra(SmsEventDispatcher.EXTRA_SMS_BODY)
                    ).asResult()
                }

                Event.EVENT_ON_INTENT_RECEIVED -> {
                    IntentReferent(
                        hit.getExtra(IntentEventDispatcher.EXTRA_INTENT_ACTION),
                        hit.getExtra(IntentEventDispatcher.EXTRA_INTENT_DATA_URI)
                    ).asResult()
                }

                Event.EVENT_ON_WIFI_CONNECTED, Event.EVENT_ON_WIFI_DISCONNECTED -> {
                    AppletResult.succeeded(hit.getExtra(NetworkEventDispatcher.EXTRA_WIFI_SSID))
                }

                Event.EVENT_ON_VARIABLE_CHANGED -> {
                    VariableChangeReferent(
                        hit.getExtra(VariableChangeEventDispatcher.EXTRA_VARIABLE_NAME),
                        hit.getExtra(VariableChangeEventDispatcher.EXTRA_VARIABLE_OLD_VALUE),
                        hit.getExtra(VariableChangeEventDispatcher.EXTRA_VARIABLE_NEW_VALUE)
                    ).asResult()
                }

                Event.EVENT_ON_MODE_CHANGED -> {
                    ModeChangeReferent(
                        hit.getExtra(ModeChangeEventDispatcher.EXTRA_MODE_NAME),
                        hit.getExtra(ModeChangeEventDispatcher.EXTRA_CHANGE_TYPE),
                        hit.getExtra(ModeChangeEventDispatcher.EXTRA_PREVIOUS_MODE_NAME)
                    ).asResult()
                }

                Event.EVENT_ON_GEOFENCE_ENTERED,
                Event.EVENT_ON_GEOFENCE_EXITED,
                Event.EVENT_ON_LOCATION_ARRIVED -> {
                    GeofenceReferent(
                        hit.getExtra(LocationEventDispatcher.EXTRA_GEOFENCE_NAME),
                        hit.getExtra(LocationEventDispatcher.EXTRA_GEOFENCE_LAT),
                        hit.getExtra(LocationEventDispatcher.EXTRA_GEOFENCE_LNG),
                        hit.getExtra<Int>(LocationEventDispatcher.EXTRA_TRANSITION_TYPE).toString()
                    ).asResult()
                }

                else -> ComponentInfoWrapper.wrap(hit.componentInfo).asResult()
            }
        }
    }

}