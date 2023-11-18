package com.neupanesushant.kurakani.data

import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.data.repository.UserRepo
import com.neupanesushant.kurakani.domain.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class UserManager : UserRepo, ValueEventListener {

    val users: MutableStateFlow<User?> = MutableStateFlow(null)

    suspend fun getSelectedUser(uid: String): DataSnapshot = coroutineScope{
        val job = async {FirebaseInstance.firebaseDatabase.reference.child("users").child(uid).get()}
        await(job.await())
    }

    override suspend fun getAllUser() = coroutineScope {
        FirebaseInstance.firebaseDatabase.reference
            .child("users")
            .addListenerForSingleValueEvent(this@UserManager)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        snapshot.children.forEach {
            it.getValue(User::class.java)?.let { user ->
                users.value = user
            }
        }
    }

    override fun onCancelled(error: DatabaseError) {}
}