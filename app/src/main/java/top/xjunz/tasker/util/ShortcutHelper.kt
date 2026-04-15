package top.xjunz.tasker.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import top.xjunz.tasker.R
import top.xjunz.tasker.ui.main.ShortcutActivity

/**
 * 动态快捷方式管理器（MVP 阶段 prototype）。
 *
 * 提供添加/移除/更新 Dynamic Shortcuts 的基础 API，
 * 后续版本可扩展为用户自定义宏的快捷方式。
 */
object ShortcutHelper {

    private const val MAX_DYNAMIC_SHORTCUTS = 4

    /**
     * 为指定宏添加一个动态快捷方式。
     *
     * @param context 上下文
     * @param macroId 宏的唯一 ID
     * @param macroName 宏的显示名称
     * @return true 如果成功添加
     */
    fun addMacroShortcut(context: Context, macroId: String, macroName: String): Boolean {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return false

        // 检查是否已达到动态快捷方式上限
        if (shortcutManager.dynamicShortcuts.size >= MAX_DYNAMIC_SHORTCUTS) {
            return false
        }

        val intent = Intent(context, ShortcutActivity::class.java).apply {
            action = ShortcutActivity.ACTION_QUICK_RUN
            putExtra("macro_id", macroId)
        }

        val shortcut = ShortcutInfo.Builder(context, "macro_$macroId")
            .setShortLabel(macroName)
            .setLongLabel("运行: $macroName")
            .setIcon(Icon.createWithResource(context, R.drawable.ic_baseline_play_arrow_24))
            .setIntent(intent)
            .build()

        return try {
            shortcutManager.addDynamicShortcuts(listOf(shortcut))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 移除指定宏的动态快捷方式。
     *
     * @param context 上下文
     * @param macroId 宏的唯一 ID
     */
    fun removeMacroShortcut(context: Context, macroId: String) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        shortcutManager.removeDynamicShortcuts(listOf("macro_$macroId"))
    }

    /**
     * 移除所有动态快捷方式。
     *
     * @param context 上下文
     */
    fun removeAllShortcuts(context: Context) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        shortcutManager.removeAllDynamicShortcuts()
    }

    /**
     * 获取当前动态快捷方式数量。
     */
    fun getDynamicShortcutCount(context: Context): Int {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return 0
        return shortcutManager.dynamicShortcuts.size
    }
}
