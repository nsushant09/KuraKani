package com.neupanesushant.kurakani.activities.main.fragments.me

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.FirebaseInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MeViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    fun setUser(user: User?) {
        _user.value = user
    }

    fun addImageToDatabase(fileName: String, profileImageURI: Uri?) {
        val ref = FirebaseInstance.firebaseStorage.getReference("/images/$fileName")
        ref.putFile(profileImageURI!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                updateUser(
                    User(
                        user.value?.uid,
                        user.value?.firstName,
                        user.value?.lastName,
                        user.value?.fullName,
                        it.toString()
                    )
                )
            }
        }
    }


    fun updateUser(user: User) {
        FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
            .setValue(user)
    }
}