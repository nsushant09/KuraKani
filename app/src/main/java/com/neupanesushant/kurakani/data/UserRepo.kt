package com.neupanesushant.kurakani.data

import com.neupanesushant.kurakani.classes.User

interface UserRepo {

    suspend fun getSelectedUser(uid: String, callback : (User) -> Unit);
    suspend fun getAllUser();
}