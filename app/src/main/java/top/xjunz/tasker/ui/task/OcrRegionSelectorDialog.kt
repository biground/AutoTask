/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.task

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.xjunz.tasker.databinding.DialogOcrRegionSelectorBinding
import top.xjunz.tasker.ktx.doWhenCreated
import top.xjunz.tasker.ui.base.BaseDialogFragment

/**
 * OCR 区域选择对话框。
 * 全屏显示当前屏幕截图，用户可拖拽选择识别区域。
 */
class OcrRegionSelectorDialog : BaseDialogFragment<DialogOcrRegionSelectorBinding>() {

    private class InnerViewModel : ViewModel() {
        var screenshot: Bitmap? = null
        var initialRegion: RectF? = null
        val selectedRegion = MutableLiveData<RectF>()
        var onRegionSelected: ((RectF?) -> Unit)? = null
    }

    private val viewModel by viewModels<InnerViewModel>()

    /** 设置截图预览 */
    fun setScreenshot(bitmap: Bitmap?) = doWhenCreated {
        viewModel.screenshot = bitmap
    }

    /** 设置初始选区（百分比值 0.0-1.0） */
    fun setInitialRegion(region: RectF?) = doWhenCreated {
        viewModel.initialRegion = region
    }

    /** 设置确认回调，返回 RectF（百分比值）或 null（取消） */
    fun setOnRegionSelected(callback: (RectF?) -> Unit) = doWhenCreated {
        viewModel.onRegionSelected = callback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置截图预览
        val screenshot = viewModel.screenshot
        if (screenshot != null) {
            binding.ivScreenshot.setImageBitmap(screenshot)
        } else {
            binding.ivScreenshot.setImageDrawable(ColorDrawable(Color.DKGRAY))
        }

        // 设置初始选区
        viewModel.initialRegion?.let { region ->
            binding.regionSelector.setRegion(region)
        }

        // 监听区域变化
        binding.regionSelector.onRegionChanged = { rect ->
            viewModel.selectedRegion.value = rect
        }

        // 确认按钮
        binding.btnConfirm.setOnClickListener {
            viewModel.onRegionSelected?.invoke(viewModel.selectedRegion.value)
            dismiss()
        }

        // 取消按钮
        binding.btnCancel.setOnClickListener {
            viewModel.onRegionSelected?.invoke(null)
            dismiss()
        }

        // 全屏按钮
        binding.btnFullScreen.setOnClickListener {
            binding.regionSelector.resetToFullScreen()
        }
    }
}
