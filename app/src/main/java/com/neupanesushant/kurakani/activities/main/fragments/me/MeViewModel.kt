package com.neupanesushant.kurakani.activities.main.fragments.me

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*

class MeViewModel(application : Application) : AndroidViewModel(application) {

    private val TAG = "MeViewModel"
    private val  firebaseAuth: FirebaseAuth
    private val firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: DatabaseReference

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _user = MutableLiveData<User?>()
    val user get() = _user

    init{
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("/users/${firebaseAuth.uid}")

    }

    fun setUser(user: User?){
        _user.value = user
    }

    fun addImageToDatabase(fileName : String, profileImageURI : Uri?){
        val ref = firebaseStorage.getReference("/images/$fileName")
        ref.putFile(profileImageURI!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                updateUser(User(user.value?.uid, user.value?.firstName, user.value?.lastName, user.value?.fullName, it.toString()))
            }
        }
    }


    fun updateUser(user : User) {
        firebaseDatabase.setValue(user)
    }
}