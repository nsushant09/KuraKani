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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.neupanesushant.kurakani.activities.main.MainActivity
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.ActivityRegisterBinding
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val TAG: String = "RegisterActivity"
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var profileImageURI: Uri? = null
    private var profileImageURL: String = ""
    private var isImageAlsoUplaoded = false

    private val IMAGE_SELECTOR_REQUEST_CODE = 111223344

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        setLogInClick()

        binding.btnSignUp.setOnClickListener {
            createNewUser()
        }

        binding.tvChoosePhoto.setOnClickListener {
            chooseImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun createNewUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val firstName = binding.etFirstname.text.toString()
        val lastName = binding.etLastname.text.toString()

        when {
            TextUtils.isEmpty(email.trim { it <= ' ' }) -> {
                Toast.makeText(this@RegisterActivity, "Please enter email ", Toast.LENGTH_SHORT)
                    .show()
            }

            TextUtils.isEmpty(firstName.trim { it <= ' ' }) -> {
                Toast.makeText(this@RegisterActivity, "Please enter password", Toast.LENGTH_SHORT)
                    .show()
            }

            TextUtils.isEmpty(password.trim { it <= ' ' }) -> {
                Toast.makeText(this@RegisterActivity, "Please enter password", Toast.LENGTH_SHORT)
                    .show()
            }

            password.length < 8 -> {
                Toast.makeText(
                    this,
                    "You password should contain at least 8 letters",
                    Toast.LENGTH_SHORT
                ).show()
            }


            else -> {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            uploadImageToFirebaseStorage()
                            val firebaseUser: FirebaseUser = it.result!!.user!!
                            Intent(this@RegisterActivity, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", firebaseUser.email)
                                startActivity(this)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }

            }
        }
    }

    fun chooseImage() {
        Log.i(TAG, "GRANTED PERMISSION")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_SELECTOR_REQUEST_CODE)
    }

    fun setLogInClick() {
        binding.tvLogIn.setOnClickListener {
            finish()
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun uploadImageToFirebaseStorage() {
        if (checkPermissions()) {
            if (profileImageURI == null) {
                saveUserToFirebaseDatabase()
                return
            }
            isImageAlsoUplaoded = true
            val fileName = UUID.randomUUID().toString()
            val ref = firebaseStorage.getReference("/images/$fileName")
            ref.putFile(profileImageURI!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    profileImageURL = it.toString()
                    saveUserToFirebaseDatabase()
                }
            }.addOnFailureListener {
                Toast.makeText(baseContext, "Could not upload Image ", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun saveUserToFirebaseDatabase() {
        val ref = firebaseDatabase.getReference("/users/${firebaseAuth.uid}")
        val user: User = User(
            firebaseAuth.uid!!,
            binding.etFirstname.text.toString(),
            binding.etLastname.text.toString(),
            profileImageURL
        )
        ref.setValue(user)
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 11)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadImageToFirebaseStorage()
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}