/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.bridge

import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 通过 Overlay Window 显示对话框的桥接封装。
 * 需要无障碍服务启动（TYPE_ACCESSIBILITY_OVERLAY）。
 */
object OverlayDialogBridge {

    private val handler = Handler(Looper.getMainLooper())

    private val context get() = ContextBridge.getContext()

    private val windowManager: WindowManager by lazy {
        context.getSystemService(WindowManager::class.java)
    }

    /**
     * 显示 Overlay 对话框。
     *
     * @param title 对话框标题
     * @param content 对话框内容
     * @param timeoutSeconds 超时自动关闭（秒），0 或负数表示不自动关闭
     */
    fun showDialog(title: String, content: String, timeoutSeconds: Int) {
        handler.post {
            val overlay = buildDialogView(title, content)
            val params = buildLayoutParams()

            windowManager.addView(overlay, params)

            val dismissRunnable = Runnable {
                removeViewSafely(overlay)
            }

            // OK 按钮点击关闭
            overlay.findViewWithTag<View>("ok_button")?.setOnClickListener {
                handler.removeCallbacks(dismissRunnable)
                removeViewSafely(overlay)
            }

            // 超时自动关闭
            if (timeoutSeconds > 0) {
                handler.postDelayed(dismissRunnable, timeoutSeconds * 1000L)
            }
        }
    }

    private fun removeViewSafely(view: View) {
        if (view.isAttachedToWindow) {
            windowManager.removeView(view)
        }
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.CENTER
        }
    }

    private fun buildDialogView(title: String, content: String): LinearLayout {
        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }

        // 半透明蒙层容器
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#80000000"))
            isClickable = true
        }

        // 对话框卡片
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val cardBg = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = cardBg
            setPadding(dp(24), dp(20), dp(24), dp(16))
        }

        val cardParams = LinearLayout.LayoutParams(
            dp(300),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // 标题
        val titleView = TextView(context).apply {
            text = title
            setTextColor(Color.parseColor("#212121"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.DEFAULT_BOLD
        }
        val titleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }

        // 内容
        val contentView = TextView(context).apply {
            text = content
            setTextColor(Color.parseColor("#616161"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val contentParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(20)
        }

        // OK 按钮
        val okButton = Button(context).apply {
            tag = "ok_button"
            text = "OK"
            setTextColor(Color.WHITE)
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1976D2"))
                cornerRadius = dp(8).toFloat()
            }
            background = btnBg
            isAllCaps = false
        }
        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(44)
        )

        card.addView(titleView, titleParams)
        card.addView(contentView, contentParams)
        card.addView(okButton, btnParams)
        root.addView(card, cardParams)

        return root
    }
}
