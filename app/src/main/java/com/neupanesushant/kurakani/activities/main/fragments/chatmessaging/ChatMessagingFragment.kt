package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
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
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.collections.LinkedHashMap


class ChatMessagingFragment : Fragment() {

    private val TAG = "ChatMessagingFragment"
    private lateinit var _binding: FragmentChatMessagingBinding
    private val binding get() = _binding
    private lateinit var viewModel: ChatMessagingViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    private val IMAGE_SELECTOR_REQUEST_CODE = 981234

    private val performDelete : (Message) -> Unit = {message ->
        binding.tvSave.isVisible = message.messageType == MessageType.IMAGE
        makeLongActionContainerVisible()
        binding.tvDelete.setOnClickListener {
//            makeTextContainerVisible()
            viewModel.deleteChatFromDatabase(message.timeStamp!!.toString())
        }
        binding.tvCancle.setOnClickListener{
            makeTextContainerVisible()
        }
        binding.tvSave.setOnClickListener {
            downloadImage(message.messageBody!!)
        }

    }
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

        viewModel.chatLog.observe(viewLifecycleOwner, Observer {
            setChatData(it)
        })

        setupView()
    }

    private fun setupView(){
        makeTextContainerVisible()
    }


    private fun makeTextContainerVisible(){
        binding.apply{
            rlTextContainer.visibility = View.VISIBLE
            rlLongActionsContainer.visibility = View.INVISIBLE
        }
    }
    private fun makeLongActionContainerVisible(){
        binding.apply{
            rlTextContainer.visibility = View.INVISIBLE
            rlLongActionsContainer.visibility = View.VISIBLE
        }
    }
    private fun setChatData(messageList: ArrayList<Message>) {
        if (messageList.size == 0) {
            viewModel.getAllChatFromDatabase()
        }
        binding.rvChatContent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding.rvChatContent.adapter =
            ChatMessageAdapter(requireContext(), mainViewModel, messageList, performDelete)
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, "Select Images"),
            IMAGE_SELECTOR_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val tempImageMap : LinkedHashMap<String, Uri?> = LinkedHashMap()
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    tempImageMap[UUID.randomUUID().toString()] = data.clipData!!.getItemAt(i).uri
                }
                viewModel.addImagesToDatabase(tempImageMap)
            }
        }
    }

    fun setupEtMessageAction() {
        binding.btnSend.setOnClickListener {
            if (binding.etWriteMessage != null && binding.etWriteMessage.text.length != 0) {
                viewModel.addChatToDatabase(
                    binding.etWriteMessage.text.toString(),
                    MessageType.TEXT
                )
                viewModel.getChatUpdateFromDatabase()
                binding.etWriteMessage.text.clear()
            }
        }
    }

    private fun downloadImage(imageUrl : String){

        // First, get the URL of the image you want to download

// Create an HttpURLConnection to download the image
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection

// Download the image
        val runnable = Runnable{
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

// Close the input stream
            inputStream.close()

// Save the downloaded image to the device's gallery
            MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "Image",
                "Image downloaded from the internet"
            )

        }

        val thread = Thread(runnable)
        thread.start()
        var error = false

        thread.setUncaughtExceptionHandler{thread1 , throwable ->
            Toast.makeText(requireContext(), "Error while saving Image", Toast.LENGTH_SHORT).show()
            error = true
            thread1.stop()
        }

        while(true){
            if(!thread.isAlive && !error){
                Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_SHORT).show()
                break;
            }
        }
    }
}