package com.hyphenate.chatdemo.callkit.extensions

import org.json.JSONObject

/**
 * Get string value from JSONObject or return null if the key does not exist.
 */
internal fun JSONObject.getStringOrNull(name: String): String? {
    return if (has(name)) {
        getString(name)
    } else {
        null
    }
}