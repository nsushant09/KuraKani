package com.neupanesushant.kurakani.data.repository

import com.neupanesushant.kurakani.domain.model.User

interface UserRepo {
    suspend fun getSelectedUser(uid: String, callback : (User) -> Unit)
    suspend fun getAllUser()
}