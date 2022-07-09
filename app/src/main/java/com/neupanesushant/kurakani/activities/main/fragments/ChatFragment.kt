package com.neupanesushant.kurakani.activities.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.FragmentChatBinding
import com.squareup.picasso.Picasso

class ChatFragment : Fragment() {

    private lateinit var _binding : FragmentChatBinding
    private val binding get() = _binding
    private lateinit var viewModel : ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner, Observer{
            binding.tvUserName.text = it?.fullName
            Picasso.get().load(it?.profileImage).centerCrop().fit().error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
        })
    }
}