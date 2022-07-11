package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
//import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingFragment
import com.neupanesushant.kurakani.activities.main.fragments.me.MeFragment
import com.neupanesushant.kurakani.activities.main.fragments.search.SearchFragment
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private val TAG = "ChatFragment"
    private lateinit var _binding : FragmentChatBinding
    private val binding get() = _binding
    lateinit var chatViewModel : ChatViewModel
    private val searchFragment = SearchFragment()
    private val meFragment  = MeFragment()
    private val chatMessagingFragment = ChatMessagingFragment()
    private val mainViewModel : MainViewModel by activityViewModels()

    val onClickOpenChatMessaging : (uid : String) -> Unit = {
        mainViewModel.getFriendUserFromDatabase(it)
//        if(it != chatViewModel.user.value?.uid){
//            if(!chatViewModel.isAlreadyFriend(it)){
//                val user = chatViewModel.user.value
////                val tempFriend = MutableList<Friend?>( if(chatViewModel.user.value?.friends?.size == null) 1 else chatViewModel.user.value!!.friends!!.size + 1, init = {i:Int -> null})
////                tempFriend.add(Friend(it,null))
//                mainViewModel.updateUser(
//                    User(
//                       user?.uid,
//                        user?.firstName,
//                        user?.lastName,
//                        user?.fullName,
//                        user?.profileImage,
//                    )
//                )
//            }
//            Toast.makeText(context, "Updated friend", Toast.LENGTH_SHORT).show()
//        }
        parentFragmentManager.beginTransaction().apply{
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
            Glide.with(requireContext()).load(it?.profileImage).centerCrop().error(R.drawable.ic_user).into(binding.ivUserProfilePicture)
            chatViewModel.setIsUILoaded(true)
        })

        binding.rvStorySizedUser.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chatViewModel.allUsers.observe(viewLifecycleOwner, Observer{
            binding.rvStorySizedUser.adapter = StorySizedUserAdapter(requireContext(),chatViewModel, it, onClickOpenChatMessaging)
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

        binding.rvLatestMessages.layoutManager = LinearLayoutManager(requireContext())
        chatViewModel.latestMessages.observe(viewLifecycleOwner, Observer{
            Log.i(TAG, "the size of the list is : " + it.size)
            val adapter = LatestMessagesAdapter(requireContext(), chatViewModel, it, onClickOpenChatMessaging)
            binding.rvLatestMessages.adapter = adapter
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

    override fun onResume() {
        super.onResume()
    }

}