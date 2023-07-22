package com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter

import android.widget.TextView
import com.neupanesushant.kurakani.databinding.ChatMessageFromLayoutBinding

class ViewHolderFromText(
    private val chatMessageAdapter: ChatMessageAdapter,
    binding: ChatMessageFromLayoutBinding
) :
    ChatMessageViewHolder(binding) {

    private var messageBody: TextView

    init {
        messageBody = binding.tvMessageBody
    }

    override fun bind(position: Int) {

        messageBody.text = chatMessageAdapter.list.get(position).messageBody

        itemView.setOnLongClickListener {
            chatMessageAdapter.onLongClickAction(chatMessageAdapter.list[position])
            true
        }
    }

}