package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.LatestChatContentLayoutBinding

class LatestMessagesAdapter(
    val context: Context,
    val viewModel: ChatViewModel,
    val onClickOpenChatMessaging: (String) -> Unit
) : RecyclerView.Adapter<LatestMessagesAdapter.ViewHolder>() {

    val messagesList : List<Message>? = viewModel.latestMessages.value
    val usersList : List<User>? = viewModel.usersOfLatestMessages.value

    inner class ViewHolder(binding: LatestChatContentLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val profileImage = binding.ivProfilePicture
        val userName = binding.tvUserName
        val latestMessage = binding.tvLatestMessageBody
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LatestChatContentLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageObject = messagesList?.get(position)
        val userObject = usersList?.get(position)

        holder.itemView.animation = AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        holder.itemView.setOnClickListener{
            onClickOpenChatMessaging(userObject?.uid.toString())
        }
        Glide.with(context).load(userObject?.profileImage).centerCrop().error(R.drawable.ic_user).into(holder.profileImage)
        holder.userName.text = userObject?.fullName
        holder.latestMessage.text = messageObject?.messageBody

    }

    override fun getItemCount(): Int {
        if(usersList == null || usersList.size == 0){
            return 0
        }else{
            return usersList.size
        }
    }



}