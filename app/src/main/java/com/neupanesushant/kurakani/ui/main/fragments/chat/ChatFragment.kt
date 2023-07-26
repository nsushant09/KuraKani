package com.neupanesushant.kurakani.ui.main.fragments.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.ui.main.MainViewModel
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.ChatMessagingFragment
import com.neupanesushant.kurakani.ui.main.fragments.me.MeFragment
import com.neupanesushant.kurakani.ui.main.fragments.search.SearchFragment
import com.neupanesushant.kurakani.databinding.FragmentChatBinding
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import org.koin.android.ext.android.inject

class ChatFragment : Fragment() {

    private lateinit var _binding: FragmentChatBinding
    private val binding get() = _binding
    private val searchFragment = SearchFragment()
    private val meFragment = MeFragment()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by inject()

    private var usersLoaded = false;
    private var messagesLoaded = false;

    private val onClickOpenChatMessaging: (uid: String) -> Unit = { uid ->
        val chatMessagingFragment =
            ChatMessagingFragment(AuthenticatedUser.getInstance().getUser()!!, uid)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatMessagingFragment)
            isAddToBackStackAllowed
            addToBackStack(null)
            commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObserver()
        setupEventListener()
    }

    private fun setupView() {

        binding.rvLatestMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.rvStorySizedUser.animation =
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        binding.rvStorySizedUser.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupEventListener() {

        binding.cardViewSearchIcon.setOnClickListener {
            replaceFragment(searchFragment)
        }
        binding.cardViewUserIcon.setOnClickListener {
            replaceFragment(meFragment)
        }
        binding.llAddNewTextInfo.setOnClickListener {
            replaceFragment(searchFragment)
        }
    }

    private fun setupObserver() {

        mainViewModel.user.observe(viewLifecycleOwner) {
            binding.tvUserName.text = it?.fullName
            Glide.with(requireContext()).load(it?.profileImage).centerCrop()
                .error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
        }

        chatViewModel.usersOfLatestMessages.observe(viewLifecycleOwner) {
            if (it.size == 0) {
                binding.rvLatestMessages.visibility = View.GONE
                binding.llAddNewTextInfo.visibility = View.VISIBLE
            } else {
                val adapter =
                    LatestMessagesAdapter(requireContext(), chatViewModel, onClickOpenChatMessaging)
                binding.rvLatestMessages.adapter = adapter
                binding.llAddNewTextInfo.visibility = View.GONE
                binding.rvLatestMessages.visibility = View.VISIBLE
            }

            if (it != null) {
                messagesLoaded = true
                displayUI()
            }
        }

        chatViewModel.allUsers.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                binding.rvStorySizedUser.adapter =
                    StorySizedUserAdapter(requireContext(), it, onClickOpenChatMessaging)
                usersLoaded = true
                displayUI()
            }
        }
    }

    private fun displayUI() {
        if (usersLoaded && messagesLoaded) {
            binding.progressBar.visibility = View.GONE
            binding.layoutChatFragment.visibility = View.VISIBLE
            binding.llAddNewTextInfo.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.VISIBLE
            binding.layoutChatFragment.visibility = View.GONE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, fragment)
            isAddToBackStackAllowed
            addToBackStack(null)
            commit()
        }
    }


}