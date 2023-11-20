package com.neupanesushant.kurakani.domain.usecase.agora

import android.content.Context
import android.widget.Toast
import com.neupanesushant.kurakani.BuildConfig
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class AgoraManager(
    private val context: Context,
    private val channelId: String,
) {

    private var agoraEngine: RtcEngine? = null
    private var isJoined = false;
    private lateinit var onUserActivityStatusChange: (Int, String) -> Unit

    fun setupVideoSDKEngine() {
        val a =
            try {
                val config = RtcEngineConfig()
                config.mContext = context
                config.mAppId = APP_ID
                config.mEventHandler = mRtcEventHandler
                agoraEngine = RtcEngine.create(config)
            } catch (e: Exception) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    fun joinChannel() {
        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION_1v1
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        agoraEngine?.let {
            it.startPreview()
            it.joinChannel(TOKEN, channelId, LOCAL_USER_ID, options)
        }
    }

    fun leaveChannel(onChannelLeft: () -> Unit) {
        if (!isJoined) return
        agoraEngine?.leaveChannel()
        isJoined = false
        onChannelLeft()
    }

    fun getAgoraEngine(perform: (RtcEngine?) -> Unit) {
        perform(agoraEngine)
    }

    fun onDestroy() {
        agoraEngine?.let {
            it.stopPreview()
            it.leaveChannel()
        }
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    fun setOnUserActivityStatusChange(onUserActivityStatusChanged: (Int, String) -> Unit) {
        this.onUserActivityStatusChange = onUserActivityStatusChanged
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            isJoined = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            if (this@AgoraManager::onUserActivityStatusChange.isInitialized) onUserActivityStatusChange(
                uid,
                "JOINED"
            )
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            if (this@AgoraManager::onUserActivityStatusChange.isInitialized) onUserActivityStatusChange(
                uid,
                "OFFLINE"
            )
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            if (state == Constants.REMOTE_VIDEO_STATE_STARTING) {
                if (this@AgoraManager::onUserActivityStatusChange.isInitialized) onUserActivityStatusChange(
                    uid,
                    "REMOTE_VIDEO_ON"
                )

            } else if (state == Constants.REMOTE_VIDEO_STATE_STOPPED) {
                if (this@AgoraManager::onUserActivityStatusChange.isInitialized) onUserActivityStatusChange(
                    uid,
                    "REMOTE_VIDEO_OFF"
                )
            }
        }
    }

    companion object {
        private val APP_ID = BuildConfig.AGORA_APP_ID
        private val TOKEN = BuildConfig.AGORA_TOKEN
        private const val LOCAL_USER_ID = 0
    }
}