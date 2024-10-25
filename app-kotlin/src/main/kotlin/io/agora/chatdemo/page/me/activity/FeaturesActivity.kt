package io.agora.chatdemo.page.me.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.extensions.internal.setSwitchDefaultStyle
import io.agora.chatdemo.databinding.DemoActivityFeaturesBinding
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseActivity

class FeaturesActivity: EaseBaseActivity<DemoActivityFeaturesBinding>(),View.OnClickListener {
    override fun getViewBinding(inflater: LayoutInflater): DemoActivityFeaturesBinding {
        return DemoActivityFeaturesBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    fun initView(){
        val enableTranslation = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_TRANSLATION,true)
        val enableThread = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_THREAD,true)
        val enableReaction = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_REACTION,true)
        val isTyping = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_TYPING_ON,true)
        binding.switchItemTranslation.setChecked(enableTranslation)
        binding.switchItemTranslation.setSwitchDefaultStyle()
        binding.switchItemTopic.setChecked(enableThread)
        binding.switchItemTopic.setSwitchDefaultStyle()
        binding.switchItemReaction.setChecked(enableReaction)
        binding.switchItemReaction.setSwitchDefaultStyle()
        binding.switchItemTyping.setChecked(isTyping)
        binding.switchItemTyping.setSwitchDefaultStyle()
    }

    fun initListener(){
        binding.let {
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
            it.switchItemTranslation.setOnClickListener(this)
            it.switchItemTopic.setOnClickListener(this)
            it.switchItemReaction.setOnClickListener(this)
            it.switchItemTyping.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.switch_item_translation -> {
                binding.switchItemTranslation.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemTranslation.setChecked(isChecked)
                    EaseIM.getConfig()?.chatConfig?.enableTranslationMessage = isChecked
                    DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.FEATURES_TRANSLATION,isChecked)
                }
            }
            R.id.switch_item_topic -> {
                binding.switchItemTopic.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemTopic.setChecked(isChecked)
                    EaseIM.getConfig()?.chatConfig?.enableChatThreadMessage = isChecked
                    DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.FEATURES_THREAD,isChecked)
                }
            }
            R.id.switch_item_reaction -> {
                binding.switchItemReaction.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemReaction.setChecked(isChecked)
                    EaseIM.getConfig()?.chatConfig?.enableMessageReaction = isChecked
                    DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.FEATURES_REACTION,isChecked)
                }
            }
            R.id.switch_item_typing -> {
                binding.switchItemTyping.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemTyping.setChecked(isChecked)
                    EaseIM.getConfig()?.chatConfig?.enableChatTyping = isChecked
                    DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.IS_TYPING_ON,isChecked)
                }
            }
            else -> {}
        }
    }
}