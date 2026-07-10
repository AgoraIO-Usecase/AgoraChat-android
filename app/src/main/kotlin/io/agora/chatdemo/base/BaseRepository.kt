package io.agora.chatdemo.base

import android.content.Context
import io.agora.chatdemo.DemoApplication

open class BaseRepository {
    fun getContext(): Context {
        return DemoApplication.getInstance()
    }
}