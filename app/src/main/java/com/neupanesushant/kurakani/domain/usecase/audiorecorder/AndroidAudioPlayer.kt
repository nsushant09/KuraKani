package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File


class AndroidAudioPlayer(private val context: Context) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    override fun play(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            mediaPlayer = this
            try {
                start()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun play(url: String, callback: (MediaPlayer) -> Unit) {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync()
            mediaPlayer = this
        }.setOnPreparedListener {
            it.start()
            callback(it)
        }
    }

    override fun pause() {
        mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }

    }

    override fun resume() {
        mediaPlayer?.let { mediaPlayer ->
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        try {
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    override fun getAudioPlayer(): MediaPlayer? {
        return mediaPlayer
    }

    override fun seekTo(progress: Int) {
        mediaPlayer?.seekTo(progress)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

}