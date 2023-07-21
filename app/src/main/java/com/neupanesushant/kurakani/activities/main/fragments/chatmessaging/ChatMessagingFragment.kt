package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.audiorecorder.AndroidAudioRecorder
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter
import com.neupanesushant.kurakani.databinding.FragmentChatMessagingBinding
import com.neupanesushant.kurakani.model.Message
import com.neupanesushant.kurakani.model.MessageType
import com.neupanesushant.kurakani.model.User
import com.neupanesushant.kurakani.services.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.*


class ChatMessagingFragment(private val user: User, private val friendUID: String) : Fragment() {

    private lateinit var _binding: FragmentChatMessagingBinding
    private val binding get() = _binding

    private val downloadService: DownloadService by inject()
    private val shareService: ShareService by inject()
    private val cameraService: CameraService by inject()
    private val viewModel: ChatMessagingViewModel by inject { parametersOf(friendUID) }

    private lateinit var audioRecorder: AndroidAudioRecorder
    private var file: File? = null

    private val performDelete: (Message) -> Unit = { message ->
        binding.btnSave.isVisible = message.messageType == MessageType.IMAGE
        binding.btnShare.isVisible = message.messageType == MessageType.IMAGE

        makeLongActionContainerVisible()
        binding.btnDelete.setOnClickListener {
            makeTextContainerVisible()
            viewModel.deleteMessage(message.timeStamp!!.toString())
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
        viewModel.getFriendUserDetails(friendUID)
        viewModel.setUser(user)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioRecorder = AndroidAudioRecorder(requireContext())

        setupView()
        setupEventListener()
        setupObserver()
    }

    private fun setupView() {
        makeTextContainerVisible()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupEventListener() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivSelectImage.setOnClickListener {
            chooseImage()
        }

        binding.etWriteMessage.addTextChangedListener {
            if (it == null || it.isEmpty()) {
                binding.btnSend.visibility = View.GONE
                binding.ivSelectImage.visibility = View.VISIBLE
                binding.ivRecordAudioMessage.visibility = View.VISIBLE
            } else {
                binding.btnSend.visibility = View.VISIBLE
                binding.ivSelectImage.visibility = View.GONE
                binding.ivRecordAudioMessage.visibility = View.GONE
            }
        }

        binding.btnSend.setOnClickListener {
            if (binding.etWriteMessage.text.isNotEmpty()) {
                viewModel.sendTextMessage(binding.etWriteMessage.text.toString())
                binding.etWriteMessage.text.clear()
            }
        }

        binding.cardViewAddImageIcon.setOnClickListener {
            if (PermissionManager.hasCameraPermission(requireContext())) {
                startActivityForResult(
                    cameraService.getCaptureImageIntent(),
                    CAMERA_IMAGE_CAPTURE_CODE
                )
            } else {
                PermissionManager.requestCameraPermission(requireActivity())
            }
        }


        binding.ivRecordAudioMessage.setOnTouchListener { view, event ->

            if (PermissionManager.hasRecordAudioPermission(requireContext())) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        file = null
                        File(
                            requireContext().cacheDir,
                            System.currentTimeMillis().toString() + ".mp3"
                        ).also {
                            audioRecorder.start(it)
                            file = it
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        audioRecorder.stop()
                        file?.let {
                            viewModel.sendAudioMessage(it.toUri())
                        }
                    }
                }
            } else {
                PermissionManager.requestRecordAudioPermission(requireActivity())
            }
            true
        }

    }

    private fun setupObserver() {
        viewModel.friendUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Glide.with(requireContext()).load(user.profileImage).centerCrop()
                    .error(R.drawable.ic_user).into(binding.ivFriendProfileImage)
                binding.tvFriendFirstName.text = user.firstName
                setChatLogObserver()
            }
        }
    }

    private fun setChatLogObserver() {
        viewModel.chatLog.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setChatData(it as ArrayList<Message>)
            }
        }
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
            ChatMessageAdapter(
                requireContext(),
                viewModel.user.value!!,
                viewModel.friendUser.value!!,
                messageList,
                performDelete
            )
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

            val tempImages: ArrayList<Uri> = arrayListOf()
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    tempImages.add(data.clipData!!.getItemAt(i).uri)
                }
                viewModel.sendImagesMessage(tempImages)
            }
        }

        if (requestCode == CAMERA_IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            val file = File(requireContext().cacheDir, cameraService.getLastCapturedFileName())
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file
            )
            val tempImages: ArrayList<Uri> = arrayListOf()
            tempImages.add(uri)
            viewModel.sendImagesMessage(tempImages)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(cameraService.getCaptureImageIntent(), CAMERA_IMAGE_CAPTURE_CODE)
        }
    }

}

