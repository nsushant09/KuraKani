package com.neupanesushant.kurakani.view.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.neupanesushant.kurakani.view.main.MainActivity
import com.neupanesushant.kurakani.view.register.RegisterActivity
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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


            if (TextUtils.isEmpty(binding.etEmail.text.toString().trim { it <= ' ' })) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' })) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val email: String = binding.etEmail.text.toString()
                val password: String = binding.etPassword.text.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    registerAndLogin.login(email, password, object : RegisterAndLogin.Callback {
                        override fun onSuccess() {
                            gotoMainActivity()
                        }

                        override fun onFailure(failureReason: String) {
                            Toast.makeText(
                                this@LoginActivity,
                                failureReason ?: "Could not login",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })
                }
            }

        }
    }

    private fun gotoMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }
}