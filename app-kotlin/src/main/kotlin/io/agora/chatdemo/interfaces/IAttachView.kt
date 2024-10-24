package io.agora.chatdemo.interfaces

import io.agora.uikit.common.interfaces.IControlDataView


interface IAttachView {
    fun attachView(view: IControlDataView)

    fun detachView()
}