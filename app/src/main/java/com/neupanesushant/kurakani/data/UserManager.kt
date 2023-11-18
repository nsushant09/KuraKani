package com.neupanesushant.kurakani.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.repository.UserRepo
import com.neupanesushant.kurakani.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class UserManager : UserRepo, ValueEventListener {

    val allUsers: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())

    override suspend fun getSelectedUser(uid: String, callback: (User) -> Unit) {
        withContext(Dispatchers.IO) {
            FirebaseInstance.firebaseDatabase.reference.child("users").child(uid).get()
                .addOnSuccessListener {
                    it.getValue(User::class.java)?.let { user ->
                        callback(user)
                    }
                }
        }
    }

    override suspend fun getAllUser() = coroutineScope {
        FirebaseInstance.firebaseDatabase.reference
            .child("users")
            .addListenerForSingleValueEvent(this@UserManager)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val tempList = ArrayList<User>()
        snapshot.children.forEach {
            it.getValue(User::class.java)?.let { user ->
                tempList.add(user)
            }
        }
        allUsers.value = tempList
    }

    override fun onCancelled(error: DatabaseError) {}
}