package com.neupanesushant.kurakani.activities.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.fragments.ChatFragment
import com.neupanesushant.kurakani.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val chatFragment = ChatFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatFragment)
            commit()
        }


    }
}