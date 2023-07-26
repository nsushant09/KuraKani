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

    companion object {
        private val instance = AuthenticatedUser()
        fun getInstance(): AuthenticatedUser {
            return instance
        }
    }

}