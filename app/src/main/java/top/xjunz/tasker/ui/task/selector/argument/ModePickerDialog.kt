/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.DialogModePickerBinding
import top.xjunz.tasker.databinding.ItemEnumSelectorBinding
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.ktx.doWhenCreated
import top.xjunz.tasker.ktx.show
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.ui.base.inlineAdapter
import top.xjunz.tasker.util.ClickListenerUtil.setNoDoubleClickListener

/**
 * 模式选择器 Dialog：从 ModeRepository 中列出已有模式，供用户选择。
 */
class ModePickerDialog : BaseDialogFragment<DialogModePickerBinding>() {

    override val isFullScreen: Boolean = false

    private class InnerViewModel : ViewModel() {
        lateinit var modeRepository: ModeRepository
        lateinit var doOnCompletion: (String) -> Unit

        var title: CharSequence? = null
        var selectedModeName: String? = null

        val modes = MutableLiveData<List<Mode>>()
        val selectedIndex = MutableLiveData<Int>()

        fun loadModes() {
            viewModelScope.launch {
                val list = modeRepository.getAllModes()
                modes.value = list
                // 恢复之前的选中状态
                if (selectedModeName != null) {
                    val idx = list.indexOfFirst { it.name == selectedModeName }
                    if (idx >= 0) selectedIndex.value = idx
                }
            }
        }

        fun selectMode(index: Int) {
            selectedIndex.value = index
        }

        fun complete(): String? {
            val idx = selectedIndex.value ?: return null
            val modeList = modes.value ?: return null
            if (idx in modeList.indices) {
                return modeList[idx].name
            }
            return null
        }
    }

    private val viewModel by viewModels<InnerViewModel>()

    private val adapter: RecyclerView.Adapter<*> by lazy {
        inlineAdapter(
            viewModel.modes.value ?: emptyList(),
            ItemEnumSelectorBinding::class.java,
            {
                itemView.setNoDoubleClickListener {
                    viewModel.selectMode(adapterPosition)
                }
            }
        ) { b, p, item ->
            b.tvTitle.text = item.name
            b.root.isSelected = viewModel.selectedIndex.value == p
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = viewModel.title ?: getString(R.string.select_mode)
        binding.rvModes.adapter = adapter
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnComplete.setNoDoubleClickListener {
            val name = viewModel.complete()
            if (name == null) {
                toastAndShake(R.string.error_no_selection)
            } else {
                viewModel.doOnCompletion(name)
                dismiss()
            }
        }
        binding.btnManage.setNoDoubleClickListener {
            ModeManagerDialog().init(viewModel.modeRepository) {
                // 模式数据变更后重新加载
                viewModel.loadModes()
            }.show(childFragmentManager)
        }

        viewModel.modes.observe(viewLifecycleOwner) { modeList ->
            binding.tvEmptyHint.isVisible = modeList.isEmpty()
            binding.rvModes.isVisible = modeList.isNotEmpty()
            // 因为 inlineAdapter 绑定的是初始列表，需要重新设置
            @Suppress("UNCHECKED_CAST")
            (binding.rvModes.adapter as? RecyclerView.Adapter<RecyclerView.ViewHolder>)
                ?.notifyDataSetChanged()
        }

        viewModel.selectedIndex.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        viewModel.loadModes()
    }

    fun init(
        repository: ModeRepository,
        onCompletion: (String) -> Unit
    ): ModePickerDialog = doWhenCreated {
        viewModel.modeRepository = repository
        viewModel.doOnCompletion = onCompletion
    }

    fun setTitle(title: CharSequence?): ModePickerDialog = doWhenCreated {
        viewModel.title = title
    }

    fun setInitialSelection(modeName: String?): ModePickerDialog = doWhenCreated {
        viewModel.selectedModeName = modeName
    }
}
