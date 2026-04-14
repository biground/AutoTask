package top.xjunz.tasker.ui.task

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.DialogVariableManagerBinding
import top.xjunz.tasker.databinding.ItemVariableBinding
import top.xjunz.tasker.engine.variable.Variable
import top.xjunz.tasker.engine.variable.VariableScope
import top.xjunz.tasker.engine.variable.VariableType
import top.xjunz.tasker.ktx.observe
import top.xjunz.tasker.ktx.str
import top.xjunz.tasker.ktx.text
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.ktx.toastUnexpectedError
import top.xjunz.tasker.task.applet.option.AppletOptionFactory
import top.xjunz.tasker.ui.base.BaseDialogFragment
import top.xjunz.tasker.ui.base.inlineAdapter

/**
 * 变量管理对话框：展示/搜索/过滤/编辑/删除全局和局部变量
 *
 * @author AutoPilot
 */
class VariableManagerDialog : BaseDialogFragment<DialogVariableManagerBinding>() {

    // -------------------------  ViewModel  -------------------------

    private class InnerViewModel : ViewModel() {
        /** 所有变量（原始列表，不受过滤影响） */
        val allVariables = MutableLiveData<List<Variable>>(emptyList())

        /** 经过搜索 + 类型过滤后的显示列表 */
        val filteredVariables = MutableLiveData<List<Variable>>(emptyList())

        /** 当前搜索关键字 */
        var searchQuery: String = ""

        /** 当前选中的类型过滤（null = 全部） */
        var typeFilter: VariableType? = null

        /** 更新过滤结果 */
        fun applyFilter() {
            val query = searchQuery.trim().lowercase()
            val all = allVariables.value ?: emptyList()
            filteredVariables.value = all.filter { v ->
                val matchesQuery = query.isEmpty() || v.name.lowercase().contains(query)
                val matchesType = typeFilter == null || v.type == typeFilter
                matchesQuery && matchesType
            }
        }

        /** 从仓库加载所有变量 */
        fun loadVariables() {
            viewModelScope.launch {
                try {
                    val vars = AppletOptionFactory.variableRepository.getAllVariables()
                    allVariables.value = vars
                    applyFilter()
                } catch (e: Exception) {
                    toastUnexpectedError(e)
                }
            }
        }

        /** 删除变量并刷新 */
        fun deleteVariable(name: String) {
            viewModelScope.launch {
                try {
                    AppletOptionFactory.variableRepository.deleteVariable(name)
                    loadVariables()
                } catch (e: Exception) {
                    toastUnexpectedError(e)
                }
            }
        }

        /** 更新变量值并刷新 */
        fun updateVariable(variable: Variable, newValue: Any?) {
            viewModelScope.launch {
                try {
                    val updated = variable.copy(value = newValue)
                    AppletOptionFactory.variableRepository.setVariable(updated)
                    loadVariables()
                } catch (e: Exception) {
                    toastUnexpectedError(e)
                }
            }
        }

        /** 添加新变量（MVP 默认 STRING 类型） */
        fun addVariable(name: String, type: VariableType, value: Any?) {
            viewModelScope.launch {
                try {
                    val v = Variable(
                        id = name,
                        name = name,
                        type = type,
                        value = value,
                        scope = VariableScope.GLOBAL
                    )
                    AppletOptionFactory.variableRepository.setVariable(v)
                    loadVariables()
                } catch (e: Exception) {
                    toastUnexpectedError(e)
                }
            }
        }
    }

    // -------------------------  Fields  -------------------------

    private val viewModel by viewModels<InnerViewModel>()

    private val adapter: RecyclerView.Adapter<*> by lazy {
        inlineAdapter(
            viewModel.filteredVariables.value ?: emptyList(),
            ItemVariableBinding::class.java,
            {
                // 编辑按钮点击
                binding.btnEdit.setOnClickListener {
                    val list = viewModel.filteredVariables.value ?: return@setOnClickListener
                    val variable = list.getOrNull(adapterPosition) ?: return@setOnClickListener
                    showEditDialog(variable)
                }
                // 删除按钮点击
                binding.btnDelete.setOnClickListener {
                    val list = viewModel.filteredVariables.value ?: return@setOnClickListener
                    val variable = list.getOrNull(adapterPosition) ?: return@setOnClickListener
                    showDeleteConfirmDialog(variable)
                }
            }
        ) { binding, _, variable ->
            binding.tvVariableName.text = variable.name
            binding.tvVariableType.text = variable.type.name
            binding.tvVariableScope.text = when (variable.scope) {
                VariableScope.GLOBAL -> R.string.variable_scope_global.str
                VariableScope.LOCAL -> R.string.variable_scope_local.str
            }
            binding.tvVariableValue.text = variable.value?.toString() ?: "—"

            // MVP：仅 String/Int/Boolean 可编辑，其余只读
            val editable = variable.type in listOf(
                VariableType.STRING, VariableType.INTEGER, VariableType.BOOLEAN
            )
            binding.btnEdit.isEnabled = editable
            if (!editable) {
                binding.btnEdit.alpha = 0.4f
            } else {
                binding.btnEdit.alpha = 1f
            }
        }
    }

