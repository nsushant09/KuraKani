package com.neupanesushant.kurakani.ui.register

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Tasks
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.databinding.ActivityRegisterBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.usecase.validator.RegistrationValidator
import com.neupanesushant.kurakani.domain.usecase.validator.Validator
import com.neupanesushant.kurakani.services.IMAGE_SELECTOR_REQUEST_CODE
import com.neupanesushant.kurakani.services.READ_EXTERNAL_STORAGE_PERMISSION_CODE
import com.neupanesushant.kurakani.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var profileImageURI: Uri? = null
    private val registerAndLogin: RegisterAndLogin by inject()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupEventListener()
        setupObserver()
    }

    private fun setupView() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupEventListener() {
        binding.btnSignUp.setOnClickListener {
            validateRegistrationDetails()
        }

        binding.tvChoosePhoto.setOnClickListener {
            chooseImage()
        }

        binding.llLogIn.setOnClickListener {
            finish()
        }
    }

    private fun setupObserver() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun validateRegistrationDetails() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val firstName = binding.etFirstname.text.toString()

        val validator: Validator = RegistrationValidator(firstName, email, password)
        val (isValid, errorMessage) = validator.isValid()
        if (!isValid) {
            Utils.showToast(this, errorMessage)
            return
        }
        createNewUser(email, password)
    }

    private fun createNewUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = registerAndLogin.createNewUser(email, password)
            if (result.user != null) {
                saveUserToFirebaseDatabase()
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(intent, "Select Images"),
            IMAGE_SELECTOR_REQUEST_CODE
        )
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            profileImageURI = data.data!!

            Glide.with(this)
                .load(profileImageURI)
                .apply(RequestOptions().centerCrop())
                .into(binding.ivProfileImage)


            binding.ivProfileImage.visibility = View.VISIBLE
            binding.tvChoosePhoto.visibility = View.GONE

        }
    }

    private fun saveUserToFirebaseDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val isSuccessfulTask = registerAndLogin.addUser(
                binding.etFirstname.text.toString(),
                binding.etLastname.text.toString(),
                profileImageURI
            )
            if(isSuccessfulTask){
                logIn()
            }
        }
    }

    private fun logIn() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            val result = registerAndLogin.login(email, password)
            if (result.user != null) {
                gotoMainActivity()
            }
        }
    }

    private fun gotoMainActivity() {
        Intent(this@RegisterActivity, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImage()
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}