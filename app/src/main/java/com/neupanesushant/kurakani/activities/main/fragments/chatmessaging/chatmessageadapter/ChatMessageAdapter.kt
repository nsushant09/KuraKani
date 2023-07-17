package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.chatmessageadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.ChatImageFromLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatImageToLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatMessageFromLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatMessageToLayoutBinding

class ChatMessageAdapter(
    val context: Context,
    val user: User,
    val friendUser: User,
    val list: List<Message>,
    val performDelete: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val FROM_TEXT = 10
        const val FROM_IMAGE = 11
        const val TO_TEXT = 20
        const val TO_IMAGE = 21
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
            Pair(MessageType.IMAGE, FROM_IMAGE)
        )
        val toValues = hashMapOf(
            Pair(MessageType.TEXT, TO_TEXT),
            Pair(MessageType.IMAGE, TO_IMAGE)
        )

        return if (list[position].fromUid == user.uid)
            fromValues.get(list[position].messageType) ?: -1
        else
            toValues.get(list[position].messageType) ?: -1

    }


}
