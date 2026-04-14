/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import top.xjunz.tasker.Preferences
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.FragmentOnboardingBinding
import top.xjunz.tasker.ui.base.BaseFragment

/**
 * 首次启动引导页 Fragment。
 * 通过 ViewPager2 展示 4 个引导页：欢迎、功能介绍、权限配置、完成。
 *
 * @author autopilot 2024
 */
class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>() {

    /** 引导页数据 */
    private data class OnboardingPage(
        val iconRes: Int,
        val titleRes: Int,
        val descRes: Int,
        /** 可选的操作按钮文案，null 表示不显示 */
        val actionRes: Int? = null
    )

    private val pages = listOf(
        OnboardingPage(
            iconRes = R.drawable.ic_baseline_auto_awesome_24,
            titleRes = R.string.onboarding_welcome_title,
            descRes = R.string.onboarding_welcome_desc
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_widgets_24px,
            titleRes = R.string.onboarding_feature_title,
            descRes = R.string.onboarding_feature_desc
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_outline_lock_24,
            titleRes = R.string.onboarding_permission_title,
            descRes = R.string.onboarding_permission_desc,
            actionRes = R.string.onboarding_setup_permissions
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_check_circle_24px,
            titleRes = R.string.onboarding_done_title,
            descRes = R.string.onboarding_done_desc
        )
    )

    /** 完成回调，由 Activity 设置 */
    var onCompleted: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 关联 TabLayout 指示器
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        // 监听页面切换，更新按钮文案
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButton(position)
            }
        })

        // 初始化按钮状态
        updateButton(0)

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                // 最后一页，标记完成
                Preferences.isOnboardingCompleted = true
                onCompleted?.invoke()
            }
        }
    }

    /** 根据当前页面更新底部按钮文案 */
    private fun updateButton(position: Int) {
        binding.btnNext.text = if (position == pages.size - 1) {
            getString(R.string.onboarding_get_started)
        } else {
            getString(R.string.onboarding_next)
        }
    }

    /**
     * ViewPager2 适配器，使用 FragmentStateAdapter 避免生命周期问题
     */
    private inner class OnboardingPagerAdapter(
        fragment: Fragment
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = pages.size

        override fun createFragment(position: Int): Fragment {
            return OnboardingPageFragment.newInstance(
                pages[position].iconRes,
                pages[position].titleRes,
                pages[position].descRes,
                pages[position].actionRes
            )
        }
    }

    /**
     * 单个引导页 Fragment
     */
    class OnboardingPageFragment : Fragment() {

        companion object {
            private const val ARG_ICON = "icon"
            private const val ARG_TITLE = "title"
            private const val ARG_DESC = "desc"
            private const val ARG_ACTION = "action"

            fun newInstance(iconRes: Int, titleRes: Int, descRes: Int, actionRes: Int?): OnboardingPageFragment {
                return OnboardingPageFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_ICON, iconRes)
                        putInt(ARG_TITLE, titleRes)
                        putInt(ARG_DESC, descRes)
                        if (actionRes != null) putInt(ARG_ACTION, actionRes)
                    }
                }
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.item_onboarding_page, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val args = requireArguments()

            view.findViewById<ImageView>(R.id.iv_icon).setImageResource(args.getInt(ARG_ICON))
            view.findViewById<TextView>(R.id.tv_title).setText(args.getInt(ARG_TITLE))
            view.findViewById<TextView>(R.id.tv_description).setText(args.getInt(ARG_DESC))

            val btnAction = view.findViewById<MaterialButton>(R.id.btn_action)
            if (args.containsKey(ARG_ACTION)) {
                btnAction.isVisible = true
                btnAction.setText(args.getInt(ARG_ACTION))
                btnAction.setOnClickListener {
                    // 展示权限引导对话框
                    PermissionGuideDialog.show(childFragmentManager)
                }
            }
        }
    }
}
