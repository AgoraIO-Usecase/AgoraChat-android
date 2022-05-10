package io.agora.chatdemo.general.models;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.agora.chatdemo.R;


public enum PresenceData {
    ONLINE(R.string.ease_presence_online,R.drawable.ease_presence_online),
    BUSY(R.string.ease_presence_busy,R.drawable.ease_presence_busy),
    DO_NOT_DISTURB(R.string.ease_presence_do_not_disturb,R.drawable.ease_presence_do_not_disturb),
    LEAVE(R.string.ease_presence_leave,R.drawable.ease_presence_leave),
    OFFLINE(R.string.ease_presence_offline,R.drawable.ease_presence_offline),
    CUSTOM(R.string.ease_presence_custom,R.drawable.ease_presence_custom)
    ;

    PresenceData(@StringRes int mPresence, @DrawableRes int mPresenceIcon) {
        this.mPresence = mPresence;
        this.mPresenceIcon = mPresenceIcon;
    }

    private @StringRes int mPresence;
    private @DrawableRes int mPresenceIcon;

    public @StringRes int getPresence() {
        return mPresence;
    }

    public void setPresence(@StringRes int mPresence) {
        this.mPresence = mPresence;
    }

    public @DrawableRes int getPresenceIcon() {
        return mPresenceIcon;
    }

    public void setPresenceIcon(@DrawableRes int mPresenceIcon) {
        this.mPresenceIcon = mPresenceIcon;
    }
}
