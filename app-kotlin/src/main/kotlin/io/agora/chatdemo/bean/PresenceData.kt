package io.agora.chatdemo.bean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.agora.chatdemo.R

enum class PresenceData(
    @field:StringRes @get:StringRes
    @param:StringRes var presence: Int, @field:DrawableRes @get:DrawableRes
    @param:DrawableRes var presenceIcon: Int
) {
    ONLINE(
        R.string.ease_presence_online,
        R.drawable.ease_presence_online
    ),
    BUSY(
        R.string.ease_presence_busy,
        R.drawable.ease_presence_busy
    ),
    DO_NOT_DISTURB(
        R.string.ease_presence_do_not_disturb,
        R.drawable.ease_presence_do_not_disturb
    ),
    AWAY(
        R.string.ease_presence_away,
        R.drawable.ease_presence_away
    ),
    OFFLINE(
        R.string.ease_presence_offline,
        R.drawable.ease_presence_offline
    ),
    CUSTOM(R.string.ease_presence_custom, R.drawable.ease_presence_custom)

}