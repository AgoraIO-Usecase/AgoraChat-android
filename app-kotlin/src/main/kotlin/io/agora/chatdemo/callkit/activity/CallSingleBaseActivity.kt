package io.agora.chatdemo.callkit.activity

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chat.callkit.ui.EaseCallSingleBaseActivity
import io.agora.chat.callkit.R
import io.agora.chatdemo.callkit.extensions.setFitSystemForTheme
import io.agora.uikit.common.utils.StatusBarCompat

class CallSingleBaseActivity: EaseCallSingleBaseActivity() {
    override fun initView() {
        setFitSystemForTheme(true)
        if (callType == EaseCallType.SINGLE_VIDEO_CALL) {
            StatusBarCompat.compat(this, Color.parseColor("#000000"))
        } else {
            StatusBarCompat.compat(this, Color.parseColor("#bbbbbb"))
        }
        super.initView()
        val rootLayout = findViewById<ConstraintLayout>(R.id.root_layout)
        rootLayout.setBackgroundColor(resources.getColor(io.agora.uikit.R.color.ease_neutral_10))
    }

}