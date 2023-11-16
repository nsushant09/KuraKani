package com.neupanesushant.kurakani.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.databinding.ActivityLoginBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.usecase.validator.LoginValidator
import com.neupanesushant.kurakani.domain.usecase.validator.Validator
import com.neupanesushant.kurakani.ui.main.MainActivity
import com.neupanesushant.kurakani.ui.register.RegisterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val registerAndLogin: RegisterAndLogin by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseInstance.firebaseAuth.currentUser != null) {
            gotoMainActivity()
        }

        setupView()
        setupEventListener()
        setupObserver()
    }

    private fun setupView() {
    }

    private fun setupObserver() {
    }

    private fun setupEventListener() {
        binding.llSignUp.setOnClickListener {
            Intent(this@LoginActivity, RegisterActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            val validator: Validator =
                LoginValidator(email, password)
            val (isValid, errorMessage) = validator.isValid()
            if (!isValid) {
                Utils.showToast(this, errorMessage)
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String)  {

        CoroutineScope(Dispatchers.IO).launch {

            val result = registerAndLogin.login(email, password)
            if(result.user != null){
                gotoMainActivity()
            }
        }
    }

    private fun gotoMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun String.isEmptyAfterTrim(): Boolean {
        return TextUtils.isEmpty(this.trim { it <= ' ' })
    }
}