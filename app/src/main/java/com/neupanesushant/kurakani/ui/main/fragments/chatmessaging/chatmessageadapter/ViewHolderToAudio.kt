package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.chatmessageadapter

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ChatAudioToLayoutBinding
import com.neupanesushant.kurakani.domain.usecase.audiorecorder.AndroidAudioPlayer

class ViewHolderToAudio(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatAudioToLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var context: Context
    private var seekBar: SeekBar
    private var btnPlayPause: ImageView
    private var audioPlayer: AndroidAudioPlayer
    private var profileImage: ImageView
    private var layout: ConstraintLayout
    private var isPlayedFirstTime = false
    private var isPlaying = false

    private val maxFramesSpeed: Long = 120

    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var progressAnimator: ObjectAnimator
    private lateinit var runnable: Runnable

    init {
        layout = binding.layout
        profileImage = binding.ivProfileImage
        btnPlayPause = binding.btnPlayPauseAudio
        seekBar = binding.seekBarAudio
        context = chatMessageAdapter.context
        audioPlayer = AndroidAudioPlayer(context)
        pauseAudioActions()
    }

    override fun bind(position: Int) {
        val message = chatMessageAdapter.list[position]
        val url = message.messageBody ?: ""

        if (position == 0 || chatMessageAdapter.list[position - 1].fromUid == chatMessageAdapter.list[position].fromUid) {
            Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.friendUser.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
        } else {
            profileImage.visibility = View.INVISIBLE
        }

        runnable = Runnable {
            val newProgress =
                if (audioPlayer.getCurrentPosition() < seekBar.max - maxFramesSpeed) audioPlayer.getCurrentPosition() else seekBar.max

            progressAnimator =
                ObjectAnimator.ofInt(seekBar, "progress", seekBar.progress, newProgress)
            progressAnimator.duration = maxFramesSpeed
            progressAnimator.start()
            handler.postDelayed(runnable, maxFramesSpeed)
        }

        layout.setOnLongClickListener {
            chatMessageAdapter.onLongClickAction(message)
            true
        }

        btnPlayPause.setOnClickListener {
            if (!isPlayedFirstTime) {
                audioPlayer.play(url) {
                    seekBar.max = it.duration
                    handler.post(runnable)
                }
                isPlayedFirstTime = true
                playAudioActions()
            } else {
                if (isPlaying) {
                    pauseAudioActions()
                    audioPlayer.pause()
                } else {
                    playAudioActions()
                    audioPlayer.resume()
                    handler.post(runnable)
                }
            }
        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekbar: SeekBar?,
                progress: Int,
                changeFromUser: Boolean
            ) {
                seekbar?.let {
                    if (progress == seekbar.max) {
                        pauseAudioActions()
                        handler.removeCallbacks(runnable)
                        progressAnimator.end()
                        seekbar.progress = 0
                    }
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.let {
                    audioPlayer.seekTo(seekbar.progress)
                }
            }

        })

    }

    private fun pauseAudioActions() {
        isPlaying = false

        btnPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
        btnPlayPause.animation =
            AnimationUtils.loadAnimation(context, com.bumptech.glide.R.anim.abc_fade_in)
    }

    private fun playAudioActions() {
        isPlaying = true

        btnPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause))
        btnPlayPause.animation =
            AnimationUtils.loadAnimation(context, com.bumptech.glide.R.anim.abc_fade_in)
    }
}