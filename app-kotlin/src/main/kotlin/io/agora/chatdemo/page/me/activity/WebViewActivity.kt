package io.agora.chatdemo.page.me.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebViewClient
import io.agora.chatdemo.databinding.DemoActivityWebviewBinding
import io.agora.uikit.base.EaseBaseActivity

class WebViewActivity : EaseBaseActivity<DemoActivityWebviewBinding>() {
    private var url = "https://www.agora.io/en/"

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityWebviewBinding? {
        return DemoActivityWebviewBinding.inflate(inflater)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ( intent.hasExtra(LOAD_TYPE) ){
            val type =  WebViewLoadType.from(intent.getIntExtra(LOAD_TYPE,0))
            if (type == WebViewLoadType.LocalHtml){
                url = "Html"
            } else if (type == WebViewLoadType.RemoteUrl){
                url = intent.getStringExtra(LOAD_URL)?:""
            }
        }
        binding.let {
            val webSettings = it.wbView.settings
            webSettings.javaScriptEnabled = true
            it.wbView.webViewClient = WebViewClient()
            it.wbView.loadUrl(url)
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
        }
    }

    companion object {
        private const val LOAD_TYPE = "webView_load_type"
        private const val LOAD_URL = "webView_url"
        fun actionStart(context: Context,type:WebViewLoadType,url:String?=null) {
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(LOAD_TYPE, type.ordinal)
                url?.let {
                    if (it.isNotEmpty()){
                        putExtra(LOAD_URL,it)
                    }
                }
                context.startActivity(this)
            }
        }
    }
}

enum class WebViewLoadType(val value:Int){
    RemoteUrl(0),
    LocalHtml(1),
    LocalUrl(2);

    companion object {
        fun from(value: Int): WebViewLoadType {
            val types = WebViewLoadType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return RemoteUrl
        }
    }
}