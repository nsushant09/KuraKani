package com.neupanesushant.kurakani.activities.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.UserManager
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel() : ViewModel(), KoinComponent, FirebaseInstance {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    private val userManager: UserManager by inject()

    init {
        getUserFromDatabase()
    }

    fun getUserFromDatabase() {
        uiScope.launch {
            userManager.getSelectedUser(firebaseAuth.uid!!) { user ->
                _user.postValue(user)
            }
        }
    }

}