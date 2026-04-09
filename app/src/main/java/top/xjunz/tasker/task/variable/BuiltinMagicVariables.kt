package top.xjunz.tasker.task.variable

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import top.xjunz.tasker.engine.variable.MagicVariableProvider
import top.xjunz.tasker.engine.variable.MagicVariableRegistry
import top.xjunz.tasker.engine.variable.VariableType

// 1. __current_time__ → 当前时间戳（毫秒）
class CurrentTimeMagicVariable : MagicVariableProvider {
    override val name = "__current_time__"
    override val type = VariableType.INTEGER
    override fun getCurrentValue(): Any = System.currentTimeMillis()
}

// 2. __battery_level__ → 电量百分比 (0-100)
class BatteryLevelMagicVariable(private val context: Context) : MagicVariableProvider {
    override val name = "__battery_level__"
    override val type = VariableType.INTEGER
    override fun getCurrentValue(): Any {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        return if (scale > 0) (level * 100L / scale) else -1L
    }
}

// 3. __device_name__ → 设备型号名
class DeviceNameMagicVariable : MagicVariableProvider {
    override val name = "__device_name__"
    override val type = VariableType.STRING
    override fun getCurrentValue(): Any = "${Build.MANUFACTURER} ${Build.MODEL}"
}

// 4. __screen_brightness__ → 屏幕亮度 (0-255)
class ScreenBrightnessMagicVariable(private val context: Context) : MagicVariableProvider {
    override val name = "__screen_brightness__"
    override val type = VariableType.INTEGER
    override fun getCurrentValue(): Any {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toLong()
        } catch (_: Exception) {
            -1L
        }
    }
}

// 5. __wifi_ssid__ → 当前 WiFi SSID
@Suppress("DEPRECATION")
class WifiSsidMagicVariable(private val context: Context) : MagicVariableProvider {
    override val name = "__wifi_ssid__"
    override val type = VariableType.STRING
    override fun getCurrentValue(): Any {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val info = wifiManager?.connectionInfo
        val ssid = info?.ssid?.removeSurrounding("\"") ?: ""
        return if (ssid == "<unknown ssid>") "" else ssid
    }
}

// 注册所有内置魔术变量
fun registerBuiltinMagicVariables(registry: MagicVariableRegistry, context: Context) {
    registry.register(CurrentTimeMagicVariable())
    registry.register(BatteryLevelMagicVariable(context))
    registry.register(DeviceNameMagicVariable())
    registry.register(ScreenBrightnessMagicVariable(context))
    registry.register(WifiSsidMagicVariable(context))
}
