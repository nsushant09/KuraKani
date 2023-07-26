package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.chatmessageadapter

import android.view.View
import android.widget.ImageView
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

    private var profileImage: ImageView
    private var messageBody: ImageView

    init {
        profileImage = binding.ivProfileImage
        messageBody = binding.ivChatImage
    }

    override fun bind(position: Int) {

        if (position == 0 || chatMessageAdapter.list[position - 1].fromUid == chatMessageAdapter.list[position].fromUid) {
            Glide.with(chatMessageAdapter.context).load(chatMessageAdapter.friendUser.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
        } else {
            profileImage.visibility = View.INVISIBLE
        }

        Glide.with(chatMessageAdapter.context)
            .asBitmap()
            .load(chatMessageAdapter.list.get(position).messageBody)
            .apply(RequestOptions().override(480))
            .apply(RequestOptions().transform(RoundedCorners(32)))
            .into(messageBody)
        itemView.setOnLongClickListener {
            chatMessageAdapter.onLongClickAction(chatMessageAdapter.list[position])
            true
        }
    }
}