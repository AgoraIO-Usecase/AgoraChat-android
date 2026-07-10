package io.agora.chatdemo.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment

/**
 * As the base class for dialog fragments
 */
abstract class BaseDialogFragment : DialogFragment() {
    var mContext: Activity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initArgument()
        val view = inflater.inflate(layoutId, container, false)
        setChildView(view)
        setDialogAttrs()
        return view
    }

    open fun setChildView(view: View?) {}

    abstract val layoutId: Int
    private fun setDialogAttrs() {
        try {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initListener()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    open fun initArgument() {}
    open fun initView(savedInstanceState: Bundle?) {}
    open fun initListener() {}
    open fun initData() {}

    /**
     * To obtain the current view control through the ID, it needs to be called in the lifecycle after onViewCreated ()
     * @param id
     * @param <T>
     * @return
    </T> */
    protected fun <T : View?> findViewById(@IdRes id: Int): T? {
        return requireView().findViewById(id)
    }

    /**
     * Dialog width is full, height is customizable
     */
    fun setDialogParams() {
        try {
            val dialogWindow = dialog!!.window
            val lp = dialogWindow!!.attributes
            lp.dimAmount = 0.6f
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.BOTTOM
            setDialogParams(lp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Full screen dialog
     */
    fun setDialogFullParams() {
        val dialogHeight = getContextRect(mContext)
        val height = if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight
        setDialogParams(ViewGroup.LayoutParams.MATCH_PARENT, height, 0.0f)
    }

    open fun setDialogParams(width: Int, height: Int, dimAmount: Float) {
        try {
            val dialogWindow = dialog!!.window
            val lp = dialogWindow!!.attributes
            lp.dimAmount = dimAmount
            lp.width = width
            lp.height = height
            dialogWindow.attributes = lp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun setDialogParams(layoutParams: WindowManager.LayoutParams?) {
        try {
            val dialogWindow = dialog!!.window
            dialogWindow!!.attributes = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Get content area
    private fun getContextRect(activity: Activity?): Int {
        //application area
        val outRect1 = Rect()
        activity?.window?.decorView?.getWindowVisibleDisplayFrame(outRect1)
        return outRect1.height()
    }
}