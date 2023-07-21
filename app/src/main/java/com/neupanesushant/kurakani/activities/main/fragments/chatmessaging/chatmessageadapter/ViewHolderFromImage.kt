package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.databinding.ChatImageFromLayoutBinding

class ViewHolderFromImage(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatImageFromLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var messageBody: ImageView

    init {
        messageBody = binding.ivChatImage
    }

    override fun bind(position: Int) {

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