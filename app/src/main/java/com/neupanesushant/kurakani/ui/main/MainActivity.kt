package com.neupanesushant.kurakani.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.ui.main.fragments.chat.ChatFragment
import com.neupanesushant.kurakani.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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