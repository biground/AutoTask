package top.xjunz.tasker.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 处理 App Shortcut 的 Intent 路由。
 * 无 UI，仅做 Intent 转发后 finish。
 */
class ShortcutActivity : AppCompatActivity() {
    companion object {
        const val ACTION_CREATE_MACRO = "top.xjunz.tasker.ACTION_CREATE_MACRO"
        const val ACTION_QUICK_RUN = "top.xjunz.tasker.ACTION_QUICK_RUN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeIntent(intent)
        finish()
    }

    private fun routeIntent(intent: Intent?) {
        val targetIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            when (intent?.action) {
                ACTION_CREATE_MACRO -> putExtra("shortcut_action", "create_macro")
                ACTION_QUICK_RUN -> putExtra("shortcut_action", "quick_run")
            }
        }
        startActivity(targetIntent)
    }
}
