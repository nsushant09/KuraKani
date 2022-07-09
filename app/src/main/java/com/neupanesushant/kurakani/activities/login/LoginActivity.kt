package com.neupanesushant.kurakani.activities.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainActivity
import com.neupanesushant.kurakani.activities.register.RegisterActivity
import com.neupanesushant.kurakani.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
//        FirebaseAuth.getInstance().signOut()
        if(firebaseAuth.currentUser != null){
            gotoMainActivity()
        }

        validateLogin()
        setSignUpClick()
    }


    fun setSignUpClick(){
        binding.tvSignUp.setOnClickListener {
            Intent(this@LoginActivity, RegisterActivity::class.java).apply{
                startActivity(this)
            }
        }
    }

    fun gotoMainActivity(){
        Intent(this, MainActivity::class.java).apply{
            startActivity(this)
            finish()
        }
    }

    fun validateLogin(){
        binding.btnLogin.setOnClickListener {

            when{
                TextUtils.isEmpty(binding.etEmail.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                TextUtils.isEmpty(binding.etPassword.text.toString().trim{it <= ' '}) -> {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                else -> {
                    val email: String = binding.etEmail.text.toString()
                    val password : String = binding.etPassword.text.toString()

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                        if(it.isSuccessful){
                            gotoMainActivity()
                        }else{
                            Toast.makeText(this, it.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        }
    }
}