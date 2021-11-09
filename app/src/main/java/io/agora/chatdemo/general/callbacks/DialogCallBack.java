package io.agora.chatdemo.general.callbacks;

import android.content.DialogInterface;

public interface DialogCallBack {

    /**
     * Click event for dialog
     * @param dialog
     * @param which
     */
    void onClick(DialogInterface dialog, int which);
}
