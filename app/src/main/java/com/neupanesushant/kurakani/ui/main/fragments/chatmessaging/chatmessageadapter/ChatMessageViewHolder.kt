package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.chatmessageadapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ChatMessageViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(position: Int)
}