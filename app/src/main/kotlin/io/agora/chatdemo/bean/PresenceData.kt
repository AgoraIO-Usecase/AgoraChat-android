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
        R.string.uikit_presence_online,
        R.drawable.uikit_presence_online
    ),
    BUSY(
        R.string.uikit_presence_busy,
        R.drawable.uikit_presence_busy
    ),
    DO_NOT_DISTURB(
        R.string.uikit_presence_do_not_disturb,
        R.drawable.uikit_presence_do_not_disturb
    ),
    AWAY(
        R.string.ease_presence_away,
        R.drawable.ease_presence_away
    ),
    OFFLINE(
        R.string.uikit_presence_offline,
        R.drawable.uikit_presence_offline
    ),
    CUSTOM(R.string.uikit_presence_custom, R.drawable.uikit_presence_custom)

}