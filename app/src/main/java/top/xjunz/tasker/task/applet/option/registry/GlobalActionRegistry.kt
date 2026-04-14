/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import android.accessibilityservice.AccessibilityService
import android.app.UiAutomationHidden
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.provider.Settings
import android.view.KeyEvent
import top.xjunz.shared.ktx.casted
import top.xjunz.tasker.R
import top.xjunz.tasker.bridge.AudioManagerBridge
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.bridge.PowerManagerBridge
import top.xjunz.tasker.engine.applet.action.emptyArgAction
import top.xjunz.tasker.engine.applet.action.emptyArgOptimisticAction
import top.xjunz.tasker.engine.applet.action.simpleSingleNonNullArgAction
import top.xjunz.tasker.ktx.array
import top.xjunz.tasker.ktx.foreColored
import top.xjunz.tasker.ktx.formatSpans
import top.xjunz.tasker.service.uiAutomation
import top.xjunz.tasker.service.uiAutomatorBridge
import top.xjunz.tasker.task.applet.anno.AppletOrdinal
import top.xjunz.tasker.task.applet.option.AppletOption
import top.xjunz.tasker.task.applet.util.IntValueUtil
import top.xjunz.tasker.task.applet.value.VariantArgType
import top.xjunz.tasker.util.formatMinSecMills

/**
 * @author xjunz 2022/11/15
 */
class GlobalActionRegistry(id: Int) : AppletOptionRegistry(id) {

    private fun globalActionOption(title: Int, action: Int): AppletOption {
        return appletOption(title) {
            emptyArgAction {
                uiAutomation.performGlobalAction(action)
            }
        }
    }

    @AppletOrdinal(0x0000)
    val waitForIdle = appletOption(R.string.wait_for_idle) {
        simpleSingleNonNullArgAction<Int> {
            val xy = IntValueUtil.parseXY(it)
            uiAutomatorBridge.waitForIdle(xy.x.toLong(), xy.y.toLong())
        }
    }.withValueArgument<Int>(
        R.string.wait_for_idle, variantValueType = VariantArgType.INT_INTERVAL_XY
    ).withHelperText(R.string.tip_wait_for_idle).withSingleValueDescriber<Int> {
        val xy = IntValueUtil.parseXY(it)
        R.string.format_wait_for_idle_desc.formatSpans(
            formatMinSecMills(xy.x).foreColored(), formatMinSecMills(xy.y).foreColored()
        )
    }

    @AppletOrdinal(0x0001)
    val pressBack = globalActionOption(R.string.press_back, AccessibilityService.GLOBAL_ACTION_BACK)

    @AppletOrdinal(0x0002)
    val pressRecents = globalActionOption(
        R.string.press_recent, AccessibilityService.GLOBAL_ACTION_RECENTS
    )

    @AppletOrdinal(0x0003)
    val pressHome = globalActionOption(
        R.string.press_home, AccessibilityService.GLOBAL_ACTION_HOME
    )

    @AppletOrdinal(0x0004)
    val openNotificationShade = globalActionOption(
        R.string.open_notification_shade, AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
    )

    @AppletOrdinal(0x0005)
    val lockScreen = globalActionOption(
        R.string.lock_screen,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
        else -1
    ).restrictApiLevel(Build.VERSION_CODES.P)

    @AppletOrdinal(0x0006)
    val takeScreenshot = appletOption(R.string.take_screenshot) {
        emptyArgAction {
            uiAutomation.performGlobalAction(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
                else -1
            )
        }
    }.restrictApiLevel(Build.VERSION_CODES.P)
        .premiumOnly()

    @AppletOrdinal(0x0007)
    val setRotation = appletOption(R.string.rotate_screen) {
        simpleSingleNonNullArgAction<Int> {
            uiAutomatorBridge.setRotation(it)
        }
    }.withValueArgument<Int>(R.string.rotation_direction, VariantArgType.INT_ROTATION).shizukuOnly()
        .withSingleValueAppletDescriber<Int> { applet, t ->
            R.string.format_desc_rotation_screen.formatSpans(
                R.array.rotations.array[t!!].clickToEdit(applet)
            )
        }.descAsTitle()

    @AppletOrdinal(0x0008)
    val wakeUpScreen = appletOption(R.string.wake_up_screen) {
        emptyArgOptimisticAction {
            PowerManagerBridge.wakeUpScreen()
        }
    }

