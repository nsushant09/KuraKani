package com.neupanesushant.kurakani.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.databinding.ActivityLoginBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.ui.main.MainActivity
import com.neupanesushant.kurakani.ui.register.RegisterActivity
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

            val email: String = binding.etEmail.text.toString()
            val password: String = binding.etPassword.text.toString()

            if (email.isEmptyAfterTrim()) {
                Utils.showToast(this, "Please enter email")
                return@setOnClickListener
            }

            if (password.isEmptyAfterTrim()) {
                Utils.showToast(this, "Please enter password")
                return@setOnClickListener
            }

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