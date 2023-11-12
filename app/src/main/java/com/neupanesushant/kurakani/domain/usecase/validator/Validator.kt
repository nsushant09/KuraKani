package com.neupanesushant.kurakani.domain.usecase.validator

interface Validator {
    fun isValid() : Pair<Boolean, String>
}