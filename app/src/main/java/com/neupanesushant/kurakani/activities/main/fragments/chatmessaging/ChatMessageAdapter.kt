package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

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
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.MessageType
import com.neupanesushant.kurakani.databinding.ChatImageFromLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatImageToLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatMessageFromLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatMessageToLayoutBinding

class ChatMessageAdapter(
    val context: Context,
    val viewModel: MainViewModel,
    val list: List<Message>,
    val performDelete: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val FROM_TEXT = 10
    private val FROM_IMAGE = 11
    private val TO_TEXT = 20
    private val TO_IMAGE = 21

    private inner class ViewHolderFromText(binding: ChatMessageFromLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var profileImage: ImageView
        var messageBody: TextView

        init {
            profileImage = binding.ivProfileImage
            messageBody = binding.tvMessageBody
        }

        internal fun bind(position: Int) {
            Glide.with(context).load(viewModel.user.value?.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
            messageBody.text = list.get(position).messageBody

            itemView.setOnLongClickListener {
                performDelete(list[position])
                true
            }
        }
    }

    private inner class ViewHolderToText(binding: ChatMessageToLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var profileImage: ImageView
        var messageBody: TextView

        init {
            profileImage = binding.ivProfileImage
            messageBody = binding.tvMessageBody
        }

        internal fun bind(position: Int) {
            Glide.with(context).load(viewModel.friendUser.value?.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)
            messageBody.text = list.get(position).messageBody

            itemView.setOnLongClickListener {
                performDelete(list[position])
                true
            }
        }

    }

    private inner class ViewHolderFromImage(binding: ChatImageFromLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var profileImage: ImageView
        var messageBody: ImageView

        init {
            profileImage = binding.ivProfileImage
            messageBody = binding.ivChatImage
        }

        internal fun bind(position: Int) {
            Glide.with(context).load(viewModel.user.value?.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)

            Glide.with(context)
                .asBitmap()
                .load(list.get(position).messageBody)
                .apply(RequestOptions().override(480))
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(messageBody)

            itemView.setOnLongClickListener {
                performDelete(list[position])
                true
            }
        }

    }

    private inner class ViewHolderToImage(binding: ChatImageToLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var profileImage: ImageView
        var messageBody: ImageView

        init {
            profileImage = binding.ivProfileImage
            messageBody = binding.ivChatImage
        }

        internal fun bind(position: Int) {
            Glide.with(context).load(viewModel.friendUser.value?.profileImage)
                .apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(profileImage)

            Glide.with(context)
                .asBitmap()
                .load(list.get(position).messageBody)
                .apply(RequestOptions().override(480))
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(messageBody)
            itemView.setOnLongClickListener {
                performDelete(list[position])
                true
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FROM_TEXT) {
            ViewHolderFromText(
                ChatMessageFromLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if (viewType == FROM_IMAGE) {
            ViewHolderFromImage(
                ChatImageFromLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if (viewType == TO_IMAGE) {
            ViewHolderToImage(
                ChatImageToLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ViewHolderToText(
                ChatMessageToLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (list.get(position).fromUid == viewModel.user.value?.uid) {

            if (position == 0) {
                holder.itemView.animation =
                    AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
            }
            if (list.get(position).messageType == MessageType.TEXT) {
                (holder as ViewHolderFromText).bind(position)
            }

            if (list.get(position).messageType == MessageType.IMAGE) {
                (holder as ViewHolderFromImage).bind(position)
            }
        } else {
            if (position == 0) {
                holder.itemView.animation =
                    AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
            }
            if (list.get(position).messageType == MessageType.TEXT) {
                (holder as ViewHolderToText).bind(position)
            }
            if (list.get(position).messageType == MessageType.IMAGE) {
                (holder as ViewHolderToImage).bind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position].fromUid == viewModel.user.value?.uid) {
            if (list[position].messageType == MessageType.TEXT) {
                return FROM_TEXT
            }
            if (list[position].messageType == MessageType.IMAGE) {
                return FROM_IMAGE
            }
        } else {
            if (list[position].messageType == MessageType.TEXT) {
                return TO_TEXT
            }
            if (list[position].messageType == MessageType.IMAGE) {
                return TO_IMAGE
            }
        }

        return -1
    }


}
