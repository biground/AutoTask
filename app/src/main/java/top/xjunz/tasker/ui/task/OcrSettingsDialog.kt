package top.xjunz.tasker.ui.task

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.databinding.DialogOcrSettingsBinding
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.task.ocr.CloudOcrProvider
import top.xjunz.tasker.task.ocr.MlKitOcrProvider
import top.xjunz.tasker.task.ocr.OcrManager
import top.xjunz.tasker.ui.base.BaseDialogFragment

// DataStore 扩展属性
// TODO: 正式上线前需改用 EncryptedSharedPreferences 存储 API Key，防止明文泄露
private val Context.ocrSettingsDataStore by preferencesDataStore(name = "ocr_settings")

/**
 * OCR 设置对话框
 *
 * 功能：引擎选择（ML Kit 离线 / 云端）、云端 API Key 配置、测试识别、结果预览
 */
class OcrSettingsDialog : BaseDialogFragment<DialogOcrSettingsBinding>() {

    companion object {
        /** 引擎类型：0 = ML Kit，1 = 云端 */
        private val KEY_ENGINE_TYPE = intPreferencesKey("engine_type")
        private val KEY_CLOUD_API_KEY = stringPreferencesKey("cloud_api_key")

        const val ENGINE_MLKIT = 0
        const val ENGINE_CLOUD = 1
    }

    override val isFullScreen: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ibDismiss.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveSettings() }
        binding.btnTest.setOnClickListener { testRecognize() }

        binding.rgEngine.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            updateCloudKeyVisibility(checkedId == R.id.rb_cloud)
        }

        loadSettings()
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val prefs = app.ocrSettingsDataStore.data.first()
            val engineType = prefs[KEY_ENGINE_TYPE] ?: ENGINE_MLKIT
            val cloudApiKey = prefs[KEY_CLOUD_API_KEY] ?: ""

            if (engineType == ENGINE_CLOUD) {
                binding.rbCloud.isChecked = true
                updateCloudKeyVisibility(true)
            } else {
                binding.rbMlkit.isChecked = true
                updateCloudKeyVisibility(false)
            }
            binding.etCloudApiKey.setText(cloudApiKey)
        }
    }

    private fun updateCloudKeyVisibility(showCloud: Boolean) {
        binding.tilCloudApiKey.visibility = if (showCloud) View.VISIBLE else View.GONE
    }

    private fun getSelectedEngineType(): Int {
        return if (binding.rbCloud.isChecked) ENGINE_CLOUD else ENGINE_MLKIT
    }

    private fun saveSettings() {
        val engineType = getSelectedEngineType()
        val cloudApiKey = binding.etCloudApiKey.text?.toString()?.trim() ?: ""

        lifecycleScope.launch {
            app.ocrSettingsDataStore.edit { prefs ->
                prefs[KEY_ENGINE_TYPE] = engineType
                prefs[KEY_CLOUD_API_KEY] = cloudApiKey
            }
            // 切换引擎
            if (engineType == ENGINE_MLKIT) {
                OcrManager.setProvider(MlKitOcrProvider())
            }
            // 云端引擎暂无具体实现，保持 ML Kit
            toast(R.string.ocr_settings)
            dismiss()
        }
    }

    private fun testRecognize() {
        binding.btnTest.isEnabled = false
        binding.tvResult.text = "..."
        lifecycleScope.launch {
            try {
                val result = OcrManager.recognizeScreen()
                if (result != null && result.blocks.isNotEmpty()) {
                    val text = result.blocks.joinToString("\n") { it.text }
                    binding.tvResult.text = text
                } else {
                    binding.tvResult.text = getString(R.string.ocr_result_preview)
                }
            } catch (e: Exception) {
                binding.tvResult.text = e.message ?: "Error"
            } finally {
                binding.btnTest.isEnabled = true
            }
        }
    }
}
