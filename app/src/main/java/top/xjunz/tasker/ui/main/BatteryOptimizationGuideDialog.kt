/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.main

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
import top.xjunz.tasker.databinding.DialogBatteryOptimizationGuideBinding
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.util.ClickListenerUtil.setNoDoubleClickListener

/**
 * 电池优化白名单引导对话框，引导用户将 App 加入电池优化白名单。
 *
 * @author xjunz 2024/01/15
 */
class BatteryOptimizationGuideDialog :
    BaseDialogFragment<DialogBatteryOptimizationGuideBinding>() {

    override val isFullScreen: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStatusView()
        binding.btnGoSettings.setNoDoubleClickListener {
            requestIgnoreBatteryOptimization()
        }
        binding.btnLater.setNoDoubleClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从设置页返回时刷新状态
        updateStatusView()
    }

    /**
     * 检查应用是否已被加入电池优化白名单。
     */
    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    /**
     * 更新白名单状态显示。
     */
    private fun updateStatusView() {
        val ignored = isBatteryOptimizationIgnored()
        binding.tvStatus.isVisible = ignored
        binding.btnGoSettings.isVisible = !ignored
    }

    /**
     * 跳转到系统电池优化设置页面。
     */
    private fun requestIgnoreBatteryOptimization() {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${requireContext().packageName}")
            )
            startActivity(intent)
        } catch (e: Exception) {
            toast(R.string.launch_failed)
        }
    }

    companion object {
        private const val TAG = "BatteryOptimizationGuideDialog"

        fun show(fragmentManager: FragmentManager) {
            BatteryOptimizationGuideDialog().show(fragmentManager, TAG)
        }
    }
}
