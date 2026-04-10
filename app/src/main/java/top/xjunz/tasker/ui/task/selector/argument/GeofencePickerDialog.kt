package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.databinding.DialogGeofencePickerBinding
import top.xjunz.tasker.ktx.textString
import top.xjunz.tasker.ktx.toast
import top.xjunz.tasker.task.location.AmapApiKeyManager
import top.xjunz.tasker.task.location.GeofenceConfig
import top.xjunz.tasker.ui.base.BaseDialogFragment
import java.util.UUID

/**
 * 地理围栏配置的地图选点对话框
 * 全屏显示高德 MapView，十字准线固定在地图中心，移动地图时取中心坐标
 */
class GeofencePickerDialog : BaseDialogFragment<DialogGeofencePickerBinding>() {

    override val isFullScreen: Boolean = true

    private var aMap: AMap? = null

    // 当前选中的坐标
    private var selectedLat: Double = 39.908823
    private var selectedLng: Double = 116.397470

    // 当前逆地理编码地址
    private var currentAddress: String? = null

    // 选点完成回调
    var onGeofenceSelected: ((GeofenceConfig) -> Unit)? = null

    // 编辑模式：传入已有配置
    var initialConfig: GeofenceConfig? = null

    private val apiKeyManager by lazy { AmapApiKeyManager(app) }

    private var geocodeSearch: GeocodeSearch? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { onConfirm() }

        lifecycleScope.launch {
            apiKeyManager.initialize()
            if (!apiKeyManager.hasApiKey()) {
                showNoApiKeyState()
                return@launch
            }
            initMap(savedInstanceState)
        }
    }

    private fun showNoApiKeyState() {
        binding.mapContainer.isVisible = false
        binding.tvNoApiKey.isVisible = true
    }

    private fun initMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        aMap = binding.mapView.map

        // 初始化逆地理编码搜索
        geocodeSearch = GeocodeSearch(requireContext())
        geocodeSearch?.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, resultCode: Int) {
                if (resultCode == 1000 && result?.regeocodeAddress != null) {
                    currentAddress = result.regeocodeAddress.formatAddress
                    binding.tvAddress.text = currentAddress
                } else {
                    binding.tvAddress.text = null
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, resultCode: Int) {
                // 不使用正向地理编码
            }
        })

        // 编辑模式：定位到已有坐标
        initialConfig?.let { config ->
            selectedLat = config.latitude
            selectedLng = config.longitude
            binding.etName.setText(config.name)
            currentAddress = config.address
            binding.tvAddress.text = config.address
        }

        // 移动相机到选中位置
        val target = LatLng(selectedLat, selectedLng)
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15f))

        // 地图移动停止时取中心坐标并逆地理编码
        aMap?.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: com.amap.api.maps.model.CameraPosition?) {}

            override fun onCameraChangeFinish(position: com.amap.api.maps.model.CameraPosition?) {
                position?.target?.let { center ->
                    selectedLat = center.latitude
                    selectedLng = center.longitude
                    binding.tvAddress.text = getString(R.string.address_loading)
                    reverseGeocode(center.latitude, center.longitude)
                }
            }
        })

        // 首次逆地理编码
        reverseGeocode(selectedLat, selectedLng)
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        val query = RegeocodeQuery(LatLonPoint(lat, lng), 200f, GeocodeSearch.AMAP)
        geocodeSearch?.getFromLocationAsyn(query)
    }

    private fun onConfirm() {
        val name = binding.etName.textString.trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.geofence_name_empty_error)
            return
        }
        binding.tilName.error = null

        val config = GeofenceConfig(
            id = initialConfig?.id ?: UUID.randomUUID().toString(),
            name = name,
            latitude = selectedLat,
            longitude = selectedLng,
            radius = initialConfig?.radius ?: 200f,
            enabled = initialConfig?.enabled ?: true,
            address = currentAddress
        )
        onGeofenceSelected?.invoke(config)
        dismiss()
    }

    // --- MapView 生命周期管理 ---

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
