package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.databinding.DialogAmapApiKeyBinding
import top.xjunz.tasker.ktx.textString
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.task.location.AmapApiKeyManager
import top.xjunz.tasker.ui.base.BaseDialogFragment

/**
 * 高德地图 API Key 配置对话框
 */
class AmapApiKeyDialog : BaseDialogFragment<DialogAmapApiKeyBinding>() {

    override val isFullScreen: Boolean = false

    private val apiKeyManager by lazy { AmapApiKeyManager(app) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ibDismiss.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveApiKey() }
        loadCurrentKey()
    }

    private fun loadCurrentKey() {
        lifecycleScope.launch {
            val currentKey = apiKeyManager.getApiKey()
            if (!currentKey.isNullOrBlank()) {
                binding.etApiKey.setText(currentKey)
            }
            updateStatus()
        }
    }

    private fun updateStatus() {
        val configured = apiKeyManager.hasApiKey()
        binding.tvStatus.text = if (configured) {
            getString(R.string.amap_api_key_configured)
        } else {
            getString(R.string.amap_api_key_not_configured)
        }
    }

    private fun saveApiKey() {
        val key = binding.etApiKey.textString.trim()
        if (key.isEmpty()) {
            binding.tilApiKey.error = getString(R.string.amap_api_key_empty_error)
            return
        }
        binding.tilApiKey.error = null
        lifecycleScope.launch {
            apiKeyManager.setApiKey(key)
            toast(R.string.amap_api_key_saved)
            dismiss()
        }
    }
}
