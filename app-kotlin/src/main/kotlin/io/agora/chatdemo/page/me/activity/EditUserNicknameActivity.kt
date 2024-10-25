package io.agora.chatdemo.page.me.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import io.agora.chatdemo.R
import io.agora.chatdemo.databinding.DemoActivityMeInformationEditBinding
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseActivity
import io.agora.uikit.model.EaseProfile

open class EditUserNicknameActivity: EaseBaseActivity<DemoActivityMeInformationEditBinding>() {
    var selfProfile: EaseProfile? = null
    private var newName:String = ""

    companion object{
        private const val RESULT_REFRESH = "isRefresh"
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityMeInformationEditBinding {
       return DemoActivityMeInformationEditBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selfProfile = EaseIM.getCurrentUser()
        initTitle()
        initListener()
        showKeyboard(binding.etName)
    }

    open fun initTitle(){
        binding.run {
            titleBar.setTitle(getString(R.string.main_about_me_information_edit_nick_name))
            selfProfile?.let {
                etName.setText(it.name)
            }
            etName.requestFocus()
            showKeyboard(etName)
        }
        binding.inputNameCount.text = resources.getString(
            R.string.main_about_me_information_change_name_count
            ,selfProfile?.name?.length ?: 0)
    }

    open fun initListener(){
        binding.titleBar.setNavigationOnClickListener { mContext.onBackPressed() }
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val length = s.toString().trim().length
                if (length == 0){
                    binding.inputNameCount.text =
                        resources.getString(R.string.main_about_me_information_change_name_count, 0)
                }else{
                    binding.inputNameCount.text =
                        resources.getString(R.string.main_about_me_information_change_name_count, length)

                }
                updateSaveView(binding.etName.text.length)
            }
        })
        binding.titleBar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                io.agora.uikit.R.id.action_save -> {
                    updateUserInfo()
                }

                else -> {}
            }
            true
        }
    }

    open fun updateSaveView(length: Int){
        binding.titleBar.setMenuTitleColor(
            ContextCompat.getColor(mContext,
            if (length != 0) R.color.color_primary
            else R.color.demo_on_background_high))
    }

    private fun updateUserInfo(){
        newName = binding.etName.text.trim().toString()
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_REFRESH, true)
        resultIntent.putExtra("nickname", newName)
        setResult(RESULT_OK,resultIntent)
        finish()
    }

}