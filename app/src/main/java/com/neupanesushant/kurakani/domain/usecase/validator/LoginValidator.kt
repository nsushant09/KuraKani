package com.neupanesushant.kurakani.domain.usecase.validator

import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.Utils.isEmptyAfterTrim

class LoginValidator(
    private val email : String,
    private val password : String
) : Validator {
    override fun isValid(): Pair<Boolean, String>{
        if (email.isEmptyAfterTrim()) {
            return Pair(false, "Please enter email")
        }

        if (password.isEmptyAfterTrim()) {
            return Pair(false, "Please enter password")
        }

        return Pair(true, "")
    }
}