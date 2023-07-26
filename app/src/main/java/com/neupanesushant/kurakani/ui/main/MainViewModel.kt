package com.neupanesushant.kurakani.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import kotlinx.coroutines.*
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
        uiScope.launch {
            userManager.getSelectedUser(FirebaseInstance.firebaseAuth.uid!!) { user ->
                AuthenticatedUser.getInstance().setUser(user)
                _user.postValue(user)
            }
        }
    }

}