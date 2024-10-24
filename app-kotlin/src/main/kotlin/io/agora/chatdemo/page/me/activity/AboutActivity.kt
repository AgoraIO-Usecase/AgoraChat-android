package io.agora.chatdemo.page.me.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import io.agora.chatdemo.R
import io.agora.chatdemo.databinding.DemoActivityAboutBinding
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseActivity
import io.agora.uikit.common.ChatClient

class AboutActivity: EaseBaseActivity<DemoActivityAboutBinding>(), View.OnClickListener {
    companion object{
        const val Documentation = "https://"
        const val Platform = "/overview/product-overview?platform=web"
        const val BaseUrl = "https://www."
    }
    override fun getViewBinding(inflater: LayoutInflater): DemoActivityAboutBinding? {
        return DemoActivityAboutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    private fun initView(){
        binding.let {
            it.tvVersion.text = getString(R.string.about_version, ChatClient.VERSION)
            it.tvKitVersion.text = getString(R.string.about_uikit_version,EaseIM.version)
        }
    }

    private fun initListener(){
        binding.let {
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
            it.arrowItemDocumentation.setOnClickListener(this)
            it.arrowItemSales.setOnClickListener(this)
            it.arrowItemDemoRepo.setOnClickListener(this)
            it.arrowItemMore.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.arrow_item_documentation -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.RemoteUrl,"$Documentation${binding.arrowItemDocumentation.getSubTitle()}$Platform")
            }
            R.id.arrow_item_sales -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.RemoteUrl,"$BaseUrl${binding.arrowItemSales.getSubTitle()}")
            }
            R.id.arrow_item_demo_repo -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.RemoteUrl,"$BaseUrl${binding.arrowItemDemoRepo.getSubTitle()}")
            }
            R.id.arrow_item_more -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.RemoteUrl,"$BaseUrl${binding.arrowItemMore.getSubTitle()}")
            }
            else -> {}
        }
    }

}