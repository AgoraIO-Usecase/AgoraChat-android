package io.agora.chatdemo.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import io.agora.uikit.databinding.EaseLayoutCustomDialogBinding

class SimpleDialog(
    context: Context,
    private var title: String? = "",
    private var subtitle: String? = "",
    private var isEditTextMode: Boolean = false,
    private var inputHint: String? = "",
    private var onNegativeButtonClickListener: (() -> Unit)? = {},
    private var onPositiveButtonClickListener: (() -> Unit)? = {},
    private var onInputTextChangeListener: ((String) -> Unit)? = {},
    private var onInputModeConfirmListener: ((String) -> Unit)? = {},
) : Dialog(context) {

    private var mPositiveButtonText: String? = null
    private var mNegativeButtonText: String? = null
    private var showCancelButton: Boolean = true
    private var showConfirmButton: Boolean = true
    private val binding by lazy { EaseLayoutCustomDialogBinding.inflate(LayoutInflater.from(context)) }

    init {
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setData()

        binding.leftButton.setOnClickListener {
            onNegativeButtonClickListener?.invoke()
            dismiss()
        }

        binding.rightButton.setOnClickListener {
            if (isEditTextMode){
                if (binding.editText.text.isNotEmpty()){
                    onInputModeConfirmListener?.invoke(binding.editText.text.toString())
                }
            }else{
                onPositiveButtonClickListener?.invoke()
            }
            dismiss()
        }
    }

    fun setData() {
        if (mPositiveButtonText.isNullOrEmpty().not()) {
            binding.rightButton.text = mPositiveButtonText
        }
        if (mNegativeButtonText.isNullOrEmpty().not()) {
            binding.leftButton.text = mNegativeButtonText
        }
        if (title.isNullOrEmpty()) {
            binding.titleTextView.visibility = View.GONE
        } else {
            binding.titleTextView.text = title
            binding.titleTextView.visibility = View.VISIBLE
        }

        if (subtitle.isNullOrEmpty()) {
            binding.subtitleTextView.visibility = View.GONE
        } else {
            binding.subtitleTextView.text = subtitle
            binding.subtitleTextView.visibility = View.VISIBLE
        }
        if (isEditTextMode) {
            binding.editText.requestFocus()
            binding.inputClear.setOnClickListener{
                binding.editText.setText("")
            }
            binding.editText.visibility = View.VISIBLE
            binding.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s.isEmpty()){
                        binding.inputClear.visibility = View.GONE
                        binding.rightButton.isSelected = false
                    }else{
                        binding.inputClear.visibility = View.VISIBLE
                        binding.rightButton.isSelected = true
                    }
                    onInputTextChangeListener?.invoke(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        } else {
            binding.editText.visibility = View.GONE
            binding.rightButton.isSelected = true
            binding.rightButton.visibility = View.VISIBLE
        }
        binding.leftButton.visibility = if (showCancelButton) View.VISIBLE else View.GONE
        binding.rightButton.visibility = if (showConfirmButton) View.VISIBLE else View.GONE
    }

    class Builder(context: Context) {
        private val dialog = SimpleDialog(context)

        /**
         * Set the dialog title.
         */
        fun setTitle(title: String?): Builder {
            dialog.title = title
            return this
        }

        /**
         * Set the dialog subtitle.
         */
        fun setSubtitle(subtitle: String?): Builder {
            dialog.subtitle = subtitle
            return this
        }

        /**
         * Set the dialog mode.
         */
        fun setEditTextMode(isEditTextMode: Boolean): Builder {
            dialog.isEditTextMode = isEditTextMode
            return this
        }

        /**
         * Set the dialog input hint when the dialog model is edit.
         */
        fun setInputHint(inputHint: String?): Builder {
            dialog.inputHint = inputHint
            return this
        }

        /**
         * Dismiss the dialog positive button.
         */
        fun dismissPositiveButton(): Builder {
            dialog.showConfirmButton = false
            return this
        }

        /**
         * Dismiss the dialog negative button.
         */
        fun dismissNegativeButton(): Builder {
            dialog.showCancelButton = false
            return this
        }

        /**
         * Set the dialog positive button.
         */
        fun setPositiveButton(text: String? = "", onClickListener: () -> Unit): Builder {
            dialog.mPositiveButtonText = text
            dialog.onPositiveButtonClickListener = onClickListener
            return this
        }

        /**
         * Set the dialog negative button.
         */
        fun setNegativeButton(text: String? = "", onClickListener: () -> Unit): Builder {
            dialog.mNegativeButtonText = text
            dialog.onNegativeButtonClickListener = onClickListener
            return this
        }

        /**
         * Set the dialog dismiss listener.
         */
        fun setOnDismissListener(onDismissListener: () -> Unit): Builder {
            dialog.setOnDismissListener {
                onDismissListener.invoke()
            }
            return this
        }

        /**
         * Set the dialog cancelable.
         */
        fun setCancelable(cancelable: Boolean): Builder {
            dialog.setCancelable(cancelable)
            return this
        }

        /**
         * Set the dialog canceled on touch outside.
         */
        fun setCanceledOnTouchOutside(canceledOnTouchOutside: Boolean): Builder {
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            return this
        }

        /**
         * Set the dialog positive button text color.
         */
        fun setPositiveButtonTextColor(@ColorInt color: Int): Builder {
            dialog.binding.rightButton.setTextColor(color)
            return this
        }

        /**
         * Set the dialog negative button text color.
         */
        fun setNegativeButtonTextColor(@ColorInt color: Int): Builder {
            dialog.binding.leftButton.setTextColor(color)
            return this
        }

        /**
         * Set the dialog input text change listener when the dialog is in edit model.
         */
        fun setOnInputTextChangeListener(onInputTextChangeListener: (String) -> Unit): Builder {
            dialog.onInputTextChangeListener = onInputTextChangeListener
            return this
        }

        /**
         * Set the dialog input mode confirm listener when the dialog is in edit model.
         */
        fun setOnInputModeConfirmListener(onInputModeConfirmListener: (String) -> Unit): Builder {
            dialog.onInputModeConfirmListener = onInputModeConfirmListener
            return this
        }

        /**
         * Create the dialog.
         */
        fun build(): SimpleDialog {
            dialog.setData()
            return dialog
        }

        /**
         * Show the dialog.
         */
        fun show() {
            build()
            dialog.show()
        }
    }
}