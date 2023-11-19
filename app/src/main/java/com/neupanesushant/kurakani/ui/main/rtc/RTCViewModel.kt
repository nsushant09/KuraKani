package com.neupanesushant.kurakani.ui.main.rtc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent

class RTCViewModel : ViewModel(), KoinComponent {

    private val _isVideoEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _isAudioEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    val isVideoEnabled: LiveData<Boolean> get() = _isVideoEnabled
    val isAudioEnabled: LiveData<Boolean> get() = _isAudioEnabled

    fun toggleAudio() {
        _isAudioEnabled.value = !isAudioEnabled.value!!
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !isVideoEnabled.value!!
    }

    fun setIsVideoEnabled(boolean: Boolean) {
        _isVideoEnabled.value = boolean
    }

    fun setIsAudioEnabled(boolean: Boolean) {
        _isAudioEnabled.value = boolean
    }
}