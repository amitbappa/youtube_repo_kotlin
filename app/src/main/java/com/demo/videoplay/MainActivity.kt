package com.demo.videoplay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.demo.callback.SocketEventListener
import com.demo.videoplay.MainActivity
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener
import com.google.android.youtube.player.YouTubePlayerView

class MainActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener, SocketEventListener, View.OnClickListener {
    var mVideoUrl: String? = null
    private var youTubeView: YouTubePlayerView? = null
    private var playerStateChangeListener: MyPlayerStateChangeListener? = null
    private var playbackEventListener: MyPlaybackEventListener? = null
    private var player: YouTubePlayer? = null
    private var socketEventEmitter: SocketEventEmitter? = null
    private var joinRoomBtn: Button? = null
    private var mYTApiKey = ""
    private var wasRestored = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        setContentView(R.layout.yt_video)
        joinRoomBtn = findViewById<View>(R.id.join_room_btn) as Button
        joinRoomBtn!!.isEnabled = true
        joinRoomBtn!!.setOnClickListener(this)
        youTubeView = findViewById<View>(R.id.youtube_view) as YouTubePlayerView
    }

    private fun initVideoPlayer() {
        runOnUiThread(Thread(Runnable {
            youTubeView!!.initialize(mYTApiKey, this@MainActivity)
            playerStateChangeListener = MyPlayerStateChangeListener()
            playbackEventListener = MyPlaybackEventListener()
        }))
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
        this.player = player
        player.setPlayerStateChangeListener(playerStateChangeListener)
        player.setPlaybackEventListener(playbackEventListener)
        this.wasRestored = wasRestored
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider, errorReason: YouTubeInitializationResult) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show()
        } else {
            val error = String.format(getString(R.string.player_error), errorReason.toString())
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerProvider!!.initialize(mYTApiKey, this)
        }
    }

    val youTubePlayerProvider: YouTubePlayer.Provider?
         get() = youTubeView


    override fun onClick(view: View) {
        when (view.id) {
            R.id.join_room_btn -> {
                socketEventEmitter = SocketEventEmitter(this)
                joinRoomBtn!!.isEnabled = false
            }
        }
    }

    private inner class MyPlaybackEventListener : PlaybackEventListener {
        override fun onPlaying() {
            // Called when playback starts, either due to user action or call to play().
            showMessage("Playing")
        }

        override fun onPaused() {
            // Called when playback is paused, either due to user action or call to pause().
            showMessage("Paused")
        }

        override fun onStopped() {
            // Called when playback stops for a reason other than being paused.
            showMessage("Stopped")
        }

        override fun onBuffering(b: Boolean) {
            // Called when buffering starts or ends.
            showMessage("Is Buffering: " + if (b) "Yes" else "False")
        }

        override fun onSeekTo(i: Int) {
            // Called when a jump in playback position occurs, either
            // due to user scrubbing or call to seekRelativeMillis() or seekToMillis()
        }
    }

    private inner class MyPlayerStateChangeListener : PlayerStateChangeListener {
        override fun onLoading() {
            // Called when the player is loading a video
            // At this point, it's not ready to accept commands affecting playback such as play() or pause()
            showMessage("Loading")
        }

        override fun onLoaded(s: String) {
            // Called when a video is done loading.
            // Playback methods such as play(), pause() or seekToMillis(int) may be called after this callback.
            showMessage("Loaded")
        }

        override fun onAdStarted() {
            // Called when playback of an advertisement starts.
            showMessage("onAdStarted")
        }

        override fun onVideoStarted() {
            // Called when playback of the video starts.
        }

        override fun onVideoEnded() {
            // Called when the video reaches its end.
            showMessage("onAdStarted")
            if (!player!!.hasNext()) {
                player!!.cueVideo(mVideoUrl)
            }
        }

        override fun onError(errorReason: YouTubePlayer.ErrorReason) {
            // Called when an error occurs.
            showMessage("Error occur")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val RECOVERY_REQUEST = 1
    }

    override fun onStartVideo(url: String?) {
        mVideoUrl = url
        if (null != mVideoUrl) {
            if (!wasRestored) {
                player!!.loadVideo(mVideoUrl)
            }
        }
    }

    override fun onInitializeYTVideoPlayer(apiKey: String?) {
        if (apiKey != null) {
            mYTApiKey = apiKey
        }
        initVideoPlayer()    }


}