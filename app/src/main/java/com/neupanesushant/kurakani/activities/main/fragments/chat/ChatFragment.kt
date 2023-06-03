package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingFragment
import com.neupanesushant.kurakani.activities.main.fragments.me.MeFragment
import com.neupanesushant.kurakani.activities.main.fragments.search.SearchFragment
import com.neupanesushant.kurakani.databinding.FragmentChatBinding
import org.koin.android.ext.android.inject

class ChatFragment : Fragment() {

    private lateinit var _binding: FragmentChatBinding
    private val binding get() = _binding
    private val searchFragment = SearchFragment()
    private val meFragment = MeFragment()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val chatViewModel : ChatViewModel by inject()

    private val onClickOpenChatMessaging: (uid: String) -> Unit = { uid ->
        val chatMessagingFragment = ChatMessagingFragment.getInstance(mainViewModel.user.value!!, uid)
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
    ): View? {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //open search fragment
        binding.cardViewSearchIcon.setOnClickListener {
            replaceFragment(searchFragment)
        }
        //open me fragment
        binding.cardViewUserIcon.setOnClickListener {
            replaceFragment(meFragment)
        }
        //new message fragment
        binding.llAddNewTextInfo.setOnClickListener {
            replaceFragment(searchFragment)
        }

        //show ui
        chatViewModel.isAllUILoaded.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.progressBar.visibility = View.GONE
                binding.layoutChatFragment.visibility = View.VISIBLE
                binding.llAddNewTextInfo.visibility = View.GONE
            }
        })

        // set profile image and username
        mainViewModel.user.observe(viewLifecycleOwner, Observer {
            binding.tvUserName.text = it?.fullName
            Glide.with(requireContext()).load(it?.profileImage).centerCrop()
                .error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
        })

        //set items in storysized recycler view
        binding.rvStorySizedUser.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvStorySizedUser.animation =
            AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        chatViewModel.allUsers.observe(viewLifecycleOwner, Observer {
            binding.rvStorySizedUser.adapter =
                StorySizedUserAdapter(requireContext(), chatViewModel, it, onClickOpenChatMessaging)
        })

        //open search to write new messages
        chatViewModel.isNewMessageUIClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                replaceFragment(searchFragment)
                chatViewModel.setNewMessageUIClicked(false)
            }
        })


        //set items in latest messages
        binding.rvLatestMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStorySizedUser.animation =
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)

        chatViewModel.usersOfLatestMessages.observe(viewLifecycleOwner, Observer {
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


        })

    }


    fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, fragment)
            isAddToBackStackAllowed
            addToBackStack(null)
            commit()
        }
    }


}