package com.ajou.xive

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
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
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
    val intent = Intent(App.context(), NetworkErrorActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    App.context().startActivity(intent)
}

fun byteArrayToStringWithNDEF(byteArray: ByteArray): String {
    if (byteArray.isEmpty()) {
        return ""
    }

    // 첫 번째 바이트는 상태 바이트
    val statusByte = byteArray[0].toInt()

    // 상태 바이트의 하위 5비트는 언어 코드의 길이를 나타냄
    val languageCodeLength = statusByte and 0x3F

    // 실제 텍스트 데이터는 언어 코드 다음에 위치
    return String(
        byteArray,
        languageCodeLength + 1,
        byteArray.size - languageCodeLength - 1,
        Charsets.UTF_8
    )
}