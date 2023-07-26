package com.neupanesushant.kurakani.ui.main.fragments.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.LatestChatContentLayoutBinding
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User

class LatestMessagesAdapter(
    val context: Context,
    viewModel: ChatViewModel,
    val onClickOpenChatMessaging: (String) -> Unit
) : RecyclerView.Adapter<LatestMessagesAdapter.ViewHolder>() {

    private var messagesList: List<Message>? = viewModel.latestMessages.value
    private val usersList: List<User>? = viewModel.usersOfLatestMessages.value

    inner class ViewHolder(binding: LatestChatContentLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val profileImage = binding.ivProfilePicture
        val userName = binding.tvUserName
        val latestMessage = binding.tvLatestMessageBody
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LatestChatContentLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageObject = messagesList?.get(position)
        val userObject = usersList?.get(position)

        holder.itemView.animation =
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        holder.itemView.setOnClickListener {
            onClickOpenChatMessaging(userObject?.uid.toString())
        }
        Glide.with(context).load(userObject?.profileImage).apply(RequestOptions().circleCrop())
            .error(R.drawable.ic_user).into(holder.profileImage)
        holder.userName.text = userObject?.fullName

        if (messageObject?.toUid == userObject?.uid) {
            if (messageObject?.messageType == MessageType.TEXT) {
                holder.latestMessage.text = messageObject.messageBody
            }
            if (messageObject?.messageType == MessageType.IMAGE) {
                holder.latestMessage.text = "You sent a image"
            }
            if (messageObject?.messageType == MessageType.AUDIO) {
                holder.latestMessage.text = "You sent a voice message"
            }
        } else {
            if (messageObject?.messageType == MessageType.TEXT) {
                holder.latestMessage.text = messageObject.messageBody
            }
            if (messageObject?.messageType == MessageType.IMAGE) {
                holder.latestMessage.text = userObject?.firstName + " sent a image"
            }
            if (messageObject?.messageType == MessageType.AUDIO) {
                holder.latestMessage.text = userObject?.firstName + " sent a voice message"
            }
        }

    }

    override fun getItemCount(): Int {
        return if (usersList == null || usersList.isEmpty()) {
            0
        } else {
            usersList.size
        }
    }


}