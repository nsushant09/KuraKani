package com.neupanesushant.kurakani.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class UserManager : UserRepo {

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

    override suspend fun getAllUser() {
        withContext(Dispatchers.IO) {
            FirebaseInstance.firebaseDatabase.reference
                .child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tempList = ArrayList<User>()
                        snapshot.children.forEach {
                            it.getValue(User::class.java)?.let { user ->
                                tempList.add(user)
                            }
                        }
                        allUsers.value = tempList
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
    }
}