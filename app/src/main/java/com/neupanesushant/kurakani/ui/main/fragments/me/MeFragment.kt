package com.neupanesushant.kurakani.ui.main.fragments.me

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.databinding.FragmentMeBinding
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import com.neupanesushant.kurakani.ui.login.LoginActivity
import com.neupanesushant.kurakani.ui.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MeFragment : Fragment() {

    private lateinit var _binding: FragmentMeBinding
    private val binding get() = _binding
    private lateinit var viewModel: MeViewModel

    private var profileImageURI: Uri? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMeBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MeViewModel::class.java]
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
        AuthenticatedUser.getInstance().getUser()?.let {
            setupUserDetails(it)
        }

        //choose new image for user profile
        binding.relativeLayoutUserProfileImageAndIcon.setOnClickListener {
            chooseImage()
        }
    }

    private fun setupUserDetails(user: User) {
        binding.tvUserName.text = user.fullName
        Glide.with(requireContext()).load(user.profileImage)
            .apply(RequestOptions().circleCrop())
            .error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageChooseLauncher.launch(intent)
    }

    val imageChooseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val intent = it.data ?: return@registerForActivityResult
        profileImageURI = intent.data ?: return@registerForActivityResult
        Glide.with(this).load(profileImageURI).apply(RequestOptions().centerCrop())
            .into(binding.ivUserProfilePicture)
        updateUserInfo()
    }

    private fun updateUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            if (profileImageURI == null) return@launch
            viewModel.updateUserProfileImage(profileImageURI!!)
            mainViewModel.getUserFromDatabase()
        }
    }

    private fun btnSignOutAction() {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                removeFCMToken()
            }
            .setNegativeButton(
                "Cancel"
            ) { p0, _ -> p0?.cancel() }

        alertDialog.create()
        alertDialog.show()
    }

    private fun removeFCMToken() {
        FirebaseInstance.firebaseCloudMessage.deleteToken().addOnCompleteListener {
            signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }
}