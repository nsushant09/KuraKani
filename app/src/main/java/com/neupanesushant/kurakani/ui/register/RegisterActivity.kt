package com.neupanesushant.kurakani.ui.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.databinding.ActivityRegisterBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.usecase.permission.PermissionManager
import com.neupanesushant.kurakani.domain.usecase.validator.RegistrationValidator
import com.neupanesushant.kurakani.domain.usecase.validator.Validator
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
            if (PermissionManager.hasReadExternalStoragePermission(this)) {
                chooseImage()
            } else {
                PermissionManager.requestReadExternalStoragePermission(this)
                externalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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
        Toast.makeText(this, "Creating your account", Toast.LENGTH_LONG).show()
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
        imageChooserLauncher.launch(Intent.createChooser(intent, "Select Images"))
    }


    private val imageChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val intent = it.data ?: return@registerForActivityResult
        profileImageURI = intent.data!!
        Glide.with(this)
            .load(profileImageURI)
            .apply(RequestOptions().centerCrop())
            .into(binding.ivProfileImage)


        binding.ivProfileImage.visibility = View.VISIBLE
        binding.tvChoosePhoto.visibility = View.GONE
    }

    private val externalStorageLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!it) {
            Utils.showToast(this@RegisterActivity, "Denied Storage Permission")
            return@registerForActivityResult
        }
        chooseImage()
    }

    private fun saveUserToFirebaseDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val isSuccessfulTask = registerAndLogin.addUser(
                binding.etFirstname.text.toString(),
                binding.etLastname.text.toString(),
                profileImageURI
            )
            if (isSuccessfulTask) {
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
}