package com.neupanesushant.kurakani.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel() : ViewModel(), KoinComponent {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val userManager: UserManager by inject()

    init {
        getUserFromDatabase()
    }

    fun getUserFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = userManager.getSelectedUser(FirebaseInstance.firebaseAuth.uid!!)
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                AuthenticatedUser.getInstance().setUser(user)
                _user.postValue(user)
                getFCMToken()
            }
        }
    }

    private fun getFCMToken() {
        FirebaseInstance.firebaseCloudMessage.token.addOnCompleteListener { task ->
            val user = AuthenticatedUser.getInstance().getUser() ?: return@addOnCompleteListener
            if (task.isSuccessful && task.result != user.fcmToken) {
                user.fcmToken = task.result
                FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
                    .setValue(user)
                    .addOnSuccessListener {
                        AuthenticatedUser.getInstance().setUser(user)
                    }
            }
        }
    }
}