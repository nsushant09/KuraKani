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

    private val CHANNEL_ID = "channelID"
    private lateinit var builder: NotificationCompat.Builder
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val IMAGE_SELECTOR_REQUEST_CODE = 981234

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
            downloadImage(message.messageBody!!, false)
        }
        binding.btnShare.setOnClickListener {
            makeTextContainerVisible()
            downloadImage(message.messageBody!!, true)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMessagingBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[ChatMessagingViewModel::class.java]
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

        createNotificationChannel()
        setupNotification()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channelName"
            val description = "channelDescription"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            // register the channel with the system
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupNotification() {
        builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_kurakani_logo)
            .setContentTitle("Download")
            .setContentText("Image saved")
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    private fun downloadImage(imageUrl: String, isSharing: Boolean) {

        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection

        uiScope.launch {
            if (isSharing)
                shareImage(connection)
            else
                downloadImageSuspened(connection)
        }

    }

    private suspend fun downloadImageSuspened(connection: HttpURLConnection) {
        withContext(Dispatchers.IO) {

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            kotlin.runCatching {
                inputStream.close()
            }
            MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "Image",
                "Image downloaded from the internet"
            )

            with(NotificationManagerCompat.from(requireContext())) {
                notify(1, builder.build())
            }
        }
    }

    private suspend fun shareImage(connection: HttpURLConnection) {

        withContext(Dispatchers.IO) {
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val tempFile = File(context!!.cacheDir, LocalDateTime.now().toString() + ".jpeg")
            try {
                kotlin.runCatching {
                    inputStream.close()
                    val fos = FileOutputStream(tempFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.setDataAndType(
                    FileProvider.getUriForFile(context!!, context!!.applicationContext.packageName + ".provider", tempFile),
                    "image/jpeg"
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val chooser = Intent.createChooser(intent, "Send Image Via...")
                startActivity(chooser)
            } catch (e: Exception) {
                Log.i("TAG", e.printStackTrace().toString())
            }
        }
    }

}

