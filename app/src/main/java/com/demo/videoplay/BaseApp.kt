package com.demo.videoplay

import android.app.Application
import android.content.Context
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException

class BaseApp : Application() {
    var socket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        singletonInstance = this
        try {
            socket = IO.socket(Constants.SERVER_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private var singletonInstance: Context? = null

        @get:Synchronized
        val appContext: Context?
            get() {
                if (null == singletonInstance) {
                    singletonInstance = BaseApp()
                }
                return singletonInstance
            }
    }
}