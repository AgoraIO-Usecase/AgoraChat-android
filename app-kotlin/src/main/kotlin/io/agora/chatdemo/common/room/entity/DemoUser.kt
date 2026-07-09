package io.agora.chatdemo.common.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.agora.chat.uikit.model.ChatUIKitProfile

@Entity
data class DemoUser(
    @PrimaryKey val userId: String,
    val name: String?,
    val avatar: String?,
    val remark: String? = null,
    @ColumnInfo(name = "update_times")
    var updateTimes: Int = 0
)

/**
 * Convert the user data to the profile data.
 */
internal fun DemoUser.parse() = ChatUIKitProfile(userId, name, avatar, remark)
