package io.agora.chatdemo.page.splash

import android.animation.Animator
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.MainActivity
import io.agora.chatdemo.R
import io.agora.chatdemo.base.BaseInitActivity
import io.agora.chatdemo.common.dialog.SimpleDialog
import io.agora.chatdemo.common.dialog.fragment.DemoAgreementDialogFragment
import io.agora.chatdemo.common.dialog.fragment.DemoDialogFragment
import io.agora.chatdemo.databinding.DemoSplashActivityBinding
import io.agora.chatdemo.page.login.LoginActivity
import io.agora.chatdemo.page.splash.viewModel.SplashViewModel
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.extensions.catchChatException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SplashActivity : BaseInitActivity<DemoSplashActivityBinding>() {
    private lateinit var model: SplashViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoSplashActivityBinding? {
        return DemoSplashActivityBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.ivSplash.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    checkIfAgreePrivacy()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
        binding.tvProduct.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun checkIfAgreePrivacy() {
        checkSDKValid()
    }

    private fun checkSDKValid() {
        if (DemoHelper.getInstance().hasAppKey.not()) {
            showAlertDialog(R.string.splash_not_appkey)
        } else {
            if (DemoHelper.getInstance().isSDKInited().not()) {
                showAlertDialog(R.string.splash_not_init)
            } else {
                loginSDK()
            }
        }
    }

    private fun showAlertDialog(@StringRes title: Int) {
        SimpleDialog.Builder(mContext)
            .setTitle(getString(title))
            .setPositiveButton(getString(R.string.confirm)) {
                exitProcess(1)
            }
            .dismissNegativeButton()
            .show()
    }

    private fun loginSDK() {
        lifecycleScope.launch {
            model.loginData()
                .catchChatException { e ->
                    ChatLog.e("TAG", "error message = " + e.description)
                    LoginActivity.startAction(mContext)
                    finish()
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), false)
                .collect {
                    if (it) {
                        DemoHelper.getInstance().getDataModel().initDb()
                        startActivity(Intent(mContext, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }
}