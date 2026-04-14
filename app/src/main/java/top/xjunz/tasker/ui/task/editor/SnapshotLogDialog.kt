/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.task.editor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.DialogTaskLogBinding
import top.xjunz.tasker.engine.task.TaskSnapshot
import top.xjunz.tasker.ktx.applySystemInsets
import top.xjunz.tasker.ktx.doWhenCreated
import top.xjunz.tasker.ktx.format
import top.xjunz.tasker.ktx.observeConfirmation
import top.xjunz.tasker.ktx.observeError
import top.xjunz.tasker.ktx.str
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.task.runtime.LocalTaskManager
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.util.ClickListenerUtil.setNoDoubleClickListener
import top.xjunz.tasker.util.formatCurrentTime

/**
 * 快照日志查看器，支持文本搜索和时间排序。
 * 由于 TaskSnapshot.log 是纯文本（无级别标记），日志级别过滤降级为文本搜索。
 *
 * @author xjunz 2023/03/15
 */
class SnapshotLogDialog : BaseDialogFragment<DialogTaskLogBinding>(),
    ActivityResultCallback<Uri?> {

    private class InnerViewModel : ViewModel() {

        lateinit var taskName: String

        lateinit var snapshot: TaskSnapshot

        val showClearLogConfirmation = MutableLiveData<Boolean>()

        val onSaveToStorageError = MutableLiveData<Throwable>()

        /** 搜索关键词 */
        var searchQuery: String = ""

        /** 是否最新在前（默认 true） */
        var newestFirst: Boolean = true

        /** 原始日志按行拆分 */
        val rawLines: List<String>
            get() = snapshot.log?.lines().orEmpty()

        /**
         * 根据搜索关键词和排序方式，返回过滤后的日志文本。
         * @return Pair<显示文本, 匹配行数>（匹配行数 -1 表示无搜索）
         */
        fun getFilteredLog(): Pair<String, Int> {
            val lines = rawLines
            val sorted = if (newestFirst) lines.asReversed() else lines
            if (searchQuery.isBlank()) {
                return sorted.joinToString("\n") to -1
            }
            val matched = sorted.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
            return matched.joinToString("\n") to matched.size
        }

        fun saveLogToStorage(contentResolver: ContentResolver, uri: Uri) {
            viewModelScope.async {
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use {
                        it.bufferedWriter().apply {
                            write(snapshot.log)
                            flush()
                        }
                    }
                }
                toast(R.string.saved_to_storage)
            }.invokeOnCompletion {
                if (it != null && it !is CancellationException) {
                    onSaveToStorageError.postValue(it)
                }
            }
        }
    }

    private lateinit var saveToSAFLauncher: ActivityResultLauncher<String>

    private val viewModel by viewModels<InnerViewModel>()

    fun setSnapshot(taskName: String, snapshot: TaskSnapshot) = doWhenCreated {
        viewModel.snapshot = snapshot
        viewModel.taskName = taskName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveToSAFLauncher =
            registerForActivityResult(object : ActivityResultContract<String, Uri?>() {
                override fun createIntent(context: Context, input: String): Intent {
                    return Intent.createChooser(
                        Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("*/*").putExtra(Intent.EXTRA_TITLE, input),
                        R.string.select_export_path.str
                    )
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                    if (resultCode == Activity.RESULT_CANCELED) toast(R.string.cancelled)
                    return intent?.data
                }
            }, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lineCounter.bindTo(binding.etLog)
        binding.ibDismiss.setNoDoubleClickListener {
            dismiss()
        }
        binding.ibClear.setNoDoubleClickListener {
            viewModel.showClearLogConfirmation.value = true
        }
        binding.appBar.applySystemInsets { v, insets ->
            v.updatePadding(top = insets.top)
        }

        binding.ibSave.setNoDoubleClickListener {
            saveToSAFLauncher.launch(
                R.string.format_snapshot_filename.format(
                    viewModel.taskName, formatCurrentTime()
                )
            )
            toast(R.string.tip_select_log_save_dir)
        }

        // 初始化排序按钮
        updateSortButtonIcon()
        binding.ibSort.setNoDoubleClickListener {
            viewModel.newestFirst = !viewModel.newestFirst
            updateSortButtonIcon()
            refreshLogDisplay()
        }

        // 初始化搜索栏
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchQuery = s?.toString().orEmpty()
                refreshLogDisplay()
            }
        })

        // 初始显示（默认最新在前）
        refreshLogDisplay()

        binding.scrollView.doOnPreDraw {
            binding.etLog.minimumHeight = it.height
        }
        observeConfirmation(
            viewModel.showClearLogConfirmation,
            R.string.prompt_clear_snapshot_log
        ) {
            viewModel.snapshot.log = null
            LocalTaskManager.clearLog(viewModel.snapshot.checksum, viewModel.snapshot.id)
            dismiss()
        }
        observeError(viewModel.onSaveToStorageError)
    }

    /** 刷新日志显示内容（搜索 + 排序） */
    private fun refreshLogDisplay() {
        val (text, matchCount) = viewModel.getFilteredLog()
        binding.etLog.setText(text)
        // 更新搜索结果计数
        if (matchCount >= 0) {
            binding.tvSearchCount.isVisible = true
            binding.tvSearchCount.text = if (matchCount > 0) {
                R.string.format_log_match_count.format(matchCount)
            } else {
                R.string.log_no_matches.str
            }
        } else {
            binding.tvSearchCount.isVisible = false
        }
    }

    /** 更新排序按钮的提示文字 */
    private fun updateSortButtonIcon() {
        binding.ibSort.contentDescription = if (viewModel.newestFirst) {
            R.string.log_sort_newest_first.str
        } else {
            R.string.log_sort_oldest_first.str
        }
    }

    override fun onActivityResult(result: Uri?) {
        if (result != null) {
            viewModel.saveLogToStorage(requireActivity().contentResolver, result)
        }
    }
}