package io.agora.chatdemo.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView


interface IAttachView {
    fun attachView(view: IControlDataView)

    fun detachView()
}