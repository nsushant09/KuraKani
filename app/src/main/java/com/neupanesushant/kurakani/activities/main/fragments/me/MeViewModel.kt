package com.neupanesushant.kurakani.activities.main.fragments.me

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.data.FirebaseInstance

class MeViewModel(application: Application) : AndroidViewModel(application) {

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


    private fun updateUser(user: User) {
        FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
            .setValue(user)
    }
}