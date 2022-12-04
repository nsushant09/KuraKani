package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.databinding.FragmentChatMessagingBinding
import java.util.*


class ChatMessagingFragment : Fragment() {

    private val TAG = "ChatMessagingFragment"
    private lateinit var _binding: FragmentChatMessagingBinding
    private val binding get() = _binding
    private lateinit var viewModel: ChatMessagingViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    private val IMAGE_SELECTOR_REQUEST_CODE = 981234
    private var latestChatImageURI: Uri? = null

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

        //set friend name and image
        mainViewModel.isFriendValueLoaded.observe(viewLifecycleOwner, Observer {
            if (it) {
                mainViewModel.friendUser.observe(viewLifecycleOwner, Observer {
                    viewModel.setToID(it?.uid!!)
                    viewModel.getAllChatFromDatabase()
                    Glide.with(requireContext()).load(it?.profileImage).centerCrop()
                        .error(R.drawable.ic_user).into(binding.ivFriendProfileImage)
                    binding.tvFriendFirstName.text = it?.firstName
                })
            }
        })

        binding.ivSelectImage.setOnClickListener {
            chooseImage()
        }

        binding.etWriteMessage.addTextChangedListener {
            if (it == null || it.length == 0) {
                binding.btnSend.visibility = View.GONE
                binding.ivSelectImage.visibility = View.VISIBLE
            } else {
                binding.btnSend.visibility = View.VISIBLE
                binding.ivSelectImage.visibility = View.GONE
            }
        }

        setupEtMessageAction()

        viewModel.chatLog.observe(viewLifecycleOwner, Observer{
            setChatData(it)
        })

    }


    private fun setChatData(messageList : ArrayList<Message>){
        if(messageList.size == 0){
            viewModel.getAllChatFromDatabase()
        }
        binding.rvChatContent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding.rvChatContent.adapter = ChatMessageAdapter(requireContext(), mainViewModel, messageList)
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
            latestChatImageURI = data.data!!
            val bitmap =
                MediaStore.Images.Media.getBitmap(activity?.contentResolver, latestChatImageURI)
            addLatestChatImage()
        }
    }

    fun addLatestChatImage(){
        if (latestChatImageURI == null) return
        val fileName = UUID.randomUUID().toString()
        viewModel.addImageToDatabase(fileName, latestChatImageURI)
    }

    fun setupEtMessageAction() {
        binding.btnSend.setOnClickListener {
            if (binding.etWriteMessage != null && binding.etWriteMessage.text.length != 0) {
                viewModel.addChatToDatabase(binding.etWriteMessage.text.toString(), MessageType.TEXT)
                viewModel.getChatUpdateFromDatabase()
                binding.etWriteMessage.text.clear()
            }
        }
    }


}