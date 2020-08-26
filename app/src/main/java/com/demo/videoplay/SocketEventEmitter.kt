package com.demo.videoplay

import android.util.Log
import com.demo.callback.SocketEventListener
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject

class SocketEventEmitter(private val socketEventListener: SocketEventListener) {
    var TAG = SocketEventListener::class.java.simpleName
    private val app: BaseApp = BaseApp.appContext as BaseApp
    private val mSocket: Socket
    private val messageHandler: MessageHandler
    private var mVideoUrl: String? = null
    private var mIsRoomCreator = false
    private val mRoomID = Constants.ROOM_ID

    private inner class MessageHandler() {
        val onConnected = Emitter.Listener { args: Array<Any?>? ->
            try {
                Log.i(TAG, "EVENT:" + "Connected")
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
        val onDisconnected = Emitter.Listener { args: Array<Any?>? -> Log.i(TAG, "EVENT:" + "DisConnected") }
        val roomCreated = Emitter.Listener { args: Array<Any> ->
            Log.i(TAG, "EVENT: " + Constants.EVENT_ROOM_CREATED)
            try {
                val info = args[0] as JSONObject
                val key = info.getString("YT_API_KEY")
                Thread(Task(key)).start()
                Thread.sleep(2000)
                val data = JSONObject()
                data.put("yt_video_id", "")
                data.put("roomId", Constants.ROOM_ID)
                mSocket.emit(Constants.EVENT_SET_CURRENT_VIDEO, data) // Start play You tube music
                mIsRoomCreator = true
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
        val roomJoined = Emitter.Listener { args: Array<Any> ->
            Log.i(TAG, "EVENT: " + Constants.EVENT_ROOM_JOINED)
            try {
                val info = args[0] as JSONObject
                val key = info.getString("YT_API_KEY")
                Thread(Task(key)).start()
                Thread.sleep(2000)
                mSocket.emit(Constants.EVENT_GET_CURRENT_VIDEO, Constants.ROOM_ID) // Start play You tube music
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
        val playVideo = Emitter.Listener { args: Array<Any> ->
            Log.i(TAG, "EVENT: " + Constants.EVENT_PLAY_VIDEO)
            try {
                val info = args[0] as JSONObject
                val v_id = info.getString("yt_video_id")
                mVideoUrl = v_id
                val roomCreator = info.getBoolean("isRoomCreator")
                if (roomCreator && mIsRoomCreator) // Actually who is set the current video he/she is Media center for playing video
                {
                    socketEventListener.onStartVideo(mVideoUrl)
                } else if (!roomCreator && !mIsRoomCreator) { // This is for get video info play accordingly
                    socketEventListener.onStartVideo(mVideoUrl)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
        val roomFull = Emitter.Listener { args: Array<Any?>? -> Log.i(TAG, "EVENT: " + Constants.EVENT_ROOM_FULL) }
    }

    internal inner class Task(var key: String) : Runnable {
        override fun run() {
            socketEventListener.onInitializeYTVideoPlayer(key)
        }

    }

    init {
        messageHandler = MessageHandler()
        mSocket = app.socket!!
        mSocket.connect()
        mSocket.emit(Constants.EVENT_JOIN, Constants.ROOM_ID)
        mSocket.on(Socket.EVENT_CONNECT, messageHandler.onConnected)
        mSocket.on(Socket.EVENT_DISCONNECT, messageHandler.onDisconnected)
        mSocket.on(Constants.EVENT_ROOM_CREATED, messageHandler.roomCreated)
        mSocket.on(Constants.EVENT_ROOM_JOINED, messageHandler.roomJoined)
        mSocket.on(Constants.EVENT_PLAY_VIDEO, messageHandler.playVideo)
        mSocket.on(Constants.EVENT_ROOM_FULL, messageHandler.roomFull)
    }
}