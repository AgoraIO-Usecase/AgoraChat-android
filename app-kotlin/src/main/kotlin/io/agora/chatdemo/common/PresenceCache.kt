package io.agora.chatdemo.common

import io.agora.uikit.common.ChatPresence
import java.util.concurrent.ConcurrentHashMap

object PresenceCache {

    private val presencesMap: ConcurrentHashMap<String, ChatPresence> = ConcurrentHashMap()

    @Synchronized
    fun insertPresences(userId: String?,chatPresence: ChatPresence){
        presencesMap.let { presence->
            userId?.let {
                presence[it] = chatPresence
            }
        }
    }

    fun getUserPresence(userId: String):ChatPresence?{
        if (presencesMap.size > 0 && presencesMap.containsKey(userId)){
            return presencesMap[userId]
        }
        return null
    }

    var getPresenceInfo:ConcurrentHashMap<String, ChatPresence> = presencesMap

    fun clear(){
        presencesMap.clear()
    }
}