package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
//import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.fragments.me.MeFragment
import com.neupanesushant.kurakani.activities.main.fragments.search.SearchFragment
import com.neupanesushant.kurakani.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private val TAG = "ChatFragment"
    private lateinit var _binding : FragmentChatBinding
    private val binding get() = _binding
    lateinit var chatViewModel : ChatViewModel
    private val searchFragment = SearchFragment()
    private val meFragment  = MeFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewSearchIcon.setOnClickListener{
            replaceFragment(searchFragment)
        }

        chatViewModel.isAllUILoaded.observe(viewLifecycleOwner, Observer{
            if(it){
                binding.progressBar.visibility = View.GONE
                binding.layoutChatFragment.visibility = View.VISIBLE
            }
        })
        chatViewModel.user.observe(viewLifecycleOwner, Observer{
            binding.tvUserName.text = it?.fullName
//            Picasso.get().load(it?.profileImage).into(binding.ivUserProfilePicture)
            Glide.with(requireContext()).load(it?.profileImage).centerCrop().error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
            chatViewModel.setIsUILoaded(true)
        })

        binding.rvStorySizedUser.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chatViewModel.allUsers.observe(viewLifecycleOwner, Observer{
            binding.rvStorySizedUser.adapter = StorySizedUserAdapter(requireContext(),chatViewModel, it)
        })

        binding.cardViewUserIcon.setOnClickListener{
            replaceFragment(meFragment)
        }

        chatViewModel.isNewMessageUIClicked.observe(viewLifecycleOwner, Observer{
            if(it){
                replaceFragment(searchFragment)
                chatViewModel.setNewMessageUIClicked(false)
            }
        })
    }

    fun replaceFragment(fragment : Fragment){
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.fragment_container_view_tag, fragment)
            isAddToBackStackAllowed
            addToBackStack(null)
            commit()
        }
    }


}