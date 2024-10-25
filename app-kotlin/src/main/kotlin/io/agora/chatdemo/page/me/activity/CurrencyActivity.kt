package io.agora.chatdemo.page.me.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.bean.LanguageType
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PreferenceManager
import io.agora.chatdemo.common.extensions.internal.setSwitchDefaultStyle
import io.agora.chatdemo.databinding.DemoActivityCurrencyBinding
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseActivity

class CurrencyActivity: EaseBaseActivity<DemoActivityCurrencyBinding>(),View.OnClickListener {
    private var targetLanguage:String? = ""
    override fun getViewBinding(inflater: LayoutInflater): DemoActivityCurrencyBinding {
        return DemoActivityCurrencyBinding.inflate(inflater)
    }
    companion object {
        private const val LANGUAGE_TYPE_TARGET = 1
        private const val RESULT_CHOICE_TARGET_LANGUAGE = 101
        private const val RESULT_LANGUAGE_TAG = "language_tag"
        private const val RESULT_LANGUAGE_CODE = "language_code"
        private const val LANGUAGE_TYPE = "language_type"
    }

    private val launcherToTargetLanguage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CHOICE_TARGET_LANGUAGE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    private fun initView(){
        initSwitch()
        initLanguage()
    }

    private fun initListener(){
        binding.let {
            it.switchItemDark.setOnClickListener(this)
            it.arrowItemFeature.setOnClickListener(this)
            it.arrowItemTargetLanguage.setOnClickListener(this)
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
        }
    }

    private fun initSwitch(){
        val isBlack = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_BLACK_THEME)

        val handler = Looper.myLooper()?.let { Handler(it) }

        handler?.postDelayed({
            binding.switchItemDark.setChecked(isBlack)
            binding.switchItemDark.setSwitchDefaultStyle()
        }, 200)

    }

    private fun initLanguage(){
        val spTagLanguage = PreferenceManager.getValue(DemoConstant.TARGET_LANGUAGE, LanguageType.EN.value)

        if (spTagLanguage == LanguageType.ZH.value){
            targetLanguage = getString(R.string.currency_language_zh_cn)
        }else if (spTagLanguage == LanguageType.EN.value){
            targetLanguage = getString(R.string.currency_language_en)
        }
        updateLanguage()
    }

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                if (it.hasExtra(RESULT_LANGUAGE_TAG) && it.hasExtra(RESULT_LANGUAGE_CODE)){
                    val tag = it.getStringExtra(RESULT_LANGUAGE_TAG)
                    val code = it.getStringExtra(RESULT_LANGUAGE_CODE)
                    when (requestCode) {
                        RESULT_CHOICE_TARGET_LANGUAGE -> {
                            targetLanguage = tag
                            code?.let { languageCode->
                                PreferenceManager.putValue(DemoConstant.TARGET_LANGUAGE, languageCode)
                                EaseIM.getConfig()?.chatConfig?.targetTranslationLanguage = languageCode
                            }
                        }
                        else -> {}
                    }
                }
                updateLanguage()
            }
        }
    }

    private fun changeTheme(isChecked: Boolean){
        DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.IS_BLACK_THEME, isChecked)
        AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun updateLanguage(){
        binding.let {
            it.arrowItemTargetLanguage.setContent(targetLanguage?:"")
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.arrow_item_feature -> {
                startActivity(Intent(this@CurrencyActivity,FeaturesActivity::class.java))
            }
            R.id.arrow_item_target_language -> {
                val intent = Intent(this@CurrencyActivity, LanguageSettingActivity::class.java)
                intent.putExtra(LANGUAGE_TYPE, LANGUAGE_TYPE_TARGET)
                launcherToTargetLanguage.launch(intent)
            }
            R.id.switch_item_dark -> {
                binding.switchItemDark.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemDark.setChecked(isChecked)
                    changeTheme(isChecked)
                }
            }
            else -> {}
        }
    }


}