    /**
     * 执行 shell 命令并返回是否成功（stderr 为空视为成功）。
     */
    private suspend fun executeShellCmd(cmd: String): Boolean {
        val out = uiAutomation.casted<UiAutomationHidden>().executeShellCommandRwe(cmd)
        try {
            val err = AutoCloseInputStream(out[2]).bufferedReader().readText()
            return err.isEmpty()
        } finally {
            out.forEach { it.close() }
        }
    }

    @AppletOrdinal(0x0009)
    val setAutoRotate = appletOption(R.string.set_auto_rotate) {
        emptyArgAction {
            val resolver = ContextBridge.getContext().contentResolver
            val current = Settings.System.getInt(
                resolver, Settings.System.ACCELEROMETER_ROTATION, 0
            )
            Settings.System.putInt(
                resolver, Settings.System.ACCELEROMETER_ROTATION, if (current == 0) 1 else 0
            )
        }
    }.shizukuOnly()

    @AppletOrdinal(0x000A)
    val toggleBluetooth = appletOption(R.string.toggle_bluetooth) {
        emptyArgAction {
            val current = Settings.Global.getInt(
                ContextBridge.getContext().contentResolver, Settings.Global.BLUETOOTH_ON, 0
            )
            executeShellCmd("settings put global bluetooth_on ${if (current == 0) 1 else 0}")
        }
    }.shizukuOnly()

    @AppletOrdinal(0x000B)
    val setBrightness = appletOption(R.string.set_brightness) {
        simpleSingleNonNullArgAction<Int> { percent ->
            val resolver = ContextBridge.getContext().contentResolver
            // 关闭自动亮度
            Settings.System.putInt(
                resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            // 百分比映射到 0-255
            val brightness = percent * 255 / 100
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
        }
    }.withValueArgument<Int>(R.string.brightness_value, VariantArgType.INT_PERCENT)
        .shizukuOnly()

    @AppletOrdinal(0x000C)
    val toggleDarkTheme = appletOption(R.string.toggle_dark_theme) {
        emptyArgAction {
            val resolver = ContextBridge.getContext().contentResolver
            val current = Settings.Secure.getInt(resolver, "ui_night_mode", 1)
            // ui_night_mode: 1=off, 2=on
            val target = if (current == 2) "no" else "yes"
            executeShellCmd("cmd uimode night $target")
        }
    }.shizukuOnly()

    @AppletOrdinal(0x000D)
    val toggleWifi = appletOption(R.string.toggle_wifi) {
        emptyArgAction {
            val resolver = ContextBridge.getContext().contentResolver
            val enabled = Settings.Global.getInt(resolver, Settings.Global.WIFI_ON, 0) != 0
            executeShellCmd("svc wifi ${if (enabled) "disable" else "enable"}")
        }
    }.shizukuOnly()

    @AppletOrdinal(0x000E)
    val toggleTorch = appletOption(R.string.toggle_torch) {
        emptyArgAction {
            val cameraManager = ContextBridge.getContext()
                .getSystemService(CameraManager::class.java)
            val cameraId = cameraManager.cameraIdList[0]
            // 通过回调检测当前手电筒状态
            var currentEnabled = false
            val callback = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(id: String, enabled: Boolean) {
                    if (id == cameraId) currentEnabled = enabled
                }
            }
            cameraManager.registerTorchCallback(callback, null)
            // 等待回调触发
            kotlinx.coroutines.delay(50)
            cameraManager.unregisterTorchCallback(callback)
            cameraManager.setTorchMode(cameraId, !currentEnabled)
            true
        }
    }.also { it.appletId = 0x700E } // 避免与 waitForIdle 的 hashCode 碰撞

    @AppletOrdinal(0x000F)
    val controlMedia = appletOption(R.string.control_media) {
        simpleSingleNonNullArgAction<Int> { mediaAction ->
            val keyCode = when (mediaAction) {
                0 -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                1 -> KeyEvent.KEYCODE_MEDIA_NEXT
                2 -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                3 -> KeyEvent.KEYCODE_MEDIA_STOP
                else -> return@simpleSingleNonNullArgAction false
            }
            AudioManagerBridge.dispatchMediaKeyEvent(keyCode)
            true
        }
    }.withValueArgument<Int>(R.string.media_action)
        .withSingleValueAppletDescriber<Int> { applet, t ->
            val actions = R.array.media_actions.array
            val idx = t ?: 0
            if (idx in actions.indices) {
                actions[idx].clickToEdit(applet)
            } else {
                null
            }
        }.descAsTitle()
}