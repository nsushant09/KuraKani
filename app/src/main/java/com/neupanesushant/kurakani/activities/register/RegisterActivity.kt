package com.neupanesushant.kurakani.activities.register

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.activities.main.MainActivity
import com.neupanesushant.kurakani.data.FirebaseInstance
import com.neupanesushant.kurakani.data.RegisterAndLogin
import com.neupanesushant.kurakani.databinding.ActivityRegisterBinding
import com.neupanesushant.kurakani.services.IMAGE_SELECTOR_REQUEST_CODE
import com.neupanesushant.kurakani.services.PermissionManager
import com.neupanesushant.kurakani.services.READ_EXTERNAL_STORAGE_PERMISSION_CODE
import com.neupanesushant.kurakani.services.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.Util
import org.koin.android.ext.android.inject
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var profileImageURI: Uri? = null
    private val registerAndLogin : RegisterAndLogin by inject()

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
            createNewUser()
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
    fun createNewUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val firstName = binding.etFirstname.text.toString()

        when {
            TextUtils.isEmpty(email.trim { it <= ' ' }) -> {
                Utils.showToast(this@RegisterActivity, "Please enter email ")
            }

            TextUtils.isEmpty(firstName.trim { it <= ' ' }) -> {
                Utils.showToast(this@RegisterActivity, "Please enter firstname")
            }

            TextUtils.isEmpty(password.trim { it <= ' ' }) -> {
                Utils.showToast(this@RegisterActivity, "Please enter password")
            }

            password.length < 8 -> {
                Utils.showToast(
                    this,
                    "You password should contain at least 8 letters"
                )
            }


            else -> {

                CoroutineScope(Dispatchers.Main).launch {
                    registerAndLogin.createNewUser(
                        email,
                        password,
                        object : RegisterAndLogin.Callback {
                            override fun onSuccess() {
                                saveUserToFirebaseDatabase()
                            }

                            override fun onFailure(failureReason: String) {
                                Utils.showToast(this@RegisterActivity, failureReason)
                            }
                        })
                }
            }
        }
    }

    private fun chooseImage() {
        if (PermissionManager.hasReadExternalStoragePermission(this)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_SELECTOR_REQUEST_CODE)
        } else {
            PermissionManager.requestReadExternalStoragePermission(this)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            profileImageURI = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageURI)
            val bitmapDrawable = BitmapDrawable(bitmap)
            binding.ivProfileImage.setImageDrawable(bitmapDrawable)
            binding.ivProfileImage.visibility = View.VISIBLE
            binding.tvChoosePhoto.visibility = View.GONE

        }
    }

    private fun saveUserToFirebaseDatabase() {
        CoroutineScope(Dispatchers.Main).launch {
            registerAndLogin.addUser(
                binding.etFirstname.text.toString(),
                binding.etLastname.text.toString(),
                profileImageURI,
                object : RegisterAndLogin.Callback {
                    override fun onSuccess() {
                        logIn()
                    }

                    override fun onFailure(failureReason: String) {
                        Toast.makeText(this@RegisterActivity, failureReason, Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            )
        }
    }

    private fun logIn() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        CoroutineScope(Dispatchers.Main).launch {
            registerAndLogin.login(email, password, object : RegisterAndLogin.Callback {
                override fun onSuccess() {
                    Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(this)
                        finish()
                    }
                }

                override fun onFailure(failureReason: String) {
                    Utils.showToast(this@RegisterActivity, failureReason)
                }
            })
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