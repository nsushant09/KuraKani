package com.neupanesushant.kurakani.ui.main.rtc

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.databinding.ActivityRtcactivityBinding
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.agora.AgoraManager
import com.neupanesushant.kurakani.domain.usecase.permission.PermissionManager
import io.agora.rtc2.video.VideoCanvas

class RTCActivity : AppCompatActivity() {

    enum class CommunicationType {
        AUDIO,
        VIDEO
    }

    private lateinit var binding: ActivityRtcactivityBinding
    private lateinit var agoraManager: AgoraManager

    private lateinit var user: User
    private lateinit var friend: User
    private lateinit var initialCommunicationType: CommunicationType

    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRtcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!getIntentData()) finish()

        agoraManager = AgoraManager(baseContext, getChannelId())
        agoraManager.setupVideoSDKEngine()

        setupView()
        setupEventListener()
        setupObserver()

        agoraManager.setOnUserActivityStatusChange(onUserActivityStatusChange)
        joinChannel()
    }

    private fun getIntentData(): Boolean {
        if (intent == null || intent.extras == null) return false
        user = intent.extras!!.getParcelable<User>("user") ?: return false
        friend = intent.extras!!.getParcelable<User>("friend") ?: return false
        initialCommunicationType =
            intent.extras!!.getSerializable("communicationType") as CommunicationType
        return true
    }

    private fun setupView() {
    }

    private fun setupEventListener() {
        binding.btnLeave.setOnClickListener {
            agoraManager.leaveChannel {
                this.finish()
            }
        }
    }

    private fun setupObserver() {

    }

    private fun joinChannel() {
        if (checkPermission()) {
            agoraManager.joinChannel(onChannelJoin)
            if (initialCommunicationType == CommunicationType.AUDIO) {
                agoraManager.getAgoraEngine { it?.enableAudio() }

            } else {
                agoraManager.getAgoraEngine { it?.enableVideo() }
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (initialCommunicationType == CommunicationType.AUDIO) {
            PermissionManager.hasRecordAudioPermission(this)
        } else {
            PermissionManager.hasRecordAudioPermission(this) && PermissionManager.hasCameraPermission(
                this
            )
        }
    }

    private val onChannelJoin: () -> Unit = {
        setupLocalVideo()
    }

    private fun setupLocalVideo() {
        runOnUiThread {
            val container = binding.localVideoViewContainer
            localSurfaceView = SurfaceView(baseContext)
            container.addView(localSurfaceView)
            agoraManager.getAgoraEngine {
                it?.setupLocalVideo(
                    VideoCanvas(
                        localSurfaceView,
                        VideoCanvas.RENDER_MODE_HIDDEN,
                        0
                    )
                )
            }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        runOnUiThread {
            val container = binding.remoteVideoViewContainer
            remoteSurfaceView = SurfaceView(baseContext)
            remoteSurfaceView?.setZOrderMediaOverlay(true)
            container.addView(remoteSurfaceView)
            agoraManager.getAgoraEngine {
                it?.setupRemoteVideo(
                    VideoCanvas(
                        remoteSurfaceView,
                        VideoCanvas.RENDER_MODE_FIT,
                        uid
                    )
                )
            }
            remoteSurfaceView?.visibility = View.VISIBLE
        }
    }

    private val onUserActivityStatusChange: (Int, String) -> Unit = { uid, action ->
        if (action == "JOINED") {
            setupRemoteVideo(uid)
        }
        if (action == "OFFLINE") {
            runOnUiThread {
                remoteSurfaceView?.visibility = View.GONE
            }
        }
    }

    private fun onAudioCall() {}
    private fun onVideoCall() {}

    private fun getChannelId(): String {
        return "AgoraRTCChannel"
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraManager.onDestroy()
    }

}