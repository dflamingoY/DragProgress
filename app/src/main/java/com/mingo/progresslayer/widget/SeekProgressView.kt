package com.mingo.progresslayer.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ProgressBar
import com.mingo.progresslayer.R

/**
 * 绘制一个可以拖动的带遮罩层的progress,点击跳动到制定位置, 也可以拖动,比较适合播放音频, 拖动进度
 */
class SeekProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {
    private var isIntercepted = false
    private var maskDraw: Drawable
    private val startColor = Color.parseColor("#33FFFFFF")
    private val endColor = Color.parseColor("#4dFFFFFF")
    private var radius = 0f

    /**
     * 绘制背景可无
     */
    private val paintBg = Paint().also {
        it.isDither = true
        it.isAntiAlias = true
    }

    /**
     * 裁剪圆形的背景
     */
    private val paintBounds = Paint().also {
        it.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    /**
     * 绘制layer
     */
    private val paintMask = Paint().also {
        it.isDither = true
        it.isAntiAlias = true
    }

    /**
     * 绘制进度的边界线
     */
    private val paintLine = Paint().also {
        it.color = Color.parseColor("#7cffffff")
        it.isDither = true
        it.isAntiAlias = true
    }
    private var isDrag = false
    private var isPressing = false
    private var touchEnable = true
    private var detector: GestureDetector

    init {
        val arrays =
            context.obtainStyledAttributes(attrs, R.styleable.SeekProgressView)
        val maskColor = arrays.getColor(R.styleable.SeekProgressView_mask_color, 0)
        isIntercepted = arrays.getBoolean(R.styleable.SeekProgressView_progress_intercepted, true)
        radius = arrays.getDimension(R.styleable.SeekProgressView_wrap_radius, 0f)
        arrays.recycle()
        paintBg.color = maskColor
        maskDraw = GradientDrawable().also {
            it.cornerRadius = radius
            it.setColor(maskColor)
        }
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                isDrag = true
                isPressing = true
                parent.requestDisallowInterceptTouchEvent(true)
                setProgress(
                    ((progress.toFloat() / max * measuredWidth - distanceX) / measuredWidth * max).toInt(),
                    true
                )
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return if (isIntercepted)
                    performClick()
                else false
            }


            override fun onLongPress(e: MotionEvent?) {
                performLongClick()
            }
        })
    }

    override fun setProgress(progress: Int, update: Boolean) {
        if (update)
            super.setProgress(progress)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val shade = LinearGradient(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            startColor,
            endColor,
            Shader.TileMode.MIRROR
        )
        paintMask.shader = shade
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(
            0f,
            0f,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            radius,
            radius,
            paintBg
        )
        canvas?.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
        canvas?.let {
            maskDraw.setBounds(0, 0, measuredWidth, measuredHeight)
            maskDraw.draw(canvas)
        }
        canvas?.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintBounds)
        canvas?.drawRoundRect(
            0f,
            0f,
            progress.toFloat() / max * measuredWidth,
            measuredHeight.toFloat(),
            0f,
            0f,
            paintMask
        )
        canvas?.drawRect(
            progress.toFloat() / max * measuredWidth - 3,
            0f,
            progress.toFloat() / 1000 * measuredWidth,
            measuredHeight.toFloat(),
            paintLine
        )
        canvas?.restore()
        canvas?.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!touchEnable) {
            return super.onTouchEvent(event)
        }
        detector.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            if (!isDrag) {
//            } else {
                if (isIntercepted) {
                    val percent = event.x / width
                    setProgress((percent * max).toInt(), true)
                }
            }
            isDrag = false
            isPressed = false
        }
        return true
    }

    /**
     * 是否可以点击或者拖动, 列表中做拦截处理
     */
    fun isTouched(isTouch: Boolean) {
        touchEnable = isTouch
    }


}