package io.agora.chatdemo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import io.agora.chatdemo.DemoApplication
import io.agora.chatdemo.R
import io.agora.uikit.EaseIM
import io.agora.uikit.common.extensions.dpToPx
import io.agora.uikit.common.extensions.mainScope
import kotlinx.coroutines.launch

/**
 * Toast工具类，统一Toast样式，处理重复显示的问题，处理7.1.x版本crash的问题
 */
object ToastUtils {
    private const val DEFAULT = 0
    private const val SUCCESS = 1
    private const val FAIL = 2
    private const val TOAST_LAST_TIME = 1000
    private var toast: Toast? = null
    private const val DISMISS = 2
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DISMISS -> synchronized(ToastUtils::class.java) {
                    if (toast != null) {
                        toast?.cancel()
                        toast = null
                    }
                }
            }
        }
    }

    /**
     * 弹出成功的toast
     * @param message
     */
    fun showSuccessToast(message: String?) {
        showCenterToast(null, message, SUCCESS, TOAST_LAST_TIME)
    }

    /**
     * 弹出成功的toast
     * @param message
     */
    fun showSuccessToast(@StringRes message: Int) {
        showCenterToast(0, message, SUCCESS, TOAST_LAST_TIME)
    }

    /**
     * 弹出失败的toast
     * @param message
     */
    fun showFailToast(message: String?) {
        showCenterToast(null, message, FAIL, TOAST_LAST_TIME)
    }

    /**
     * 弹出失败的toast
     * @param message
     */
    fun showFailToast(@StringRes message: Int) {
        showCenterToast(0, message, FAIL, TOAST_LAST_TIME)
    }

    /**
     * 弹出默认的toast
     * @param message
     */
    fun showToast(message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showBottomToast(null, message, DEFAULT, TOAST_LAST_TIME)
    }

    /**
     * 弹出默认的toast
     * @param message
     */
    fun showToast(@StringRes message: Int) {
        showBottomToast(0, message, DEFAULT, TOAST_LAST_TIME)
    }

    /**
     * 弹出成功的toast，有标题
     * @param title
     * @param message
     */
    fun showSuccessToast(title: String?, message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showCenterToast(title, message, SUCCESS, TOAST_LAST_TIME)
    }

    /**
     * 弹出成功的toast，有标题
     * @param title
     * @param message
     */
    fun showSuccessToast(@StringRes title: Int, @StringRes message: Int) {
        showCenterToast(title, message, SUCCESS, TOAST_LAST_TIME)
    }

    /**
     * 弹出失败的toast，有标题
     * @param title
     * @param message
     */
    fun showFailToast(title: String?, message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showCenterToast(title, message, FAIL, TOAST_LAST_TIME)
    }

    /**
     * 弹出失败的toast，有标题
     * @param title
     * @param message
     */
    fun showFailToast(@StringRes title: Int, @StringRes message: Int) {
        showCenterToast(title, message, FAIL, TOAST_LAST_TIME)
    }

    /**
     * 弹出成功的toast，有标题，可以设置显示时长
     * @param title
     * @param message
     * @param duration
     */
    fun showSuccessToast(title: String?, message: String?, duration: Int) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showCenterToast(title, message, SUCCESS, duration)
    }

    /**
     * 弹出成功的toast，有标题，可以设置显示时长
     * @param title
     * @param message
     * @param duration
     */
    fun showSuccessToast(@StringRes title: Int, @StringRes message: Int, duration: Int) {
        showCenterToast(title, message, SUCCESS, duration)
    }

    /**
     * 弹出失败的toast，有标题，可以设置显示时长
     * @param title
     * @param message
     * @param duration
     */
    fun showFailToast(title: String?, message: String?, duration: Int) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showCenterToast(title, message, FAIL, duration)
    }

    /**
     * 弹出失败的toast，有标题，可以设置显示时长
     * @param title
     * @param message
     * @param duration
     */
    fun showFailToast(@StringRes title: Int, @StringRes message: Int, duration: Int) {
        showCenterToast(title, message, FAIL, duration)
    }

    /**
     * 弹出toast，无图标，无标题，可以设置显示时长
     * @param message
     * @param duration
     */
    fun showToast(message: String?, duration: Int) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showCenterToast(null, message, DEFAULT, duration)
    }

    /**
     * 弹出toast，无图标，无标题，可以设置显示时长
     * @param message
     * @param duration
     */
    fun showToast(@StringRes message: Int, duration: Int) {
        showCenterToast(0, message, DEFAULT, duration)
    }

    /**
     * 在屏幕中部显示，在此处传入application
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    fun showCenterToast(title: String?, message: String?, type: Int, duration: Int) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showToast(EaseIM.getContext(), title, message, type, duration, Gravity.CENTER)
    }

    /**
     * 在屏幕中部显示，在此处传入application
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    fun showCenterToast(@StringRes title: Int, @StringRes message: Int, type: Int, duration: Int) {
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.CENTER)
    }

    /**
     * 在屏幕底部显示，在此处传入application
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    fun showBottomToast(title: String?, message: String?, type: Int, duration: Int) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.BOTTOM)
    }

    /**
     * 在屏幕底部显示，在此处传入application
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    fun showBottomToast(@StringRes title: Int, @StringRes message: Int, type: Int, duration: Int) {
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.BOTTOM)
    }

    /**
     * 此处判断toast不为空，选择cancel，是因为toast因为类型不同（是否显示图片）或者是否有标题，会导致不同的toast展示
     * @param context
     * @param title
     * @param message
     * @param type
     * @param duration
     * @param gravity
     */
    fun showToast(
        context: Context?,
        @StringRes title: Int,
        @StringRes message: Int,
        type: Int,
        duration: Int,
        gravity: Int
    ) {
        showToast(
            context,
            if (title == 0) null else context?.getString(title),
            context?.getString(message),
            type,
            duration,
            gravity
        )
    }

    /**
     * 此处判断toast不为空，选择cancel，是因为toast因为类型不同（是否显示图片）或者是否有标题，会导致不同的toast展示
     * @param context
     * @param title
     * @param message
     * @param type
     * @param duration
     * @param gravity
     */
    fun showToast(
        context: Context?,
        title: String?,
        message: String?,
        type: Int,
        duration: Int,
        gravity: Int
    ) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        //保证在主线程中展示toast
        context?.mainScope()?.launch {
            synchronized(ToastUtils::class.java) {
                if (toast != null) {
                    toast?.cancel()
                }
                toast = getToast(context, title, message, type, duration, gravity)
                toast?.show()
                handler.removeCallbacksAndMessages(null)
                handler.sendEmptyMessageDelayed(DISMISS, 2000)
            }
        }
    }

    private fun getToast(
        context: Context?,
        title: String?,
        message: String?,
        type: Int,
        duration: Int,
        gravity: Int
    ): Toast {
        val toast = Toast(context)
        val toastView: View = LayoutInflater.from(context).inflate(R.layout.demo_toast_layout, null)
        toast.setView(toastView)
        val ivToast = toastView.findViewById<ImageView>(R.id.iv_toast)
        val tvToastTitle = toastView.findViewById<TextView>(R.id.tv_toast_title)
        val tvToastContent = toastView.findViewById<TextView>(R.id.tv_toast_content)
        if (TextUtils.isEmpty(title)) {
            tvToastTitle.visibility = View.GONE
        } else {
            tvToastTitle.visibility = View.VISIBLE
            tvToastTitle.text = title
        }
        if (!TextUtils.isEmpty(message)) {
            tvToastContent.text = message
        }
        ivToast.visibility = View.VISIBLE
        if (type == SUCCESS) {
            ivToast.setImageResource(R.drawable.em_toast_success)
        } else if (type == FAIL) {
            ivToast.setImageResource(R.drawable.em_toast_fail)
        } else {
            ivToast.visibility = View.GONE
        }
        var yOffset = 0
        if (gravity == Gravity.BOTTOM || gravity == Gravity.TOP) {
            yOffset = 50.dpToPx(context!!)
        }
        toast.duration = duration
        toast.setGravity(gravity, 0, yOffset)
        hookToast(toast)
        return toast
    }

    /**
     * 为了解决7.1.x版本toast可以导致crash的问题
     * @param toast
     */
    @SuppressLint("SoonBlockedPrivateApi")
    private fun hookToast(toast: Toast) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return
        }
        val cToast: Class<Toast> = Toast::class.java
        try {
            //TN是private的
            val fTn = cToast.getDeclaredField("mTN")
            fTn.isAccessible = true

            //获取tn对象
            val oTn = fTn[toast]
            //获取TN的class，也可以直接通过Field.getType()获取。
            val cTn: Class<*> = oTn.javaClass
            val fHandle = cTn.getDeclaredField("mHandler")

            //重新set->mHandler
            fHandle.isAccessible = true
            fHandle[oTn] = HandlerProxy(fHandle[oTn] as Handler)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private class HandlerProxy(private val mHandler: Handler) : Handler() {
        override fun handleMessage(msg: Message) {
            try {
                mHandler.handleMessage(msg)
            } catch (e: WindowManager.BadTokenException) {
                //ignore
            }
        }
    }
}