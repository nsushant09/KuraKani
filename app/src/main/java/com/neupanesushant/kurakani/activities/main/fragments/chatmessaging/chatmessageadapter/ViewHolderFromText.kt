package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ChatMessageFromLayoutBinding

class ViewHolderFromText(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatMessageFromLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var profileImage: ImageView
    private var messageBody: TextView

    init {
        profileImage = binding.ivProfileImage
        messageBody = binding.tvMessageBody
    }

    override fun bind(position: Int) {
        Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.user.profileImage)
            .apply(RequestOptions().circleCrop())
            .error(R.drawable.ic_user).into(profileImage)
        messageBody.text = chatMessageAdapter.list.get(position).messageBody

        itemView.setOnLongClickListener {
            chatMessageAdapter.performDelete(chatMessageAdapter.list[position])
            true
        }
    }

}