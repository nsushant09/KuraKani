package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.ChatImageToLayoutBinding

class ViewHolderToImage(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatImageToLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    var profileImage: ImageView
    var messageBody: ImageView

    init {
        profileImage = binding.ivProfileImage
        messageBody = binding.ivChatImage
    }

    override fun bind(position: Int) {
        Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.friendUser.profileImage)
            .apply(RequestOptions().circleCrop())
            .error(R.drawable.ic_user).into(profileImage)

        Glide.with(chatMessageAdapter.context)
            .asBitmap()
            .load(chatMessageAdapter.list.get(position).messageBody)
            .apply(RequestOptions().override(480))
            .apply(RequestOptions().transform(RoundedCorners(32)))
            .into(messageBody)
        itemView.setOnLongClickListener {
            chatMessageAdapter.performDelete(chatMessageAdapter.list[position])
            true
        }
    }
}