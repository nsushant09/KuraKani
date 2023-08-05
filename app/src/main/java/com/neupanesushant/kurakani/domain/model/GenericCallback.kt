package com.neupanesushant.kurakani.domain.model

interface GenericCallback<T> {
    fun execute(result: GenericResult<T>)
}

sealed class GenericResult<out T> {
    data class Success<T>(val data: T?) : GenericResult<T>()
    data class Failure(val exception: Exception) : GenericResult<Nothing>()
}