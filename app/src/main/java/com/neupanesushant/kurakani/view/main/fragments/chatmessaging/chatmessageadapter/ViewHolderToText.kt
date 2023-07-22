package com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ChatMessageToLayoutBinding

class ViewHolderToText(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatMessageToLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var profileImage: ImageView
    private var messageBody: TextView

    init {
        profileImage = binding.ivProfileImage
        messageBody = binding.tvMessageBody
    }

    override fun bind(position: Int) {

        if (position == 0 || chatMessageAdapter.list[position - 1].fromUid == chatMessageAdapter.list[position].fromUid) {
            Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.friendUser.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
        } else {
            profileImage.visibility = View.INVISIBLE
        }
        messageBody.text = chatMessageAdapter.list[position].messageBody

        itemView.setOnLongClickListener {
            chatMessageAdapter.onLongClickAction(chatMessageAdapter.list[position])
            true
        }
    }
}