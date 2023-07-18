package com.neupanesushant.kurakani.services

import com.neupanesushant.kurakani.classes.User

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