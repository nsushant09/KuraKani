package com.neupanesushant.kurakani.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.databinding.ActivityMainBinding
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.ui.main.fragments.chat.ChatFragment
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.ChatMessagingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val chatFragment = ChatFragment()
    private val userManager = UserManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        if (intent.extras != null) {
            val friendUID = intent.extras!!.getString("friendUID")
            navigateToChatMessagingFragmentFromUser(friendUID ?: "")
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatFragment)
            commit()
        }
    }

    private fun navigateToChatMessagingFragmentFromUser(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {

            if (userManager.allUsers.value.filter { it.uid == uid }.isNotEmpty()) {
                val user = userManager.allUsers.value.filter { it.uid == uid }.get(0)
                chatMessagingFragment(user)
                return@launch
            }

            userManager.getSelectedUser(uid) { user ->
                chatMessagingFragment(user)
            }

        }
    }

    private fun chatMessagingFragment(user: User) {
        CoroutineScope(Dispatchers.Main).launch {
            val chatMessagingFragment = ChatMessagingFragment(user)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag, chatMessagingFragment)
                commit()
            }

        }
    }
}