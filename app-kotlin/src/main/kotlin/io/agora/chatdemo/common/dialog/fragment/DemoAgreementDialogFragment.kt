package io.agora.chatdemo.common.dialog.fragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.agora.chatdemo.R

class DemoAgreementDialogFragment : DemoDialogFragment() {
    override val middleLayoutId: Int
        get() = R.layout.demo_fragment_middle_agreement

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val tv_privacy = findViewById<TextView>(R.id.tv_privacy)
        tv_privacy?.text = spannable
        tv_privacy?.movementMethod = LinkMovementMethod.getInstance()
        mBtnDialogConfirm?.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary))
    }

    override fun initData() {
        super.initData()
        if (dialog != null) {
            dialog!!.setCancelable(false)
            dialog!!.setCanceledOnTouchOutside(false)
        }
    }

    private val spannable: SpannableString
        get() {
            val spanStr = SpannableString(getString(R.string.demo_login_dialog_content_privacy))
            val start1 = 18
            val end1 = 25
            val start2 = 30
            val end2 = 44
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToAgreement()
                }
            }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_primary)),
                start1,
                end1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToProtocol()
                }
            }, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_primary)),
                start2,
                end2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spanStr
        }

    private fun jumpToAgreement() {
        val uri = Uri.parse("http://www.easemob.com/agreement")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private fun jumpToProtocol() {
        val uri = Uri.parse("http://www.easemob.com/protocol")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private abstract inner class MyClickableSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.bgColor = Color.TRANSPARENT
        }
    }

    open class Builder(context: AppCompatActivity) : DemoDialogFragment.Builder(context) {
        override val fragment: DemoDialogFragment
            protected get() = DemoAgreementDialogFragment()
    }
}