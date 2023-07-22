package com.neupanesushant.kurakani.services.audiorecorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile : File)
    fun stop()
}