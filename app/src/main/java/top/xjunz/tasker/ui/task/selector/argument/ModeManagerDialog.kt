/*
 * Copyright (c) 2024 AutoPilot. All rights reserved.
 */

package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.DialogModeManagerBinding
import top.xjunz.tasker.databinding.ItemModeManagerBinding
import top.xjunz.tasker.engine.mode.Mode
import top.xjunz.tasker.engine.mode.ModeRepository
import top.xjunz.tasker.ktx.doWhenCreated
import top.xjunz.tasker.ktx.show
import top.xjunz.tasker.ktx.str
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.ui.base.inlineAdapter
import top.xjunz.tasker.ui.common.TextEditorDialog
import top.xjunz.tasker.util.ClickListenerUtil.setNoDoubleClickListener
import java.util.UUID

/**
 * 模式管理 Dialog：增删模式列表。
 */
class ModeManagerDialog : BaseDialogFragment<DialogModeManagerBinding>() {

    override val isFullScreen: Boolean = false

    private class InnerViewModel : ViewModel() {
        lateinit var modeRepository: ModeRepository
        var onChanged: (() -> Unit)? = null

        val modes = MutableLiveData<List<Mode>>()

        fun loadModes() {
            viewModelScope.launch {
                modes.value = modeRepository.getAllModes()
            }
        }

        fun createMode(name: String) {
            viewModelScope.launch {
                val mode = Mode(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    isActive = false
                )
                modeRepository.createMode(mode)
                loadModes()
                onChanged?.invoke()
            }
        }

        fun deleteMode(id: String) {
            viewModelScope.launch {
                modeRepository.deleteMode(id)
                loadModes()
                onChanged?.invoke()
            }
        }

        fun isModeNameDuplicate(name: String): Boolean {
            return modes.value?.any { it.name == name } == true
        }
    }

    private val viewModel by viewModels<InnerViewModel>()

    private val adapter: RecyclerView.Adapter<*> by lazy {
        inlineAdapter(
            viewModel.modes.value ?: emptyList(),
            ItemModeManagerBinding::class.java,
            {
                binding.btnDelete.setNoDoubleClickListener {
                    val pos = adapterPosition
                    val mode = viewModel.modes.value?.getOrNull(pos) ?: return@setNoDoubleClickListener
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.prompt_delete_mode)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            viewModel.deleteMode(mode.id)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            }
        ) { b, _, item ->
            b.tvModeName.text = item.name
            b.tvStatus.text = if (item.isActive) {
                R.string.mode_active.str
            } else {
                R.string.mode_inactive.str
            }
            b.tvStatus.setTextColor(
                if (item.isActive) {
                    requireContext().getColor(com.google.android.material.R.color.design_default_color_primary)
                } else {
                    requireContext().getColor(com.google.android.material.R.color.design_default_color_on_surface)
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvModes.adapter = adapter
        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnAdd.setNoDoubleClickListener {
            TextEditorDialog()
                .setHint(R.string.mode_name_input_hint.str)
                .init(R.string.create_mode.str, null) { input ->
                    if (input.isNullOrBlank()) {
                        return@init R.string.error_mode_name_empty.str
                    }
                    if (viewModel.isModeNameDuplicate(input.toString())) {
                        return@init R.string.error_mode_name_duplicate.str
                    }
                    viewModel.createMode(input.toString().trim())
                    return@init null
                }.show(childFragmentManager)
        }

        viewModel.modes.observe(viewLifecycleOwner) { modeList ->
            binding.tvEmptyHint.isVisible = modeList.isEmpty()
            binding.rvModes.isVisible = modeList.isNotEmpty()
            @Suppress("UNCHECKED_CAST")
            (binding.rvModes.adapter as? RecyclerView.Adapter<RecyclerView.ViewHolder>)
                ?.notifyDataSetChanged()
        }

        viewModel.loadModes()
    }

    fun init(
        repository: ModeRepository,
        onChanged: (() -> Unit)? = null
    ): ModeManagerDialog = doWhenCreated {
        viewModel.modeRepository = repository
        viewModel.onChanged = onChanged
    }
}
