package com.neupanesushant.kurakani.activities.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.register.RegisterActivity
import com.neupanesushant.kurakani.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSignUpClick()
    }


    fun setSignUpClick(){
        binding.tvSignUp.setOnClickListener {
            Intent(this@LoginActivity, RegisterActivity::class.java).apply{
                startActivity(this)
            }
        }
    }
}