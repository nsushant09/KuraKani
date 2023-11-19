package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.FragmentChatMessagingBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import com.neupanesushant.kurakani.domain.usecase.CameraUseCase
import com.neupanesushant.kurakani.domain.usecase.audiorecorder.AndroidAudioRecorder
import com.neupanesushant.kurakani.domain.usecase.audiorecorder.AutoRunningTimer
import com.neupanesushant.kurakani.domain.usecase.permission.PermissionManager
import com.neupanesushant.kurakani.services.*
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.long_actions.LongActionsFragment
import com.neupanesushant.kurakani.ui.main.rtc.RTCActivity
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.*


class ChatMessagingFragment(private val friendObj: User) : Fragment() {

    private lateinit var user: User
    private lateinit var _binding: FragmentChatMessagingBinding
    private val binding get() = _binding

    private val cameraUseCase: CameraUseCase by inject()
    private val viewModel: ChatMessagingViewModel by inject { parametersOf(friendObj) }

    private lateinit var audioRecorder: AndroidAudioRecorder
    private val autoRunningTimer = AutoRunningTimer()
    private var audioRecorderFile: File? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMessagingBinding.inflate(layoutInflater)

        if (AuthenticatedUser.getInstance().getUser() == null) {
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }

        user = AuthenticatedUser.getInstance().getUser()!!
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
        setFriendUserDetail()
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
            isMessageWritten(!it.isNullOrEmpty())
        }

        binding.btnSend.setOnClickListener {
            if (binding.etWriteMessage.text.isNotEmpty()) {
                viewModel.sendTextMessage(binding.etWriteMessage.text.toString())
                binding.etWriteMessage.text.clear()
            }
        }

        binding.cardViewAddImageIcon.setOnClickListener {
            openCamera()
        }

        binding.ivVideo.setOnClickListener {
            onVideoCall()
        }
        binding.ivCall.setOnClickListener {
            onVoiceCall()
        }


        binding.ivRecordAudioMessage.setOnTouchListener { _, event ->
            recordAudioMessageEventHandler(event)
        }
    }


    private fun setupObserver() {
    }

    private fun recordAudioMessageEventHandler(event: MotionEvent): Boolean {
        if (!PermissionManager.hasRecordAudioPermission(requireContext())) {
            PermissionManager.requestRecordAudioPermission(requireActivity())
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                audioRecorderFile = null
                audioRecorderFile = audioRecorder.start()
                displayAudioRecording(true)
            }

            MotionEvent.ACTION_UP -> {
                audioRecorder.stop()
                displayAudioRecording(false)
                audioRecorderFile?.let {
                    //TODO : Ask user if they are sure about sending the message
                    viewModel.sendAudioMessage(it.toUri())
                }
            }

            else -> {}
        }
        return true
    }

    private fun setFriendUserDetail() {
        Glide.with(requireContext()).load(friendObj.profileImage).centerCrop()
            .error(R.drawable.ic_user).into(binding.ivFriendProfileImage)
        binding.tvFriendFirstName.text = friendObj.firstName
        setChatLogObserver()
    }

    private fun displayAudioRecording(isRecording: Boolean) {

        binding.etWriteMessage.isCursorVisible = !isRecording
        val color = if (isRecording) R.color.neon_orange else R.color.grey

        if (isRecording) {
            autoRunningTimer.getPrettyTime { time ->
                binding.etWriteMessage.hint = time
            }
        } else {
            binding.etWriteMessage.hint = getString(R.string.message)
            autoRunningTimer.resetTime()
        }

        binding.etWriteMessage.setHintTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun setChatLogObserver() {
        viewModel.chatLog.observe(viewLifecycleOwner) {
            if (it.isEmpty())
                return@observe

            setChatData(it)
        }
    }

    private fun openCamera() {
        if (PermissionManager.hasCameraPermission(requireContext())) {
            cameraActivityLauncher.launch(cameraUseCase.getCaptureImageIntent())
        } else {
            PermissionManager.requestCameraPermission(requireActivity())
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun isMessageWritten(boolean: Boolean) {
        binding.apply {
            btnSend.isVisible = boolean
            ivSelectImage.isVisible = !boolean
            ivRecordAudioMessage.isVisible = !boolean
        }
    }

    private fun setChatData(messageList: ArrayList<Message>) {
        binding.rvChatContent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding.rvChatContent.adapter =
            ChatMessageAdapter(
                requireContext(),
                user,
                friendObj,
                messageList,
                onLongClickAction
            )
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imageSelectorRequestLauncher.launch(Intent.createChooser(intent, "Select images"))
    }

    private val imageSelectorRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        val tempImages: ArrayList<Uri> = arrayListOf()
        if (data.clipData != null) {
            for (i in 0 until data.clipData!!.itemCount) {
                tempImages.add(data.clipData!!.getItemAt(i).uri)
            }
            viewModel.sendImagesMessage(tempImages)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) return@registerForActivityResult
        cameraActivityLauncher.launch(cameraUseCase.getCaptureImageIntent())
    }

    private val cameraActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val file = File(requireContext().cacheDir, cameraUseCase.getLastCapturedFileName())
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".provider",
            file
        )
        val tempImages: ArrayList<Uri> = arrayListOf()
        tempImages.add(uri)
        viewModel.sendImagesMessage(tempImages)
    }

    private val onLongClickAction: (Message) -> Unit = { message ->
        LongActionsFragment.getInstance(message, friendObj)
            .show(parentFragmentManager, LongActionsFragment::class.java.name)
    }

    private fun onVoiceCall() {
        Intent(activity, RTCActivity::class.java).apply {
            putExtra("user", user)
            putExtra("friend", friendObj)
            putExtra("communicationType", RTCActivity.CommunicationType.AUDIO)
            startActivity(this)
        }
    }

    private fun onVideoCall() {
        Intent(activity, RTCActivity::class.java).apply {
            putExtra("user", user)
            putExtra("friend", friendObj)
            putExtra("communicationType", RTCActivity.CommunicationType.VIDEO)
            startActivity(this)
        }
    }
}

