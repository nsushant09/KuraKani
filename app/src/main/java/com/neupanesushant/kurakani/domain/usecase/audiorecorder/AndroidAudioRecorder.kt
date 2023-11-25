package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var file: File? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()

    }

    override fun start(): File {
        val file = File(
            context.cacheDir,
            System.currentTimeMillis().toString() + ".mp3"
        ).also { file ->
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(file).fd)

                prepare()
                start()

                recorder = this
            }
        }
        return file
    }

    fun onMotionEventDown(onEventDown: () -> Unit) {
        file = start()
        onEventDown()
    }

    fun onMotionEventUp(onEventUp: (File) -> Unit) {
        stop()
        file?.let(onEventUp)
    }

    override fun stop() {
        try {
            recorder?.stop()
            recorder?.reset()
            recorder = null
        } catch (_: Exception) {
        }
    }

}