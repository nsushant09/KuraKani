package com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.chatmessageadapter

import android.content.Context
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.neupanesushant.kurakani.domain.model.Message
import com.neupanesushant.kurakani.domain.model.MessageType
import com.neupanesushant.kurakani.domain.model.User

class ChatMessageAdapter(
    val context: Context,
    val user: User,
    val friendUser: User,
    val list: List<Message>,
    val onLongClickAction: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val FROM_TEXT = 10
        const val FROM_IMAGE = 11
        const val FROM_AUDIO = 12
        const val TO_TEXT = 20
        const val TO_IMAGE = 21
        const val TO_AUDIO = 22
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatMessageViewHolderFactory().getViewHolder(this, parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (position == 0) {
            holder.itemView.animation =
                AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        }

        (holder as ChatMessageViewHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val fromValues = hashMapOf(
            Pair(MessageType.TEXT, FROM_TEXT),
            Pair(MessageType.IMAGE, FROM_IMAGE),
            Pair(MessageType.AUDIO, FROM_AUDIO)
        )
        val toValues = hashMapOf(
            Pair(MessageType.TEXT, TO_TEXT),
            Pair(MessageType.IMAGE, TO_IMAGE),
            Pair(MessageType.AUDIO, TO_AUDIO)
        )

        return if (list[position].fromUid == user.uid)
            fromValues[list[position].messageType] ?: -1
        else
            toValues[list[position].messageType] ?: -1

    }


}
