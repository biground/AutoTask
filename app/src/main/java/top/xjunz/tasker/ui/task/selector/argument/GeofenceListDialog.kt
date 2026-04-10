package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.databinding.DialogGeofenceListBinding
import top.xjunz.tasker.databinding.ItemGeofenceBinding
import top.xjunz.tasker.task.location.GeofenceConfig
import top.xjunz.tasker.task.location.GeofenceConfigRepository
import top.xjunz.tasker.ui.base.BaseDialogFragment

/**
 * 围栏列表管理对话框
 * RecyclerView 列表 + 新建/删除/启用操作
 */
class GeofenceListDialog : BaseDialogFragment<DialogGeofenceListBinding>() {

    override val isFullScreen: Boolean = true

    // 选中回调
    var onSelect: ((GeofenceConfig) -> Unit)? = null

    private val repository by lazy { GeofenceConfigRepository(app) }

    private val adapter = GeofenceAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvGeofences.adapter = adapter

        // 左滑删除
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val config = adapter.currentList[position]
                lifecycleScope.launch { repository.removeGeofence(config.id) }
            }
        }).attachToRecyclerView(binding.rvGeofences)

        // 新建按钮
        binding.ibAdd.setOnClickListener { openPicker(null) }

        // 观察数据变化
        lifecycleScope.launch {
            repository.initialize()
            repository.observeGeofences().collectLatest { list ->
                adapter.submitList(list)
                binding.tvEmpty.isVisible = list.isEmpty()
                binding.rvGeofences.isVisible = list.isNotEmpty()
            }
        }
    }

    private fun openPicker(config: GeofenceConfig?) {
        val picker = GeofencePickerDialog()
        picker.initialConfig = config
        picker.onGeofenceSelected = { result ->
            lifecycleScope.launch {
                if (config != null) {
                    repository.updateGeofence(result)
                } else {
                    repository.addGeofence(result)
                }
            }
        }
        picker.show(childFragmentManager, "geofencePicker")
    }

    // ---- Adapter ----

    private inner class GeofenceAdapter :
        ListAdapter<GeofenceConfig, GeofenceAdapter.GeofenceViewHolder>(DIFF_CALLBACK) {

        inner class GeofenceViewHolder(val binding: ItemGeofenceBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofenceViewHolder {
            return GeofenceViewHolder(
                ItemGeofenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: GeofenceViewHolder, position: Int) {
            val config = getItem(position)
            holder.binding.tvName.text = config.name

            val detail = buildString {
                if (!config.address.isNullOrBlank()) {
                    append(config.address)
                } else {
                    append("%.4f, %.4f".format(config.latitude, config.longitude))
                }
                append(" | ")
                append(getString(R.string.radius_format, config.radius.toInt()))
            }
            holder.binding.tvDetail.text = detail

            holder.binding.switchEnabled.isChecked = config.enabled
            holder.binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != config.enabled) {
                    lifecycleScope.launch {
                        repository.updateGeofence(config.copy(enabled = isChecked))
                    }
                }
            }

            // 点击 → 选中回调
            holder.itemView.setOnClickListener {
                onSelect?.invoke(config)
                dismiss()
            }

            // 长按 → 编辑
            holder.itemView.setOnLongClickListener {
                openPicker(config)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GeofenceConfig>() {
            override fun areItemsTheSame(oldItem: GeofenceConfig, newItem: GeofenceConfig) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GeofenceConfig, newItem: GeofenceConfig) =
                oldItem == newItem
        }
    }
}
