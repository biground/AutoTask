package top.xjunz.tasker.ui.task.selector.argument

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.xjunz.tasker.R
import top.xjunz.tasker.app
import top.xjunz.tasker.databinding.DialogGeofencePickerBinding
import top.xjunz.tasker.ktx.textString
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

    // POI 搜索防抖
    private var searchJob: Job? = null

    // POI 搜索结果适配器
    private val poiAdapter = PoiResultAdapter { poi ->
        val latLng = LatLng(poi.latLonPoint.latitude, poi.latLonPoint.longitude)
        aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        binding.etName.setText(poi.title)
        currentAddress = poi.snippet
        binding.tvAddress.text = poi.snippet
        hidePoiResults()
        binding.etSearch.text?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { onConfirm() }

        setupSearchView()

        lifecycleScope.launch {
            apiKeyManager.initialize()
            if (!apiKeyManager.hasApiKey()) {
                showNoApiKeyState()
                return@launch
            }
            initMap(savedInstanceState)
        }
    }

    private fun setupSearchView() {
        binding.rvPoiResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPoiResults.adapter = poiAdapter

        binding.etSearch.doAfterTextChanged { editable ->
            val keyword = editable?.toString()?.trim().orEmpty()
            searchJob?.cancel()
            if (keyword.length < 2) {
                hidePoiResults()
                return@doAfterTextChanged
            }
            searchJob = lifecycleScope.launch {
                delay(300)
                searchPoi(keyword)
            }
        }
    }

    private fun searchPoi(keyword: String) {
        val query = PoiSearchV2.Query(keyword, "", "")
        query.pageSize = 20
        val poiSearch = PoiSearchV2(requireContext(), query)
        poiSearch.setOnPoiSearchListener(object : PoiSearchV2.OnPoiSearchListener {
            override fun onPoiSearched(result: PoiResultV2?, rCode: Int) {
                if (rCode == 1000 && result != null) {
                    val items = result.pois.orEmpty()
                    if (items.isEmpty()) {
                        showNoResults()
                    } else {
                        showPoiResults(items)
                    }
                } else {
                    showNoResults()
                }
            }

            override fun onPoiItemSearched(item: PoiItemV2?, rCode: Int) {}
        })
        poiSearch.searchPOIAsyn()
    }

    private fun showPoiResults(items: List<PoiItemV2>) {
        poiAdapter.updateData(items)
        binding.rvPoiResults.isVisible = true
        binding.tvNoResults.isVisible = false
    }

    private fun showNoResults() {
        binding.rvPoiResults.isVisible = false
        binding.tvNoResults.isVisible = true
    }

    private fun hidePoiResults() {
        binding.rvPoiResults.isVisible = false
        binding.tvNoResults.isVisible = false
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

    // --- POI 搜索结果适配器 ---

    private class PoiResultAdapter(
        private val onItemClick: (PoiItemV2) -> Unit
    ) : RecyclerView.Adapter<PoiResultAdapter.ViewHolder>() {

        private var items: List<PoiItemV2> = emptyList()

        fun updateData(newItems: List<PoiItemV2>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_poi_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val poi = items[position]
            holder.tvName.text = poi.title
            holder.tvAddress.text = poi.snippet
            holder.itemView.setOnClickListener { onItemClick(poi) }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_poi_name)
            val tvAddress: TextView = view.findViewById(R.id.tv_poi_address)
        }
    }
}
