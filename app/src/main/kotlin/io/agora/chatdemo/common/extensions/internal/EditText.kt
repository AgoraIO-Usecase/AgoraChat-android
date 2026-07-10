package io.agora.chatdemo.common.extensions.internal

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText

@SuppressLint("ClickableViewAccessibility")
internal fun EditText.changePwdDrawable(
    eyeOpen: Drawable?,
    eyeClose: Drawable?,
    left: Drawable?,
    top: Drawable?,
    bottom: Drawable?
) {
    //Can the identification password be seen
    val canBeSeen = booleanArrayOf(false)
    setOnTouchListener { v: View?, event: MotionEvent ->
        val drawable = compoundDrawables[2] ?: return@setOnTouchListener false
        //If there is no image on the right, it will not be processed anymore
        //If it is not a press event, no further processing
        if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false
        if (event.x > (width
                    - paddingRight
                    - drawable.intrinsicWidth)
        ) {
            if (canBeSeen[0]) {
                inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setCompoundDrawablesWithIntrinsicBounds(left, top, eyeClose, bottom)
                canBeSeen[0] = false
            } else {
                inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                setCompoundDrawablesWithIntrinsicBounds(left, top, eyeOpen, bottom)
                canBeSeen[0] = true
            }
            setSelection(text.toString().length)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            return@setOnTouchListener true
        }
        false
    }
}

/**
 * Show EditText's right drawable when text is not null.
 */
internal fun EditText.showRightDrawable(right: Drawable?) {
    val content = text.toString().trim { it <= ' ' }
    setCompoundDrawablesWithIntrinsicBounds(
        null,
        null,
        if (content.isEmpty()) null else right,
        null
    )
}

@SuppressLint("ClickableViewAccessibility")
internal fun EditText.clearEditTextListener() {
    setOnTouchListener { v: View?, event: MotionEvent ->
        val drawable = compoundDrawables[2] ?: return@setOnTouchListener false
        //如果右边没有图片，不再处理
        //如果不是按下事件，不再处理
        if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false
        if (event.x > (width
                    - paddingRight
                    - drawable.intrinsicWidth)
        ) {
            setText("")
            return@setOnTouchListener true
        }
        false
    }
}

/**
 * Implement a default text changed listener.
 */
internal fun EditText.addDefaultTextChangedListener(listener: (Editable?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // do nothing
        }

        override fun afterTextChanged(s: Editable?) {
            listener(s)
        }

    })
}