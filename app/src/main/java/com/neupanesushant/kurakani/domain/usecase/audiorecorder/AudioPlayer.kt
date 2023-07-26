package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import android.media.MediaPlayer
import java.io.File

interface AudioPlayer {
    fun play(file: File)
    fun play(url: String, callback: (MediaPlayer) -> Unit)
    fun pause()
    fun resume()
    fun stop()
    fun getAudioPlayer(): MediaPlayer?
    fun seekTo(progress : Int)
}