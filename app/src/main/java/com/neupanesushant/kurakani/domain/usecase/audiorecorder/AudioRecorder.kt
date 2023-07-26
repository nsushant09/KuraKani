package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile : File)
    fun stop()
}