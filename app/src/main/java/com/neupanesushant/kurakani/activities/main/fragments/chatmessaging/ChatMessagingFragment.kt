package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.databinding.FragmentChatMessagingBinding


class ChatMessagingFragment : Fragment() {

    private val TAG = "ChatMessagingFragment"
    private lateinit var _binding : FragmentChatMessagingBinding
    private val binding get() = _binding
    private lateinit var viewModel : ChatMessagingViewModel
    private val mainViewModel : MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatMessagingBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(ChatMessagingViewModel::class.java)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        mainViewModel.friendUser.observe(viewLifecycleOwner, Observer{
            viewModel.setToID(it?.uid!!)
            viewModel.getAllChatFromDatabase()
            Glide.with(requireContext()).load(it?.profileImage).centerCrop().error(R.drawable.ic_user).into(binding.ivFriendProfileImage)
            binding.tvFriendFirstName.text = it?.firstName
        })

        binding.ivSelectImage.setOnClickListener {
            Toast.makeText(context, "This feature will be added soon", Toast.LENGTH_SHORT).show()
        }

        binding.etWriteMessage.addTextChangedListener {
            if(it == null || it.length == 0){
                binding.btnSend.visibility = View.GONE
                binding.ivSelectImage.visibility = View.VISIBLE
            }else{
                binding.btnSend.visibility = View.VISIBLE
                binding.ivSelectImage.visibility = View.GONE
            }
        }

        setupEtMessageAction()

        viewModel.chatLog.observe(viewLifecycleOwner, Observer{
            binding.rvChatContent.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, true)
            binding.rvChatContent.adapter = ChatMessageAdapter(requireContext(), mainViewModel, it.toList())
        })
    }


    fun setupEtMessageAction(){
        binding.btnSend.setOnClickListener {
            if(binding.etWriteMessage != null && binding.etWriteMessage.text.length != 0){
                viewModel.addChatToDatabase(binding.etWriteMessage.text.toString())
                viewModel.getChatUpdateFromDatabase()
                binding.etWriteMessage.text.clear()
            }
        }
    }

}