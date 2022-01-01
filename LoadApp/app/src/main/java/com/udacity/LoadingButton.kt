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

    var arcOffset = 50f
    var arcDiameter = 80f

    private var widthSize = 0
    private var heightSize = 0

    private var textColor = 0
    private var buttonBackground = 0
    private var progressColor = 0
    private var progressArcColor = 0
    private var text = ""
    private var initialText = ""
    private var loadingText = ""

    private var progress = 0f
    private val progressAnimation = ValueAnimator()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.NORMAL)
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        if (old != new) {
            when (new) {
                ButtonState.Loading -> {
                    text = loadingText
                }
                ButtonState.Completed -> {
                    text = initialText
                    progress = 0f
                }
            }
            invalidate()
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackground = getColor(R.styleable.LoadingButton_buttonBackground, 0)
            progressColor = getColor(R.styleable.LoadingButton_progressColor, 0)
            progressArcColor = getColor(R.styleable.LoadingButton_progressArcColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)

            initialText = getString(R.styleable.LoadingButton_text) ?: ""
            loadingText = getString(R.styleable.LoadingButton_textWhileLoading) ?: ""
            text = initialText
        }
    }

    fun setProgress(newProgress: Float) {
        setProgress(newProgress, (newProgress / 100).toLong() * 500)
    }

    fun setProgress(newProgress: Float, duration: Long) {
        // Cancel previous animation
        buttonState = ButtonState.Loading

        progressAnimation.cancel()

        progressAnimation.setFloatValues(progress, newProgress)
        progressAnimation.duration = duration
        progressAnimation.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        progressAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (progress >= 100f) {
                    buttonState = ButtonState.Completed
                }
            }
        })
        progressAnimation.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = widthSize.toFloat() / 2
        val centerY = heightSize.toFloat() / 2

        paint.color = buttonBackground
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        paint.color = progressColor
        val progressRectWidth = widthSize.toFloat() * (progress / 100)
        canvas.drawRect(0f, 0f, progressRectWidth, heightSize.toFloat(), paint)

        paint.color = textColor
        canvas.drawText(
                text,
            centerX,
                (heightSize.toFloat() - paint.descent() - paint.ascent()) / 2,
                paint
        )

        if (progress > 0) {
            val textWidth = paint.measureText(text)
            val progressArcX = centerX + textWidth / 2 + arcOffset
            paint.color = progressArcColor
            paint.style = Paint.Style.FILL
            canvas.drawArc(
                progressArcX,
                centerY - arcDiameter / 2,
                progressArcX + arcDiameter,
                centerY + arcDiameter / 2,
                0f,
                360f * (progress / 100),
                true,
                paint
            )
        }
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