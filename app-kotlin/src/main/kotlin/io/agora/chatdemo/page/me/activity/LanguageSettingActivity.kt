package io.agora.chatdemo.page.me.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chatdemo.R
import io.agora.chatdemo.bean.Language
import io.agora.chatdemo.bean.LanguageType
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PreferenceManager
import io.agora.chatdemo.databinding.DemoActivityLanguageBinding
import io.agora.chatdemo.interfaces.LanguageListItemSelectListener
import io.agora.uikit.base.EaseBaseActivity

class LanguageSettingActivity: EaseBaseActivity<DemoActivityLanguageBinding>() {
    private var tagList:MutableList<Language> = mutableListOf()
    private var languageAdapter:LanguageAdapter? = null
    private var languageTag:String = ""
    private var languageCode:String = ""
    private var languageType:Int = 0
    private var selectedPosition: Int = -1

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityLanguageBinding {
        return DemoActivityLanguageBinding.inflate(inflater)
    }

    companion object {
        private const val RESULT_LANGUAGE_TAG = "language_tag"
        private const val RESULT_LANGUAGE_CODE = "language_code"
        private const val LANGUAGE_TYPE = "language_type"
        private const val LANGUAGE_TYPE_TARGET = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.hasExtra(LANGUAGE_TYPE).apply {
            languageType = intent.getIntExtra(LANGUAGE_TYPE,0)
        }
        initView()
        initListener()
    }

    fun initView(){
        defaultLanguage()
        binding.let {
            languageAdapter = LanguageAdapter(tagList)
            val layoutManager = LinearLayoutManager(mContext)
            it.rlSheetList.layoutManager = layoutManager
            it.rlSheetList.adapter = this.languageAdapter
            when(languageType){
                LANGUAGE_TYPE_TARGET -> {
                    binding.titleBar.setTitle(getString(R.string.currency_target_language))
                    PreferenceManager.getValue(DemoConstant.TARGET_LANGUAGE,
                        LanguageType.EN.value).let { tag->
                        val index = tagList.indexOfFirst { language-> language.type.value == tag }
                        languageCode = tag
                        selectedPosition = index
                        if (selectedPosition != -1){
                            languageAdapter?.setSelectPosition(selectedPosition)
                        }
                    }
                }
                else -> {}
            }
            updateConfirm()
        }
    }

    fun initListener(){
        languageAdapter?.setLanguageListItemClickListener(object : LanguageListItemSelectListener{
            override fun onSelectListener(position: Int,language:Language) {
                languageTag = language.tag
                languageCode = language.type.value
                selectedPosition = position
                updateConfirm()
            }
        })
        binding.titleBar.setOnMenuItemClickListener { item->
            when (item.itemId){
                R.id.action_language_confirm -> {
                    chengLanguage()
                }
                else -> {}
            }
            true
        }
        binding.titleBar.setNavigationOnClickListener{
            mContext.onBackPressed()
        }

    }

    fun updateConfirm(){
        binding.let {
            it.titleBar.getToolBar().let { tb ->
                tb.menu.findItem(R.id.action_language_confirm)?.let { menuItem->
                    menuItem.isVisible = true
                    menuItem.title?.let { tl ->
                        val spannable = SpannableString(menuItem.title)
                        spannable.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(mContext,
                                if (selectedPosition != -1){
                                    R.color.color_primary
                                }else{
                                    R.color.demo_on_background_high
                                })
                            )
                            , 0, tl.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        menuItem.title = spannable
                    }
                }
            }
        }
    }

    private fun chengLanguage(){
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_LANGUAGE_TAG,languageTag)
        resultIntent.putExtra(RESULT_LANGUAGE_CODE,languageCode)
        setResult(RESULT_OK,resultIntent)
        finish()
    }

    private fun defaultLanguage(){
        tagList = mutableListOf(
            Language(LanguageType.ZH,getString(R.string.currency_language_zh_cn)),
            Language(LanguageType.EN,getString(R.string.currency_language_en))
        )
    }

    class LanguageAdapter(
        private val languageList: MutableList<Language>?,
    ) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>(){
        private lateinit var listener: LanguageListItemSelectListener
        private var selectPosition:Int = -1

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.language_tag)
            val tagCb: CheckBox = itemView.findViewById(R.id.language_cb)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ease_layout_language_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return languageList?.size ?: 0
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            languageList?.let {
                holder.textView.text = it[position].tag
                holder.tagCb.isChecked = selectPosition == position

                holder.tagCb.isClickable = false

                holder.itemView.setOnClickListener {
                    if (position == selectPosition) {
                        holder.tagCb.isChecked = false
                        selectPosition = -1
                    }else{
                        holder.tagCb.isChecked = true
                        selectPosition = position
                    }
                    notifyDataSetChanged()

                    if (holder.tagCb.isChecked){
                        listener.onSelectListener(position,languageList[selectPosition])
                    }
                }
            }
        }

        fun setSelectPosition(selectPosition:Int){
            this.selectPosition = selectPosition
            notifyDataSetChanged()
        }

        fun setLanguageListItemClickListener(listener: LanguageListItemSelectListener){
            this.listener = listener
        }

    }
}