package com.demo.callback

interface SocketEventListener {
    fun onStartVideo(url: String?)
    fun onInitializeYTVideoPlayer(apiKey: String?)
}