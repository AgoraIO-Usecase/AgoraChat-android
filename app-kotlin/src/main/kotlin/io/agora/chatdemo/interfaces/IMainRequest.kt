package io.agora.chatdemo.interfaces


interface IMainRequest: IAttachView {

    /**
     * Get all unread message count.
     */
    fun getUnreadMessageCount()

    /**
     * Get all unread request count.
     */
    fun getRequestUnreadCount()
}