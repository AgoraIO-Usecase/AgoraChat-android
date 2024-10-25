package io.agora.chatdemo.feature.presence.utils

import android.content.Context
import android.text.TextUtils
import androidx.annotation.DrawableRes
import io.agora.chatdemo.bean.PresenceData
import io.agora.chatdemo.common.DemoConstant
import io.agora.uikit.common.ChatPresence

object EasePresenceUtil {

    fun getPresenceString(context: Context, presence: ChatPresence?): String {
        if (presence != null) {
            var isOnline = false
            val statusList: Map<String?, Int> = presence.statusList
            for (entry in statusList) {
                if (entry.value == 1){
                    isOnline = true
                    break
                }
            }

            if (isOnline) {
                val ext: String = presence.ext
                return if (TextUtils.isEmpty(ext) || TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_ONLINE
                    )
                ) {
                    context.getString(PresenceData.ONLINE.presence)
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_BUSY
                    )
                ) {
                    context.getString(PresenceData.BUSY.presence)
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_DO_NOT_DISTURB
                    )
                ) {
                    context.getString(PresenceData.DO_NOT_DISTURB.presence)
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_AWAY
                    )
                ) {
                    context.getString(PresenceData.AWAY.presence)
                } else {
                    ext
                }
            }
        }
        return context.getString(PresenceData.OFFLINE.presence)
    }

    @DrawableRes
    fun getPresenceIcon(context: Context, presence: ChatPresence?): Int {
        if (presence != null){
            var isOnline = false
            val statusList: Map<String?, Int> = presence.statusList
            for (entry in statusList) {
                if (entry.value == 1){
                    isOnline = true
                    break
                }
            }
            if (isOnline) {
                val ext: String = presence.ext
                return if (TextUtils.isEmpty(ext) || TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_ONLINE
                    )
                ) {
                    PresenceData.ONLINE.presenceIcon
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_BUSY
                    )
                ) {
                    PresenceData.BUSY.presenceIcon
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_DO_NOT_DISTURB
                    )
                ) {
                    PresenceData.DO_NOT_DISTURB.presenceIcon
                } else if (TextUtils.equals(
                        ext,
                        DemoConstant.PRESENCE_AWAY
                    )
                ) {
                    PresenceData.AWAY.presenceIcon
                } else {
                    PresenceData.CUSTOM.presenceIcon
                }
            }
        }
        return PresenceData.OFFLINE.presenceIcon
    }
}