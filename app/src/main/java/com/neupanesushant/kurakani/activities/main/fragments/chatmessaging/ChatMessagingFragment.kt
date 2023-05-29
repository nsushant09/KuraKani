package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.services.DownloadService
import com.neupanesushant.kurakani.activities.services.NotificationService
import com.neupanesushant.kurakani.activities.services.ShareService
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.databinding.FragmentChatMessagingBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.ArithmeticException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.*


class ChatMessagingFragment : Fragment() {

    private lateinit var _binding: FragmentChatMessagingBinding
    private val binding get() = _binding
    private lateinit var viewModel: ChatMessagingViewModel
    private val mainViewModel: MainViewModel by activityViewModels()

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val IMAGE_SELECTOR_REQUEST_CODE = 981234

    private lateinit var downloadService: DownloadService
    private lateinit var shareService: ShareService

    private val performDelete: (Message) -> Unit = { message ->
        binding.btnSave.isVisible = message.messageType == MessageType.IMAGE
        binding.btnShare.isVisible = message.messageType == MessageType.IMAGE

        makeLongActionContainerVisible()
        binding.btnDelete.setOnClickListener {
            makeTextContainerVisible()
            viewModel.deleteChatFromDatabase(message.timeStamp!!.toString())
        }
        binding.btnCancel.setOnClickListener {
            makeTextContainerVisible()
        }
        binding.btnSave.setOnClickListener {
            makeTextContainerVisible()
            downloadService.downloadImage(message.messageBody!!)
        }
        binding.btnShare.setOnClickListener {
            makeTextContainerVisible()
            shareService.shareImage(message.messageBody!!)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMessagingBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[ChatMessagingViewModel::class.java]
        downloadService = DownloadService(requireContext());
        shareService = ShareService(requireContext());
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        //set friend name and image
        mainViewModel.isFriendValueLoaded.observe(viewLifecycleOwner) {
            if (it) {
                mainViewModel.friendUser.observe(viewLifecycleOwner) { user ->
                    viewModel.setToID(user?.uid!!)
                    viewModel.getAllChatFromDatabase()
                    Glide.with(requireContext()).load(user.profileImage).centerCrop()
                        .error(R.drawable.ic_user).into(binding.ivFriendProfileImage)
                    binding.tvFriendFirstName.text = user.firstName
                }
            }
        }

        binding.ivSelectImage.setOnClickListener {
            chooseImage()
        }

        binding.etWriteMessage.addTextChangedListener {
            if (it == null || it.isEmpty()) {
                binding.btnSend.visibility = View.GONE
                binding.ivSelectImage.visibility = View.VISIBLE
            } else {
                binding.btnSend.visibility = View.VISIBLE
                binding.ivSelectImage.visibility = View.GONE
            }
        }

        setupEtMessageAction()

        viewModel.chatLog.observe(viewLifecycleOwner) {
            setChatData(it)
        }

        setupView()
    }

    private fun setupView() {
        makeTextContainerVisible()
    }


    private fun makeTextContainerVisible() {
        binding.apply {
            rlTextContainer.visibility = View.VISIBLE
            rlLongActionsContainer.visibility = View.INVISIBLE
        }
        binding.rlTextContainer.animation = AnimationUtils.loadAnimation(
            requireContext(),
            androidx.appcompat.R.anim.abc_slide_in_bottom
        )
    }

    private fun makeLongActionContainerVisible() {
        binding.apply {
            rlTextContainer.visibility = View.INVISIBLE
            rlLongActionsContainer.visibility = View.VISIBLE
        }
        binding.rlLongActionsContainer.animation = AnimationUtils.loadAnimation(
            requireContext(),
            androidx.appcompat.R.anim.abc_slide_in_bottom
        )
    }

    private fun setChatData(messageList: ArrayList<Message>) {
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

            val tempImageMap: LinkedHashMap<String, Uri?> = LinkedHashMap()
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    tempImageMap[UUID.randomUUID().toString()] = data.clipData!!.getItemAt(i).uri
                }
                viewModel.addImagesToDatabase(tempImageMap)
            }
        }
    }

    private fun setupEtMessageAction() {
        binding.btnSend.setOnClickListener {
            if (binding.etWriteMessage.text.isNotEmpty()) {
                viewModel.addChatToDatabase(
                    binding.etWriteMessage.text.toString(),
                    MessageType.TEXT
                )
                binding.etWriteMessage.text.clear()
            }
        }
    }

}

