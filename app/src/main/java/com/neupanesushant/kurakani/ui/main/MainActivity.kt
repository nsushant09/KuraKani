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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        val userManager = UserManager()
        if (intent.extras != null) {
            val friendUID = intent.extras!!.getString("friendUID")
            CoroutineScope(Dispatchers.Main).launch{
                userManager.getSelectedUser(friendUID!!){user ->
                    val chatMessagingFragment = ChatMessagingFragment(user)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container_view_tag, chatMessagingFragment)
                        commit()
                    }
                }
            }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatFragment)
            commit()
        }
    }
}