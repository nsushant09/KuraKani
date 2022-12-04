package com.neupanesushant.kurakani.activities.main.fragments.me

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.login.LoginActivity
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.databinding.FragmentMeBinding
import java.util.*


class MeFragment : Fragment() {

    private lateinit var _binding: FragmentMeBinding
    private val binding get() = _binding
    private lateinit var viewModel: MeViewModel
    private val IMAGE_SELECTOR_REQUEST_CODE = 111223344

    private var profileImageURI: Uri? = null
    private val mainViewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMeBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MeViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBtnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rlSignOut.setOnClickListener {
            btnSignOutAction()
        }

        //set user details
        mainViewModel.user.observe(viewLifecycleOwner, Observer {
            viewModel.setUser(it)
            binding.tvUserName.text = it?.fullName
            Glide.with(requireContext()).load(it?.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
        })

        //choose new image for user profile
        binding.relativeLayoutUserProfileImageAndIcon.setOnClickListener {
            chooseImage()
        }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_SELECTOR_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            profileImageURI = data.data!!
            val bitmap =
                MediaStore.Images.Media.getBitmap(activity?.contentResolver, profileImageURI)
            val bitmapDrawable = BitmapDrawable(bitmap)
            binding.ivUserProfilePicture.setImageDrawable(bitmapDrawable)

            updateUserInfo()
        }
    }

    fun updateUserInfo() {
        if (profileImageURI == null) return
        val fileName = UUID.randomUUID().toString()
        viewModel.addImageToDatabase(fileName, profileImageURI)
        mainViewModel.getUserFromDatabase()
    }

    fun btnSignOutAction() {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    FirebaseAuth.getInstance().signOut()
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(this)
                    }
                }


            })
            .setNegativeButton("Cancle", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    p0?.cancel()
                }

            })

        alertDialog.create()
        alertDialog.show()
    }
}