package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private var textColor = 0
    private var buttonBackground = 0
    private var progressColor = 0

    private var progress = 0f
    private var progressMilestone = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.NORMAL)
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackground = getColor(R.styleable.LoadingButton_buttonBackground, 0)
            progressColor = getColor(R.styleable.LoadingButton_progressColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
        }
    }

    fun reset() {
        progress = 0f
        progressMilestone = 0f
    }

    fun setProgress(progress: Float) {
        setProgress(progress, (progress / 100).toLong() * 500)
    }

    fun setProgress(progress: Float, duration: Long) {
        val anim = ValueAnimator.ofFloat(progressMilestone, progress)
        anim.duration = duration
        anim.addUpdateListener {
            this.progress = it.animatedValue as Float
            invalidate()
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                progressMilestone = progress
            }
        })
        anim.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = buttonBackground
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        paint.color = progressColor
        val progressRectWidth = widthSize.toFloat() * (progress / 100)
        canvas.drawRect(0f, 0f, progressRectWidth, heightSize.toFloat(), paint)

        paint.color = textColor
        canvas.drawText(
                resources.getString(R.string.download),
                widthSize.toFloat() / 2,
                (heightSize.toFloat() - paint.descent() - paint.ascent()) / 2,
                paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}