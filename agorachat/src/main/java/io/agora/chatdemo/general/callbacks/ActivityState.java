package io.agora.chatdemo.general.callbacks;

import android.app.Activity;

import java.util.List;

/**
 * Created by shuwei on 2017/12/18.
 */

public interface ActivityState {
    /**
     * Get current activity
     * @return
     */
    Activity current();

    /**
     * Get activity list
     * @return
     */
    List<Activity> getActivityList();

    /**
     * Get the count in stack
     * @return
     */
    int count();

    /**
     * Determine if the application is in the foreground, that is, visible
     * @return
     */
    boolean isFront();
}
