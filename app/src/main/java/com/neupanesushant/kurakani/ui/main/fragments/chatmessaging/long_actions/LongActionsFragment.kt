package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.long_actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.FragmentLongActionsBinding
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.usecase.DownloadFileUseCase
import com.neupanesushant.kurakani.domain.usecase.ShareUseCase
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.ChatMessagingViewModel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class LongActionsFragment() : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentLongActionsBinding
    private val binding get() = _binding

    private lateinit var message: Message
    private lateinit var friendUID: String
    private val downloadFileUseCase: DownloadFileUseCase by inject()
    private val shareUseCase: ShareUseCase by inject()
    private val viewModel: ChatMessagingViewModel by inject { parametersOf(friendUID) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogThemeNoFloating)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLongActionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupEventListener()
        setupObserver()
    }

    private fun setupView() {
        if (message.messageType != MessageType.IMAGE) {
            binding.btnSave.isVisible = false
            binding.btnShare.isVisible = false
        }
    }

    private fun setupObserver() {
    }

    private fun setupEventListener() {
        binding.btnCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding.btnSave.setOnClickListener {
            downloadFileUseCase.download(message)
            dismissAllowingStateLoss()
        }

        binding.btnShare.setOnClickListener {
            shareUseCase.share(message)
            dismissAllowingStateLoss()
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteMessage(message.timeStamp.toString())
            dismissAllowingStateLoss()
        }
    }

    companion object {
        private var instance: LongActionsFragment? = null
        fun getInstance(message: Message, friendUID: String): LongActionsFragment {
            if (instance == null) {
                instance = LongActionsFragment()
            }
            instance!!.message = message
            instance!!.friendUID = friendUID
            return instance!!
        }
    }
}