package top.xjunz.tasker.task.applet.action

import android.content.Intent
import android.provider.AlarmClock
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.engine.applet.base.Applet
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * 设定系统闹钟动作。
 *
 * 通过 AlarmClock.ACTION_SET_ALARM Intent 调用系统时钟应用。
 *
 * 参数：
 * - values[0]: hour (Int, 0-23)
 * - values[1]: minute (Int, 0-59)
 * - values[2]: message (String?, 闹钟备注)
 * - values[3]: vibrate (Boolean, 是否震动)
 */
class SetAlarmAction : Applet() {

    companion object {
        internal fun validateParams(hour: Int, minute: Int): Boolean {
            return hour in 0..23 && minute in 0..59
        }
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val hour = values[0] as? Int ?: return AppletResult.EMPTY_FAILURE
        val minute = values[1] as? Int ?: return AppletResult.EMPTY_FAILURE
        val message = values[2] as? String
        val vibrate = values[3] as? Boolean ?: true

        if (!validateParams(hour, minute)) {
            return AppletResult.EMPTY_FAILURE
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            if (message != null) {
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
            }
            putExtra(AlarmClock.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val context = ContextBridge.getContext()
        if (intent.resolveActivity(context.packageManager) == null) {
            return AppletResult.EMPTY_FAILURE
        }

        return try {
            context.startActivity(intent)
            AppletResult.EMPTY_SUCCESS
        } catch (_: Exception) {
            AppletResult.EMPTY_FAILURE
        }
    }
}
