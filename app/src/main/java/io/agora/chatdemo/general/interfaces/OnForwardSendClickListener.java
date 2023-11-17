package io.agora.chatdemo.general.interfaces;

import android.view.View;

public interface OnForwardSendClickListener {
    /**
     * Item click event.
     * @param view
     * @param to
     */
    void onClick(View view, String to);
}
