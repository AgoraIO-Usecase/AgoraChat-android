package io.agora.chatdemo.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

internal object PreferenceManager {

    private var mSharedPreferences: SharedPreferences? = null
    private const val PREF_NAME = "saveInfo"

    @Synchronized
    fun init(context: Context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Save the value to the preference.
     * @param key The key of the preference.
     * @param value The value of the preference.
     */
    @SuppressLint("NewApi")
    fun <T> putValue(key: String, value: T) {
        val editor = mSharedPreferences?.edit()
        when (value) {
            is String -> editor?.putString(key, value)
            is Int -> editor?.putInt(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is Float -> editor?.putFloat(key, value)
            is Long -> editor?.putLong(key, value)
            else -> editor?.putString(key, value.toString())
        }
        editor?.apply()
    }

    /**
     * Get the value from the preference.
     * @param key The key of the preference.
     * @param defValue The default value of the preference.
     */
    fun <T> getValue(key: String, defValue: T): T {
        val value = when (defValue) {
            is String -> mSharedPreferences?.getString(key, defValue)
            is Int -> mSharedPreferences?.getInt(key, defValue)
            is Boolean -> mSharedPreferences?.getBoolean(key, defValue)
            is Float -> mSharedPreferences?.getFloat(key, defValue)
            is Long -> mSharedPreferences?.getLong(key, defValue)
            else -> mSharedPreferences?.getString(key, defValue.toString())
        }
        return value as T
    }

}