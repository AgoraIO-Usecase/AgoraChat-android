package io.agora.chatdemo.feature.presence.interfaces

import io.agora.uikit.common.ChatPresence
import io.agora.uikit.common.interfaces.IControlDataView

interface IPresenceResultView: IControlDataView {

    /**
     * Publish custom status success
     */
    fun onPublishPresenceSuccess(){}

    /**
     * Publish custom status fail
     */
    fun onPublishPresenceFail(code: Int,  message: String?){}

    /**
     * Gets the current presence state of users success
     */
    fun fetchPresenceStatusSuccess(presence:MutableList<ChatPresence>){}

    /**
     * Gets the current presence state of users fail
     */
    fun fetchPresenceStatusFail(code: Int,  message: String?){}

    /**
     * Subscribe user status success
     */
    fun subscribePresenceSuccess(result: MutableList<ChatPresence>){}

    /**
     * Subscribe user status fail
     */
    fun subscribePresenceFail(code: Int,  message: String?){}

    /**
     * unSubscribe user status success
     */
    fun unSubscribePresenceSuccess(){}

    /**
     * unSubscribe user status fail
     */
    fun unSubscribePresenceFail(code: Int,  message: String?){}

    /**
     * fetch the current user status of a specified user success.
     * @param presence
     */
    fun fetchChatPresenceSuccess(presence:MutableList<ChatPresence>){}

    /**
     * fetch the current user status of a specified user fail.
     * @param code  error code.
     * @param error error message.
     */
    fun fetchChatPresenceFail(code: Int, error: String){}

}