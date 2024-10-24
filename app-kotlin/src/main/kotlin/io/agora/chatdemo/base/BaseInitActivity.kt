package io.agora.chatdemo.base

import android.content.Intent
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import io.agora.uikit.base.EaseBaseActivity

abstract class BaseInitActivity<B : ViewBinding> : EaseBaseActivity<B>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent(intent)
        initView(savedInstanceState)
        initListener()
        initData()
    }

    /**
     * init intent
     * @param intent
     */
    protected open fun initIntent(intent: Intent?) {}

    /**
     * init view
     * @param savedInstanceState
     */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /**
     * init listener
     */
    protected open fun initListener() {}

    /**
     * init data
     */
    protected open fun initData() {}
}