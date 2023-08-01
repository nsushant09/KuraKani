package com.neupanesushant.kurakani.domain.usecase.audiorecorder

import java.io.File

interface AudioRecorder {
    fun start() : File
    fun stop()
}