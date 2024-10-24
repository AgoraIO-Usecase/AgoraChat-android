package io.agora.chatdemo.interfaces

import io.agora.chatdemo.bean.Language


interface LanguageListItemSelectListener {
    fun onSelectListener(position:Int,language: Language)
}