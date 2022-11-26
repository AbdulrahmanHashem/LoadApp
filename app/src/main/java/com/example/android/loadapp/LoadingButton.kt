package com.example.android.loadapp

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    val valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new){
            ButtonState.Loading -> loadingAnimationAnimator(0, 80,8000)
            ButtonState.Completed -> loadingAnimationAnimator(progress, 100, 500)
            else -> {}
        }
    }

    var progress = 0
        set(value) {
            field = value
            // invalidate view on progress change
            invalidate()
        }

    var buttonText = "Download"

    private val rectF = RectF(0f, 5f, 0f, 80f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var textColor = 0
    private var loadingColor = 0
    private var loadingCircleColor = 0
    private var buttonBackgroundColor = 0

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton){
            textColor = getColor(R.styleable.LoadingButton_textColor, Color.WHITE)
            loadingColor = getColor(R.styleable.LoadingButton_loadingColor, Color.GREEN)
            loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, Color.YELLOW)
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, Color.GRAY)
        }
    }

    private fun loadingAnimationAnimator(value1: Int, value2: Int, duration: Long) {
        valueAnimator.addUpdateListener { animation ->
            this.progress = animation.animatedValue as Int
        }
        valueAnimator.setIntValues(value1, value2)
        valueAnimator.duration = duration
        buttonText = "We Are Loading"
        valueAnimator.start()

        valueAnimator.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator) {
                if (buttonState == ButtonState.Completed){
                    progress = 0
                    buttonText = "Download"
                }
            }
        })
    }

    override fun performClick(): Boolean {
        buttonState = ButtonState.Clicked
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(buttonBackgroundColor)
        canvas.drawText(buttonText, measuredWidth/2f, (measuredHeight/2f) + 20f,
            paint.apply {
                color = textColor
                alpha = 1000
            })

        canvas.drawArc(rectF.apply {
            left = (measuredWidth/2f) + 200f
            right = (measuredWidth/2f) + 280f
            top = (measuredHeight/2f) - 40f
            bottom = (measuredHeight/2f) + 40f
                                   },
            0f, (3.6 * progress).toFloat(),true, paint.apply { color = loadingCircleColor})

        canvas.drawRect(0f, 0f, (measuredWidth/100f) * progress ,
            measuredHeight.toFloat(),
            paint.apply {
                color = loadingColor
                alpha = 60
            }
        )

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec,0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}