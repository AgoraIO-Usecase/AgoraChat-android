package io.agora.chatdemo.page.login.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.MainActivity
import io.agora.chatdemo.R
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.dialog.SimpleDialog
import io.agora.chatdemo.common.extensions.internal.changePwdDrawable
import io.agora.chatdemo.common.extensions.internal.clearEditTextListener
import io.agora.chatdemo.common.extensions.internal.showRightDrawable
import io.agora.chatdemo.common.helper.DeveloperModeHelper
import io.agora.chatdemo.databinding.DemoFragmentLoginBinding
import io.agora.chatdemo.page.login.viewModel.LoginViewModel
import io.agora.chatdemo.utils.ToastUtils.showToast
import io.agora.uikit.base.EaseBaseFragment
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatError
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.extensions.catchChatException
import io.agora.uikit.common.extensions.hideSoftKeyboard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginFragment : EaseBaseFragment<DemoFragmentLoginBinding>(),
    View.OnClickListener, TextWatcher,OnEditorActionListener {
    private var mUserId: String? = null
    private var mCode: String? = null
    private lateinit var mFragmentViewModel: LoginViewModel
    private var clear: Drawable? = null
    private var eyeOpen: Drawable? = null
    private var eyeClose: Drawable? = null
    private val mHits = LongArray(COUNT)
    private var isDeveloperMode = false
    private var isShowingDialog = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoFragmentLoginBinding {
        return DemoFragmentLoginBinding.inflate(inflater)
    }

    override fun initListener() {
        super.initListener()
        binding?.run {
            etLoginId.addTextChangedListener(this@LoginFragment)
            etLoginCode.addTextChangedListener(this@LoginFragment)
            tvLoginDeveloper.setOnClickListener(this@LoginFragment)
            tvVersion.setOnClickListener(this@LoginFragment)
            btnLogin.setOnClickListener(this@LoginFragment)
            etLoginCode.setOnEditorActionListener(this@LoginFragment)
            etLoginId.clearEditTextListener()
            root.setOnClickListener {
                mContext.hideSoftKeyboard()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mFragmentViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun initData() {
        super.initData()
        binding?.run {
            etLoginId.setText(ChatClient.getInstance().currentUser)
            tvVersion.text = "V${ChatClient.VERSION}"
            tvAgreement.movementMethod = LinkMovementMethod.getInstance()
            tvAgreement.setHintTextColor(Color.TRANSPARENT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val drawableCursor = etLoginId.textCursorDrawable
                drawableCursor?.colorFilter = PorterDuffColorFilter(mContext.getColor(R.color.color_primary), PorterDuff.Mode.SRC_IN)
                etLoginId.textCursorDrawable = drawableCursor
                etLoginCode.textCursorDrawable = drawableCursor
                val idDrawableIndicator = etLoginId.textSelectHandle
                val codeDrawableIndicator = etLoginCode.textSelectHandle
                idDrawableIndicator?.colorFilter = PorterDuffColorFilter(mContext.getColor(R.color.color_primary), PorterDuff.Mode.SRC_IN)
                codeDrawableIndicator?.colorFilter = PorterDuffColorFilter(mContext.getColor(R.color.color_primary), PorterDuff.Mode.SRC_IN)
            }

        }
        eyeClose = ContextCompat.getDrawable(mContext, R.drawable.sign_eye_slash)
        eyeOpen = ContextCompat.getDrawable(mContext, R.drawable.sign_eye)
        clear = ContextCompat.getDrawable(mContext, R.drawable.sign_clear_icon)
        binding?.etLoginId?.showRightDrawable(clear)
        binding?.etLoginCode?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        isDeveloperMode = DeveloperModeHelper.isDeveloperMode()
        resetView(isDeveloperMode)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_version -> {
                System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
                mHits[mHits.size - 1] = SystemClock.uptimeMillis()
                if (mHits[0] >= SystemClock.uptimeMillis() - DURATION && !isShowingDialog) {
                    isShowingDialog = true
                    showOpenDeveloperDialog()
                }
            }
            R.id.btn_login -> {
                mContext.hideSoftKeyboard()
                loginToServer()
            }
            R.id.tv_login_developer -> {
                EaseFlowBus.with<String>(DemoConstant.SKIP_DEVELOPER_CONFIG).post(lifecycleScope, LoginFragment::class.java.simpleName)
            }

        }
    }

    private fun loginToServer() {
        if (TextUtils.isEmpty(mUserId) || TextUtils.isEmpty(mCode)) {
            showToast(mContext.getString(R.string.em_login_btn_info_incomplete))
            return
        }
        lifecycleScope.launch {
            if (isDeveloperMode){
                mFragmentViewModel.login(mUserId!!, mCode!!)
                    .onStart { showLoading(true) }
                    .onCompletion { dismissLoading() }
                    .catchChatException { e ->
                        if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                            showToast(R.string.demo_error_user_authentication_failed)
                        } else {
                            showToast(e.description)
                        }
                    }
                    .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                    .collect {
                        if (it != null) {
                            DemoHelper.getInstance().getDataModel().initDb()
                            startActivity(Intent(mContext, MainActivity::class.java))
                            mContext.finish()
                        }
                    }
            }else{
                lifecycleScope.launch {
                    mFragmentViewModel.loginFromAppServer(mUserId!!, mCode!!)
                        .onStart {
                            showLoading(true)
                        }
                        .onCompletion {
                            dismissLoading()
                        }
                        .catchChatException { e ->
                            if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                                showToast(R.string.demo_error_user_authentication_failed)
                            } else {
                                showToast(e.description)
                            }
                        }
                        .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                        .collect {
                            if (it != null) {
                                DemoHelper.getInstance().getDataModel().initDb()
                                startActivity(Intent(mContext, MainActivity::class.java))
                                mContext.finish()
                            }
                        }
                }
            }
        }
    }


    private fun showOpenDeveloperDialog() {
        SimpleDialog.Builder(mContext)
            .setTitle(
                if (isDeveloperMode) getString(R.string.server_close_develop_mode) else getString(
                    R.string.server_open_develop_mode
                )
            )
            .setPositiveButton{
                isDeveloperMode = !isDeveloperMode
                DeveloperModeHelper.setDeveloperMode(isDeveloperMode)
                binding?.etLoginId?.setText("")
                resetView(isDeveloperMode)
            }
            .setOnDismissListener {
                isShowingDialog = false
            }
            .setCanceledOnTouchOutside(false)
            .build()
            .show()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        binding?.run {
            mUserId = etLoginId.text.toString().trim { it <= ' ' }
            mCode = etLoginCode.text.toString().trim { it <= ' ' }
            etLoginId.showRightDrawable(clear)
            etLoginCode.showRightDrawable(eyeClose)
            setButtonEnable(!TextUtils.isEmpty(mUserId) && !TextUtils.isEmpty(mCode))
        }
    }

    private fun setButtonEnable(enable: Boolean) {
        binding?.run {
            btnLogin.isEnabled = enable
            if (etLoginCode.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_PREVIOUS
            } else if (etLoginId.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
            }
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!TextUtils.isEmpty(mUserId) && !TextUtils.isEmpty(mCode)) {
                mContext.hideSoftKeyboard()
                loginToServer()
                return true
            }
        }
        return false
    }

    private fun resetView(isDeveloperMode: Boolean) {
        binding?.run {
            etLoginCode.setText("")
            etLoginCode.changePwdDrawable(
                eyeOpen,
                eyeClose,
                null,
                null,
                null
            )
            etLoginCode.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etLoginCode.showRightDrawable(null)
            if (!isDeveloperMode) {
                tvLoginDeveloper.visibility = View.GONE
                DeveloperModeHelper.setEnableCustom(false)
                DeveloperModeHelper.setDeveloperMode(false)
            }else{
                tvLoginDeveloper.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TAG = "LoginFragment"
        private const val COUNT: Int = 5
        private const val DURATION = (3 * 1000).toLong()
        private const val stopTimeoutMillis: Long = 5000
    }
}