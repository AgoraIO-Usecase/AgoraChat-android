package io.agora.chatdemo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import io.agora.chatdemo.base.UserActivityLifecycleCallbacks
import io.agora.chatdemo.bean.LanguageType
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PreferenceManager
import io.agora.chatdemo.utils.LanguageUtil
import io.agora.uikit.EaseIM

class DemoApplication: Application() {

    private val mLifecycleCallbacks = UserActivityLifecycleCallbacks()

    companion object {
        private lateinit var instance: DemoApplication
        fun getInstance(): DemoApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks()

        DemoHelper.getInstance().init(this)
        initFeatureConfig()
    }

    private fun registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
    }

    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks {
        return mLifecycleCallbacks
    }

    private fun initFeatureConfig(){
        val isBlack = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_BLACK_THEME)
        AppCompatDelegate.setDefaultNightMode(if (isBlack) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        val enableTranslation = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_TRANSLATION,true)
        val enableThread = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_THREAD,true)
        val enableReaction = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_REACTION,true)
        val enableTyping = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_TYPING_ON,false)
        val targetLanguage = PreferenceManager.getValue(DemoConstant.TARGET_LANGUAGE, LanguageType.EN.value)
        EaseIM.getConfig()?.chatConfig?.targetTranslationLanguage = targetLanguage
        LanguageUtil.changeLanguage("en")

        EaseIM.getConfig()?.chatConfig?.enableTranslationMessage = enableTranslation
        EaseIM.getConfig()?.chatConfig?.enableChatThreadMessage = enableThread
        EaseIM.getConfig()?.chatConfig?.enableMessageReaction = enableReaction
        EaseIM.getConfig()?.chatConfig?.enableChatTyping = enableTyping
    }
}