package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter

import android.content.Context
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.audiorecorder.AndroidAudioPlayer
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ChatAudioToLayoutBinding

class ViewHolderToAudio(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatAudioToLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var context: Context
    private var seekBar: SeekBar
    private var btnPlayPause: ImageView
    private var profileImage: ImageView
    private var audioPlayer: AndroidAudioPlayer
    private var isPlayedFirstTime = false
    private var isPlaying = false

    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    init {
        profileImage = binding.ivProfileImage
        btnPlayPause = binding.btnPlayPauseAudio
        seekBar = binding.seekBarAudio
        context = chatMessageAdapter.context
        audioPlayer = AndroidAudioPlayer(context)
        pauseAudioActions()

        seekBar.isEnabled = true

    }

    override fun bind(position: Int) {
        val url = chatMessageAdapter.list[position].messageBody ?: ""

        runnable = Runnable {
            seekBar.progress = audioPlayer.getCurrentPosition()
            handler.postDelayed(runnable, 1000)
        }

        if (position != 0 && chatMessageAdapter.list[position - 1].fromUid == chatMessageAdapter.list[position].fromUid) {
            Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.friendUser.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
        } else {
            profileImage.visibility = View.INVISIBLE
        }

        btnPlayPause.setOnClickListener {
            if (!isPlayedFirstTime) {
                audioPlayer.play(url) {
                    seekBar.max = it.duration
                    handler.postDelayed(runnable, 1000)
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
                }
            }
        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekbar: SeekBar?,
                progress: Int,
                changeFromUser: Boolean
            ) {
                if (progress == audioPlayer.getDuration() - 1000) {
                    pauseAudioActions()
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