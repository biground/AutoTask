/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.main

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.DialogPermissionGuideBinding
import top.xjunz.tasker.databinding.ItemPermissionGuideBinding
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.service.A11yAutomatorService
import top.xjunz.tasker.service.AutoPilotNotificationListenerService
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.ui.base.inlineAdapter
import top.xjunz.tasker.util.ClickListenerUtil.setNoDoubleClickListener

/**
 * 特殊权限引导对话框，引导用户逐步授予 6 种特殊权限：
 * - 无障碍服务
 * - 电池优化白名单
 * - 勿扰模式
 * - 悬浮窗权限
 * - 修改系统设置
 * - 通知访问
 *
 * @author xjunz 2024
 */
class PermissionGuideDialog : BaseDialogFragment<DialogPermissionGuideBinding>() {

    override val isFullScreen: Boolean = false

    /**
     * 权限项数据类
     */
    private data class PermissionItem(
        /** 图标资源 ID */
        val iconRes: Int,
        /** 标题字符串资源 ID */
        val titleRes: Int,
        /** 描述字符串资源 ID */
        val descRes: Int,
        /** 检测权限是否已授予 */
        val checkGranted: (Context) -> Boolean,
        /** 跳转授权页面 */
        val requestGrant: (PermissionGuideDialog) -> Unit
    )

    /**
     * 6 种特殊权限定义
     */
    private val permissions: List<PermissionItem> = listOf(
        // 无障碍服务
        PermissionItem(
            iconRes = R.drawable.ic_touch_app_24px,
            titleRes = R.string.perm_a11y_title,
            descRes = R.string.perm_a11y_desc,
            checkGranted = { ctx ->
                val componentName = ComponentName(ctx, A11yAutomatorService::class.java)
                val enabledServices = Settings.Secure.getString(
                    ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""
                enabledServices.contains(componentName.flattenToString(), ignoreCase = true)
            },
            requestGrant = { dialog ->
                dialog.launchIntent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        ),
        // 电池优化白名单
        PermissionItem(
            iconRes = R.drawable.ic_hourglass_bottom_24px,
            titleRes = R.string.perm_battery_title,
            descRes = R.string.perm_battery_desc,
            checkGranted = { ctx ->
                val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                pm.isIgnoringBatteryOptimizations(ctx.packageName)
            },
            requestGrant = { dialog ->
                dialog.launchIntent(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${dialog.requireContext().packageName}")
                    )
                )
            }
        ),
        // 勿扰模式（DND）权限
        PermissionItem(
            iconRes = R.drawable.ic_do_not_disturb_24px,
            titleRes = R.string.perm_dnd_title,
            descRes = R.string.perm_dnd_desc,
            checkGranted = { ctx ->
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.isNotificationPolicyAccessGranted
            },
            requestGrant = { dialog ->
                dialog.launchIntent(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        ),
        // 悬浮窗权限
        PermissionItem(
            iconRes = R.drawable.ic_twotone_layers_24,
            titleRes = R.string.perm_overlay_title,
            descRes = R.string.perm_overlay_desc,
            checkGranted = { ctx -> Settings.canDrawOverlays(ctx) },
            requestGrant = { dialog ->
                dialog.launchIntent(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${dialog.requireContext().packageName}")
                    )
                )
            }
        ),
        // 修改系统设置权限
        PermissionItem(
            iconRes = R.drawable.ic_tune_24px,
            titleRes = R.string.perm_write_settings_title,
            descRes = R.string.perm_write_settings_desc,
            checkGranted = { ctx -> Settings.System.canWrite(ctx) },
            requestGrant = { dialog ->
                dialog.launchIntent(
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${dialog.requireContext().packageName}")
                    )
                )
            }
        ),
        // 通知访问权限
        PermissionItem(
            iconRes = R.drawable.ic_notifications_24px,
            titleRes = R.string.perm_notification_listener_title,
            descRes = R.string.perm_notification_listener_desc,
            checkGranted = { ctx ->
                val flat = Settings.Secure.getString(
                    ctx.contentResolver,
                    "enabled_notification_listeners"
                ) ?: ""
                val myComponent = ComponentName(ctx, AutoPilotNotificationListenerService::class.java)
                flat.contains(myComponent.flattenToString(), ignoreCase = true)
            },
            requestGrant = { dialog ->
                dialog.launchIntent(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        )
    )

    /**
     * 当前权限状态列表，与 permissions 一一对应
     */
    private val grantedStates = MutableList(permissions.size) { false }

    /**
     * RecyclerView 适配器，懒初始化
     */
    private val adapter by lazy {
        inlineAdapter(
            permissions,
            ItemPermissionGuideBinding::class.java,
            initializer = { /* ViewHolder 初始化：无需全局点击 */ }
        ) { itemBinding, index, item ->
            // 设置图标
            itemBinding.ivIcon.setImageResource(item.iconRes)
            // 设置标题和描述
            itemBinding.tvTitle.setText(item.titleRes)
            itemBinding.tvDesc.setText(item.descRes)

            val granted = grantedStates[index]
            // 更新状态文字和颜色
            if (granted) {
                itemBinding.tvStatus.setText(R.string.perm_granted)
                itemBinding.tvStatus.setTextColor(
                    requireContext().getColor(android.R.color.holo_green_dark)
                )
                itemBinding.btnGrant.isVisible = false
                itemBinding.tvStatus.isVisible = true
            } else {
                itemBinding.tvStatus.setText(R.string.perm_not_granted)
                itemBinding.tvStatus.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark)
                )
                itemBinding.btnGrant.isVisible = true
                itemBinding.tvStatus.isVisible = true
            }

            // "前往授予" 按钮点击
            itemBinding.btnGrant.setNoDoubleClickListener {
                item.requestGrant(this@PermissionGuideDialog)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPermissions.adapter = adapter
        binding.btnDone.setNoDoubleClickListener { dismiss() }
    }

    override fun onResume() {
        super.onResume()
        // 从设置页返回后刷新所有权限状态
        refreshPermissionStates()
    }

    /**
     * 刷新所有权限的授予状态并通知 RecyclerView 更新。
     */
    private fun refreshPermissionStates() {
        val ctx = requireContext()
        permissions.forEachIndexed { index, item ->
            grantedStates[index] = try {
                item.checkGranted(ctx)
            } catch (e: Exception) {
                false
            }
        }
        binding.rvPermissions.adapter?.notifyDataSetChanged()
    }

    /**
     * 安全启动 Intent，若无匹配 Activity 则弹出提示。
     */
    private fun launchIntent(intent: Intent) {
        try {
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                toast(R.string.launch_failed)
            }
        } catch (e: Exception) {
            toast(R.string.launch_failed)
        }
    }

    companion object {
        private const val TAG = "PermissionGuideDialog"

        /**
         * 显示特殊权限引导对话框。
         */
        fun show(fragmentManager: FragmentManager) {
            PermissionGuideDialog().show(fragmentManager, TAG)
        }
    }
}
