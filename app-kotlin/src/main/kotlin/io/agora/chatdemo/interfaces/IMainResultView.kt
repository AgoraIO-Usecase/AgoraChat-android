package io.agora.chatdemo.interfaces

import io.agora.uikit.common.interfaces.IControlDataView

interface IMainResultView: IControlDataView {

    /**
     * Get unread message count successfully.
     */
    fun getUnreadCountSuccess(count: String?)

    /**
     * Get all unread request count successfully.
     */
    fun getRequestUnreadCountSuccess(count: String?)
}