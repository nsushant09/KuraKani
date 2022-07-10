package com.neupanesushant.kurakani.activities.main.fragments.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neupanesushant.kurakani.classes.User
import kotlinx.coroutines.*

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SearchViewModel"
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private val _allUsers = MutableLiveData<List<User>>()
    val allUser get() = _allUsers

    private val _searchedList = MutableLiveData<List<User>>()
     val searchedList get() = _searchedList

    init{
        getAllUsersFromDatabase()
    }

    private fun getAllUsersFromDatabase() {
        uiScope.launch {
            getAllUsersSuspended()
        }
    }

    suspend fun getAllUsersSuspended() {
        withContext(Dispatchers.IO) {
            val ref = FirebaseDatabase.getInstance().getReference().child("users")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = ArrayList<User>()
                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java)
                        if(it!=null){
                            tempList.add(user!!)
                        }
                        Log.i(TAG, "The name of the user is : " + user?.fullName)
                    }
                    _allUsers.value = tempList.toList()
                    _searchedList.value = _allUsers.value
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancelled")
                }

            })
        }
    }

    fun filterSearch(searchedText : String){
        val tempList = ArrayList<User>()
        _allUsers.value?.forEach{
            if(isStringInName(it.fullName, searchedText)){
                tempList.add(it)
            }
            _searchedList.value = tempList
        }

    }

    fun isStringInName(name : String?, target : String) : Boolean{
        val lengthOfTarget = target.length
        var lengthOfName = name?.length
        if(lengthOfName == null){
            lengthOfName = 0
        }
        val loopSize = lengthOfName - lengthOfTarget
        for(i in 0 until loopSize + 1){
            try {
                if (name?.substring(i, lengthOfTarget + i).equals(target, true)) {
                    return true
                }
            }catch (e : Exception){
                continue
            }
        }
        return false

    }
}