package com.neupanesushant.kurakani.domain.usecase

import com.neupanesushant.kurakani.domain.model.User

class AuthenticatedUser private constructor() {
    private var user: User? = null

    fun setUser(user: User) {
        this.user = user
    }

    fun getUser(): User? {
        return user
    }

    fun getUID(): String? {
        return user?.uid
    }

    companion object {
        private val instance = AuthenticatedUser()
        fun getInstance(): AuthenticatedUser {
            return instance
        }
    }

}