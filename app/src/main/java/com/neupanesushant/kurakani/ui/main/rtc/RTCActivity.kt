package com.neupanesushant.kurakani.ui.main.rtc

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ActivityRtcactivityBinding
import com.neupanesushant.kurakani.domain.Utils.convertToDp
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.agora.AgoraManager
import com.neupanesushant.kurakani.domain.usecase.extras.GlideBlurTransformation
import com.neupanesushant.kurakani.domain.usecase.permission.PermissionManager
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration.MIRROR_MODE_TYPE
import org.koin.android.ext.android.inject

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

    private val viewModel: RTCViewModel by inject()


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
        onVideoOff(binding.remoteVideoViewContainer, friend)
        onVideoOff(binding.localVideoViewContainer, user)
    }

    private fun setupEventListener() {
        binding.ivCamera.setOnClickListener {
            viewModel.toggleVideo()
        }
        binding.ivMic.setOnClickListener {
            viewModel.toggleAudio()
        }
        binding.ivRotateCamera.setOnClickListener {
            agoraManager.getAgoraEngine { it?.switchCamera() }
        }
        binding.btnLeave.setOnClickListener {
            agoraManager.leaveChannel {
                this.finish()
            }
        }
    }

    private fun setupObserver() {
        viewModel.isVideoEnabled.observeForever { isEnabled ->
            agoraManager.getAgoraEngine { it?.enableLocalVideo(isEnabled) }
            setCameraIcon(isEnabled)
        }
        viewModel.isAudioEnabled.observeForever { isEnabled ->
            agoraManager.getAgoraEngine { it?.muteLocalVideoStream(!isEnabled) }
            setMicrophoneIcon(isEnabled)
        }
    }

    private fun joinChannel() {
        if (checkPermission()) {
            agoraManager.joinChannel()
            if (initialCommunicationType == CommunicationType.AUDIO) {
                agoraManager.getAgoraEngine {
                    it?.enableAudio()
                    viewModel.setIsAudioEnabled(true)
                }

            } else {
                agoraManager.getAgoraEngine {
                    it?.enableVideo()
                    viewModel.setIsAudioEnabled(true)
                    viewModel.setIsVideoEnabled(true)
                }
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

    private fun setupLocalVideo() {
        runOnUiThread {
            val container = binding.localVideoViewContainer
            val cardView = getCardView()
            localSurfaceView = SurfaceView(baseContext)
            cardView.addView(localSurfaceView)
            container.removeAllViews()
            container.addView(cardView)
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
            val cardView = getCardView()
            remoteSurfaceView = SurfaceView(baseContext)
            cardView.addView(remoteSurfaceView)
            container.removeAllViews()
            container.addView(cardView)
            agoraManager.getAgoraEngine {
                it?.setupRemoteVideo(
                    VideoCanvas(
                        remoteSurfaceView,
                        VideoCanvas.RENDER_MODE_HIDDEN,
                        MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED.value,
                        uid
                    )
                )
            }
            remoteSurfaceView?.visibility = View.VISIBLE
        }
    }

    private val onUserActivityStatusChange: (Int, String) -> Unit = { uid, action ->
        if (action == "JOINED") setupRemoteVideo(uid)
        if (action == "OFFLINE") agoraManager.leaveChannel { this.finish() }
        if (action == "REMOTE_VIDEO_ON") setupRemoteVideo(uid)
        if (action == "REMOTE_VIDEO_OFF") onVideoOff(binding.remoteVideoViewContainer, friend)
    }


    private fun getCardView(): CardView {
        val cardView = CardView(baseContext)
        cardView.radius = convertToDp(8f)
        return cardView
    }

    private fun setCameraIcon(isEnabled: Boolean) {
        if (isEnabled) {
            binding.ivCamera.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_vid_cam
                )
            )
            setupLocalVideo()
        } else {
            binding.ivCamera.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_vid_cam_cut
                )
            )
            onVideoOff(binding.localVideoViewContainer, user)
        }
    }

    private fun setMicrophoneIcon(isEnabled: Boolean) {
        if (isEnabled) {
            binding.ivMic.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_microphone
                )
            )
        } else {
            binding.ivMic.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_microphone_cut
                )
            )
        }
    }

    private fun onVideoOff(container: FrameLayout, user: User) {
        runOnUiThread {
            val cardView = getCardView()
            val imageView = ImageView(baseContext)
            Glide.with(this)
                .load(user.profileImage)
                .transform(
                    MultiTransformation(GlideBlurTransformation(this), CenterCrop())
                )
                .into(imageView)
            cardView.addView(imageView)
            container.removeAllViews()
            container.addView(cardView)
        }
    }

    private fun getChannelId(): String {
        return "AgoraRTCChannel"
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraManager.onDestroy()
    }

}