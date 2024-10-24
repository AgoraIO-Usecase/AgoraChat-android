package io.agora.chatdemo.callkit.activity

import android.graphics.Color
import io.agora.chat.callkit.ui.EaseCallMultipleBaseActivity
import io.agora.chatdemo.callkit.extensions.setFitSystemForTheme
import io.agora.uikit.common.utils.StatusBarCompat

class CallMultipleBaseActivity : EaseCallMultipleBaseActivity() {
    override fun initView() {
        setFitSystemForTheme(true)
        StatusBarCompat.compat(this, Color.parseColor("#858585"))
        super.initView()
    }
}