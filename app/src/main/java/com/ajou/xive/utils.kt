package com.ajou.xive

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.text.Layout
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat.startActivity
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineExceptionHandler
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

class OnSingleClickListener(
    private var interval: Int = 600,
    private var onSingleClick: (View) -> Unit
) : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if ((elapsedRealtime - lastClickTime) < interval) {
            return
        }
        lastClickTime = elapsedRealtime
        onSingleClick(v)
    }

}

fun View.setOnSingleClickListener(onSingleClick: (View) -> Unit) {
    val oneClick = OnSingleClickListener {
        onSingleClick(it)
    }
    setOnClickListener(oneClick)
}

fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.KOREAN)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.KOREAN)
}

fun ByteArray.toHexString(): String {
    val hexChars = "0123456789ABCDEF"
    val result = StringBuilder(size * 2)

    map { byte ->
        val value = byte.toInt()
        val hexChar1 = hexChars[value shr 4 and 0x0F]
        val hexChar2 = hexChars[value and 0x0F]
        result.append(hexChar1)
        result.append(hexChar2)
    }

    return result.toString()
}

class IndentLeadingMarginSpan(
    private val indentDelimiters: List<String> = INDENT_DELIMITERS
) : LeadingMarginSpan {

    private var indentMargin: Int = 0

    override fun getLeadingMargin(first: Boolean): Int = if (first) 0 else indentMargin

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, currentMarginLocation: Int, paragraphDirection: Int,
        lineTop: Int, lineBaseline: Int, lineBottom: Int, text: CharSequence, lineStart: Int,
        lineEnd: Int, isFirstLine: Boolean, layout: Layout
    ) {
        // New Line 일때만 체크
        if (!isFirstLine) {
            return
        }

        // 해당줄의 처음 2글자를 가져옴
        val lineStartText =
            runCatching { text.substring(lineStart, lineStart + 2) }.getOrNull() ?: return
        indentMargin =
            if (indentDelimiters.contains(lineStartText.trimEnd())) {
                paint.measureText(lineStartText).toInt()
            } else {
                0
            }
    }

    companion object {
        private val INDENT_DELIMITERS = listOf("·", "ㆍ", "-", "•","•")
    }
}

fun dpToPx(context: Context, dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
}

val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    Log.d("exception",exception.message.toString())
    val intent = Intent(App.context(), NetworkErrorActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    App.context().startActivity(intent)
}

fun ViewPager2.setCurrentItemWithDuration(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width // Default value taken from getWidth() from ViewPager2 view
) {
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }

    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator) {
            beginFakeDrag()
        }

        override fun onAnimationEnd(p0: Animator) {
            endFakeDrag()
        }

        override fun onAnimationCancel(p0: Animator) {
        }

        override fun onAnimationRepeat(p0: Animator) {
        }
    })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}