    // -------------------------  Lifecycle  -------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 关闭按钮
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.ic_baseline_close_24)

        // RecyclerView
        binding.rvVariables.adapter = adapter

        // 搜索框监听
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchQuery = s?.toString() ?: ""
                viewModel.applyFilter()
            }
        })

        // Chip 类型过滤
        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.typeFilter = null
                viewModel.applyFilter()
            }
        }
        binding.chipBoolean.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.typeFilter = VariableType.BOOLEAN
                viewModel.applyFilter()
            }
        }
        binding.chipInteger.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.typeFilter = VariableType.INTEGER
                viewModel.applyFilter()
            }
        }
        binding.chipDecimal.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.typeFilter = VariableType.DECIMAL
                viewModel.applyFilter()
            }
        }
        binding.chipString.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                viewModel.typeFilter = VariableType.STRING
                viewModel.applyFilter()
            }
        }

        // FAB 添加变量
        binding.fabAddVariable.setOnClickListener {
            showAddVariableDialog()
        }

        // 观察过滤结果
        observe(viewModel.filteredVariables) { vars ->
            // 刷新 adapter 数据（通过重建 adapter 简化 MVP 逻辑）
            binding.rvVariables.swapAdapter(
                inlineAdapter(
                    vars,
                    ItemVariableBinding::class.java,
                    {
                        binding.btnEdit.setOnClickListener {
                            val list = viewModel.filteredVariables.value ?: return@setOnClickListener
                            val variable = list.getOrNull(adapterPosition) ?: return@setOnClickListener
                            showEditDialog(variable)
                        }
                        binding.btnDelete.setOnClickListener {
                            val list = viewModel.filteredVariables.value ?: return@setOnClickListener
                            val variable = list.getOrNull(adapterPosition) ?: return@setOnClickListener
                            showDeleteConfirmDialog(variable)
                        }
                    }
                ) { b, _, variable ->
                    b.tvVariableName.text = variable.name
                    b.tvVariableType.text = variable.type.name
                    b.tvVariableScope.text = when (variable.scope) {
                        VariableScope.GLOBAL -> R.string.variable_scope_global.str
                        VariableScope.LOCAL -> R.string.variable_scope_local.str
                    }
                    b.tvVariableValue.text = variable.value?.toString() ?: "—"
                    val editable = variable.type in listOf(
                        VariableType.STRING, VariableType.INTEGER, VariableType.BOOLEAN
                    )
                    b.btnEdit.isEnabled = editable
                    b.btnEdit.alpha = if (editable) 1f else 0.4f
                },
                false
            )
            // 空状态控制
            binding.tvEmpty.visibility = if (vars.isEmpty()) View.VISIBLE else View.GONE
        }

        // 加载数据
        viewModel.loadVariables()
    }

    // -------------------------  对话框  -------------------------

    /** 编辑变量值的对话框（MVP 支持 String/Int/Boolean） */
    private fun showEditDialog(variable: Variable) {
        when (variable.type) {
            VariableType.BOOLEAN -> {
                // Boolean: 用单选列表
                val options = arrayOf("true", "false")
                val current = variable.value?.toString()?.lowercase()
                val checkedItem = if (current == "false") 1 else 0
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.edit_variable.text)
                    .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                        viewModel.updateVariable(variable, which == 0)
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            VariableType.INTEGER -> {
                // Integer: 数字输入框
                showInputDialog(
                    variable = variable,
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED,
                    transform = { it.toLongOrNull() ?: 0L }
                )
            }

            VariableType.STRING -> {
                // String: 文本输入框
                showInputDialog(
                    variable = variable,
                    inputType = InputType.TYPE_CLASS_TEXT,
                    transform = { it }
                )
            }

            else -> {
                // 其他类型只读提示
                toast(R.string.variable_type_readonly_hint)
            }
        }
    }

    /** 通用文本输入对话框 */
    private fun showInputDialog(variable: Variable, inputType: Int, transform: (String) -> Any?) {
        // 复用任意含 EditText 的 layout 的思路比较繁琐，改用 AlertDialog 内置 setView
        val editText = android.widget.EditText(requireContext()).apply {
            this.inputType = inputType
            this.setText(variable.value?.toString() ?: "")
            this.hint = R.string.variable_value_label.str
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, pad / 2)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_variable.text)
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val text = editText.text.toString()
                if (text.isBlank() && variable.type != VariableType.STRING) {
                    toast(R.string.error_empty_input)
                    return@setPositiveButton
                }
                viewModel.updateVariable(variable, transform(text))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** 删除确认对话框 */
    private fun showDeleteConfirmDialog(variable: Variable) {
        val message = getString(R.string.prompt_delete_variable, variable.name)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete.text)
            .setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_COMPACT))
            .setPositiveButton(R.string.delete.text) { _, _ ->
                viewModel.deleteVariable(variable.name)
                toast(getString(R.string.format_delete, variable.name))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** 添加新变量对话框（MVP 默认 STRING / GLOBAL） */
    private fun showAddVariableDialog() {
        val nameInput = android.widget.EditText(requireContext()).apply {
            hint = R.string.variable_name_label.str
            inputType = InputType.TYPE_CLASS_TEXT
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_variable.text)
            .setView(nameInput)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) {
                    toast(R.string.error_empty_input)
                    return@setPositiveButton
                }
                viewModel.addVariable(name, VariableType.STRING, "")
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // -------------------------  Companion  -------------------------

    companion object {
        /** 展示变量管理对话框 */
        fun show(fragmentManager: FragmentManager) {
            VariableManagerDialog().show(fragmentManager, "VariableManagerDialog")
        }
    }
}
