package io.agora.chatdemo.common.dialog.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import io.agora.chatdemo.R
import io.agora.chatdemo.base.BaseDialogFragment
import io.agora.uikit.common.extensions.dpToPx

open class DemoDialogFragment : BaseDialogFragment(), View.OnClickListener {
    var mTvDialogTitle: TextView? = null
    var mBtnDialogCancel: Button? = null
    var mBtnDialogConfirm: Button? = null
    var mOnConfirmClickListener: OnConfirmClickListener? = null
    var mOnCancelClickListener: OnCancelClickListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null
    var mGroupMiddle: Group? = null
    var title: String? = null
    var content: String? = null
    override val layoutId: Int
        get() = R.layout.demo_fragment_dialog_base

    override fun setChildView(view: View?) {
        super.setChildView(view)
        val layoutId = middleLayoutId
        if (layoutId > 0) {
            view?.findViewById<RelativeLayout>(R.id.rl_dialog_middle)?.let {
                LayoutInflater.from(mContext).inflate(layoutId, it)
                view.findViewById<View>(R.id.group_middle)?.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //宽度填满，高度自适应
        try {
            dialog?.window?.let {
                val lp: WindowManager.LayoutParams = it.attributes
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                it.attributes = lp
            }
            requireView().let {
                val params = it.layoutParams
                if (params is FrameLayout.LayoutParams) {
                    val margin = 30.dpToPx(it.context)
                    params.setMargins(margin, 0, margin, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAllowingStateLoss(transaction: FragmentTransaction, tag: String?): Int {
        try {
            val dismissed = DemoDialogFragment::class.java.getDeclaredField("mDismissed")
            dismissed.isAccessible = true
            dismissed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            val shown = DemoDialogFragment::class.java.getDeclaredField("mShownByMe")
            shown.isAccessible = true
            shown[this] = true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        transaction.add(this, tag)
        try {
            val viewDestroyed = DemoDialogFragment::class.java.getDeclaredField("mViewDestroyed")
            viewDestroyed.isAccessible = true
            viewDestroyed[this] = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val mBackStackId = transaction.commitAllowingStateLoss()
        try {
            val backStackId = DemoDialogFragment::class.java.getDeclaredField("mBackStackId")
            backStackId.isAccessible = true
            backStackId[this] = mBackStackId
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return mBackStackId
    }

    open val middleLayoutId: Int
        /**
         * 获取中间布局的id
         * @return
         */
        get() = 0

    override fun initView(savedInstanceState: Bundle?) {
        mTvDialogTitle = findViewById(R.id.tv_dialog_title)
        mBtnDialogCancel = findViewById(R.id.btn_dialog_cancel)
        mBtnDialogConfirm = findViewById(R.id.btn_dialog_confirm)
        mGroupMiddle = findViewById(R.id.group_middle)
        arguments?.let { bundle ->
            title = bundle.getString(ParameterName.titleString)
            if (!TextUtils.isEmpty(title)) {
                mTvDialogTitle?.text = title
            }
            content = bundle.getString(ParameterName.contentString)
            val titleColor = bundle.getInt(ParameterName.titleColorInt, 0)
            if (titleColor != 0) {
                mTvDialogTitle?.setTextColor(titleColor)
            }
            val titleSize = bundle.getInt(ParameterName.titleSize, 0)
            if (titleSize != 0) {
                mTvDialogTitle?.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize.toFloat())
            }
            val confirm = bundle.getString(ParameterName.confirmString)
            if (!TextUtils.isEmpty(confirm)) {
                mBtnDialogConfirm?.text = confirm
            }
            val confirmColor = bundle.getInt(ParameterName.confirmColorInt, 0)
            if (confirmColor != 0) {
                mBtnDialogConfirm?.setTextColor(confirmColor)
            }
            val cancel = bundle.getString(ParameterName.cancelString)
            if (!TextUtils.isEmpty(cancel)) {
                mBtnDialogCancel?.text = cancel
            }
            val showCancel = bundle.getBoolean(ParameterName.showCancel, false)
            if (showCancel) {
                mGroupMiddle?.visibility = View.VISIBLE
            }
            val canceledOnTouchOutside =
                bundle.getBoolean(ParameterName.canceledOnTouchOutside, false)
            dialog?.setCanceledOnTouchOutside(canceledOnTouchOutside)
        }
    }

    override fun initListener() {
        mBtnDialogCancel?.setOnClickListener(this)
        mBtnDialogConfirm?.setOnClickListener(this)
    }

    override fun initData() {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_dialog_cancel -> onCancelClick(v)
            R.id.btn_dialog_confirm -> onConfirmClick(v)
        }
    }

    override fun dismiss() {
        super.dismiss()
        dismissListener?.onDismiss(dialog)
    }

    /**
     * 设置确定按钮的点击事件
     * @param listener
     */
    fun setOnConfirmClickListener(listener: OnConfirmClickListener?) {
        mOnConfirmClickListener = listener
    }

    /**
     * 设置取消事件
     * @param cancelClickListener
     */
    fun setOnCancelClickListener(cancelClickListener: OnCancelClickListener?) {
        mOnCancelClickListener = cancelClickListener
    }

    private fun setOnDismissListener(dismissListener: DialogInterface.OnDismissListener?) {
        this.dismissListener = dismissListener
    }

    /**
     * 点击了取消按钮
     * @param v
     */
    fun onCancelClick(v: View?) {
        dismiss()
        mOnCancelClickListener?.onCancelClick(v)
    }

    /**
     * 点击了确认按钮
     * @param v
     */
    fun onConfirmClick(v: View?) {
        dismiss()
        mOnConfirmClickListener?.onConfirmClick(v)
    }

    /**
     * 确定事件的点击事件
     */
    interface OnConfirmClickListener {
        fun onConfirmClick(view: View?)
    }

    /**
     * 点击取消
     */
    interface OnCancelClickListener {
        fun onCancelClick(view: View?)
    }

    open class Builder(private val context: AppCompatActivity) {
        private var listener: OnConfirmClickListener? = null
        private var cancelClickListener: OnCancelClickListener? = null
        private var dismissListener: DialogInterface.OnDismissListener? = null
        private val currentFragment: DemoDialogFragment? = null
        protected val bundle: Bundle = Bundle()

        fun setTitle(@StringRes title: Int): Builder {
            bundle.putString(ParameterName.titleString, context.getString(title))
            return this
        }

        fun setTitle(title: String?): Builder {
            bundle.putString(ParameterName.titleString, title)
            return this
        }

        fun setTitleColor(@ColorRes color: Int): Builder {
            bundle.putInt(ParameterName.titleColorInt, ContextCompat.getColor(context, color))
            return this
        }

        fun setTitleColorInt(@ColorInt color: Int): Builder {
            bundle.putInt(ParameterName.titleColorInt, color)
            return this
        }

        fun setTitleSize(size: Float): Builder {
            bundle.putFloat(ParameterName.titleSize, size)
            return this
        }

        fun setContent(@StringRes content: Int): Builder {
            bundle.putString(ParameterName.contentString, context.getString(content))
            return this
        }

        fun setContent(content: String?): Builder {
            bundle.putString(ParameterName.contentString, content)
            return this
        }

        fun showCancelButton(showCancel: Boolean): Builder {
            bundle.putBoolean(ParameterName.showCancel, showCancel)
            return this
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): Builder {
            bundle.putBoolean(ParameterName.canceledOnTouchOutside, cancel)
            return this
        }

        fun setOnConfirmClickListener(
            @StringRes confirm: Int,
            listener: OnConfirmClickListener?
        ): Builder {
            bundle.putString(ParameterName.confirmString, context.getString(confirm))
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(
            confirm: String?,
            listener: OnConfirmClickListener?
        ): Builder {
            bundle.putString(ParameterName.confirmString, confirm)
            this.listener = listener
            return this
        }

        fun setOnConfirmClickListener(listener: OnConfirmClickListener?): Builder {
            this.listener = listener
            return this
        }

        fun setConfirmColor(@ColorRes color: Int): Builder {
            bundle.putInt(ParameterName.confirmColorInt, ContextCompat.getColor(context, color))
            return this
        }

        fun setConfirmColorInt(@ColorInt color: Int): Builder {
            bundle.putInt(ParameterName.confirmColorInt, color)
            return this
        }

        fun setOnCancelClickListener(
            @StringRes cancel: Int,
            listener: OnCancelClickListener?
        ): Builder {
            bundle.putString(ParameterName.cancelString, context.getString(cancel))
            cancelClickListener = listener
            return this
        }

        fun setOnCancelClickListener(cancel: String?, listener: OnCancelClickListener?): Builder {
            bundle.putString(ParameterName.cancelString, cancel)
            cancelClickListener = listener
            return this
        }

        fun setOnCancelClickListener(listener: OnCancelClickListener?): Builder {
            cancelClickListener = listener
            return this
        }

        fun setOnDismissListener(listener: DialogInterface.OnDismissListener?): Builder {
            dismissListener = listener
            return this
        }

        fun setArgument(bundle: Bundle?): Builder {
            if (bundle != null) {
                this.bundle.putAll(bundle)
            }
            return this
        }

        fun build(): DemoDialogFragment {
            val fragment = fragment
            fragment.setOnConfirmClickListener(listener)
            fragment.setOnCancelClickListener(cancelClickListener)
            fragment.setOnDismissListener(dismissListener)
            fragment.setArguments(bundle)
            return fragment
        }

        protected open val fragment: DemoDialogFragment
            protected get() = DemoDialogFragment()

        fun show(): DemoDialogFragment {
            val fragment = build()
            val transaction: FragmentTransaction =
                context.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.showAllowingStateLoss(transaction, null)
            return fragment
        }
    }

    private object ParameterName {
        const val titleString = "titleString"
        const val titleColorInt = "titleColorInt"
        const val titleSize = "titleSize"
        const val contentString = "contentString"
        const val showCancel = "showCancel"
        const val canceledOnTouchOutside = "canceledOnTouchOutside"
        const val confirmString = "confirmString"
        const val confirmColorInt = "confirmColorInt"
        const val cancelString = "cancelString"
    }
}