/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.ui.task

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 区域选择器自定义 View，用于 OCR 识别区域的可视化选择。
 * 绘制半透明遮罩覆盖选区外区域，选区内部透明，支持拖拽边/角/整体移动。
 */
class RegionSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        // 触摸边缘判定距离 (dp)
        private const val TOUCH_SLOP_DP = 40f
        // 最小选区占 View 宽高的比例
        private const val MIN_REGION_RATIO = 0.1f
        // 遮罩颜色
        private const val OVERLAY_COLOR = 0x99000000.toInt()
        // 边框颜色
        private const val BORDER_COLOR = Color.WHITE
        // 边框宽度 (dp)
        private const val BORDER_WIDTH_DP = 2f
        // 角标长度 (dp)
        private const val CORNER_LENGTH_DP = 20f
        // 角标宽度 (dp)
        private const val CORNER_WIDTH_DP = 4f
    }

    // 选区矩形（像素坐标）
    private val region = RectF()

    // 遮罩画笔
    private val overlayPaint = Paint().apply {
        color = OVERLAY_COLOR
        style = Paint.Style.FILL
    }

    // 边框画笔
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BORDER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = BORDER_WIDTH_DP * resources.displayMetrics.density
    }

    // 角标画笔
    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BORDER_COLOR
        style = Paint.Style.STROKE
        strokeWidth = CORNER_WIDTH_DP * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    // 遮罩裁剪路径
    private val overlayPath = Path()

    private val touchSlop = TOUCH_SLOP_DP * resources.displayMetrics.density
    private val cornerLength = CORNER_LENGTH_DP * resources.displayMetrics.density

    // 拖拽模式
    private enum class DragMode {
        NONE, MOVE,
        LEFT, RIGHT, TOP, BOTTOM,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private var dragMode = DragMode.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // 区域变化回调，返回百分比值 (0.0-1.0)
    var onRegionChanged: ((RectF) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // 初始选区为整个 View 区域
            region.set(0f, 0f, w.toFloat(), h.toFloat())
            notifyRegionChanged()
        }
    }

    /** 设置选区（百分比值 0.0-1.0） */
    fun setRegion(percentRect: RectF) {
        if (width > 0 && height > 0) {
            region.set(
                percentRect.left * width,
                percentRect.top * height,
                percentRect.right * width,
                percentRect.bottom * height
            )
            invalidate()
        }
    }

    /** 重置选区为全屏 */
    fun resetToFullScreen() {
        region.set(0f, 0f, width.toFloat(), height.toFloat())
        notifyRegionChanged()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // 绘制遮罩：整个区域减去选区
        overlayPath.reset()
        overlayPath.addRect(0f, 0f, w, h, Path.Direction.CW)
        overlayPath.addRect(region, Path.Direction.CCW)
        canvas.drawPath(overlayPath, overlayPaint)

        // 绘制选区边框
        canvas.drawRect(region, borderPaint)

        // 绘制四角标记
        drawCorners(canvas)
    }

    private fun drawCorners(canvas: Canvas) {
        val l = region.left
        val t = region.top
        val r = region.right
        val b = region.bottom
        val len = cornerLength

        // 左上
        canvas.drawLine(l, t, l + len, t, cornerPaint)
        canvas.drawLine(l, t, l, t + len, cornerPaint)
        // 右上
        canvas.drawLine(r, t, r - len, t, cornerPaint)
        canvas.drawLine(r, t, r, t + len, cornerPaint)
        // 左下
        canvas.drawLine(l, b, l + len, b, cornerPaint)
        canvas.drawLine(l, b, l, b - len, cornerPaint)
        // 右下
        canvas.drawLine(r, b, r - len, b, cornerPaint)
        canvas.drawLine(r, b, r, b - len, cornerPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragMode = detectDragMode(x, y)
                if (dragMode == DragMode.NONE) return false
                lastTouchX = x
                lastTouchY = y
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastTouchX
                val dy = y - lastTouchY
                applyDrag(dx, dy)
                lastTouchX = x
                lastTouchY = y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragMode = DragMode.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
                notifyRegionChanged()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun detectDragMode(x: Float, y: Float): DragMode {
        val sl = touchSlop
        val nearLeft = Math.abs(x - region.left) < sl
        val nearRight = Math.abs(x - region.right) < sl
        val nearTop = Math.abs(y - region.top) < sl
        val nearBottom = Math.abs(y - region.bottom) < sl

        // 检查四角
        if (nearLeft && nearTop) return DragMode.TOP_LEFT
        if (nearRight && nearTop) return DragMode.TOP_RIGHT
        if (nearLeft && nearBottom) return DragMode.BOTTOM_LEFT
        if (nearRight && nearBottom) return DragMode.BOTTOM_RIGHT

        // 检查四边
        if (nearLeft && y > region.top && y < region.bottom) return DragMode.LEFT
        if (nearRight && y > region.top && y < region.bottom) return DragMode.RIGHT
        if (nearTop && x > region.left && x < region.right) return DragMode.TOP
        if (nearBottom && x > region.left && x < region.right) return DragMode.BOTTOM

        // 检查内部移动
        if (region.contains(x, y)) return DragMode.MOVE

        return DragMode.NONE
    }

    private fun applyDrag(dx: Float, dy: Float) {
        val minW = width * MIN_REGION_RATIO
        val minH = height * MIN_REGION_RATIO
        val w = width.toFloat()
        val h = height.toFloat()

        when (dragMode) {
            DragMode.MOVE -> {
                var offsetX = dx
                var offsetY = dy
                // 限制在 View 边界内
                if (region.left + offsetX < 0) offsetX = -region.left
                if (region.right + offsetX > w) offsetX = w - region.right
                if (region.top + offsetY < 0) offsetY = -region.top
                if (region.bottom + offsetY > h) offsetY = h - region.bottom
                region.offset(offsetX, offsetY)
            }
            DragMode.LEFT -> {
                region.left = (region.left + dx).coerceIn(0f, region.right - minW)
            }
            DragMode.RIGHT -> {
                region.right = (region.right + dx).coerceIn(region.left + minW, w)
            }
            DragMode.TOP -> {
                region.top = (region.top + dy).coerceIn(0f, region.bottom - minH)
            }
            DragMode.BOTTOM -> {
                region.bottom = (region.bottom + dy).coerceIn(region.top + minH, h)
            }
            DragMode.TOP_LEFT -> {
                region.left = (region.left + dx).coerceIn(0f, region.right - minW)
                region.top = (region.top + dy).coerceIn(0f, region.bottom - minH)
            }
            DragMode.TOP_RIGHT -> {
                region.right = (region.right + dx).coerceIn(region.left + minW, w)
                region.top = (region.top + dy).coerceIn(0f, region.bottom - minH)
            }
            DragMode.BOTTOM_LEFT -> {
                region.left = (region.left + dx).coerceIn(0f, region.right - minW)
                region.bottom = (region.bottom + dy).coerceIn(region.top + minH, h)
            }
            DragMode.BOTTOM_RIGHT -> {
                region.right = (region.right + dx).coerceIn(region.left + minW, w)
                region.bottom = (region.bottom + dy).coerceIn(region.top + minH, h)
            }
            DragMode.NONE -> {}
        }
    }

    private fun notifyRegionChanged() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        onRegionChanged?.invoke(
            RectF(
                region.left / w,
                region.top / h,
                region.right / w,
                region.bottom / h
            )
        )
    }
}
