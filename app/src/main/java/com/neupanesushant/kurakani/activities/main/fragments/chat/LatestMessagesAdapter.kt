package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.Friend
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.LatestChatContentLayoutBinding

class LatestMessagesAdapter(val context : Context, val viewModel : ChatViewModel, val list : List<Friend>, val onClickOpenChatMessaging : (String) -> Unit ) : RecyclerView.Adapter<LatestMessagesAdapter.ViewHolder>(){

    inner class ViewHolder(binding : LatestChatContentLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val profileImage = binding.ivProfilePicture
        val userName = binding.tvUserName
        val latestMessage = binding.tvLatestMessageBody
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LatestChatContentLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userObject = list.get(position).user
        val messageObject = list.get(position).latestMessage
        Glide.with(context).load(userObject?.profileImage).centerCrop().error(R.drawable.ic_user).into(holder.profileImage)
        holder.userName.text = userObject?.fullName
        holder.latestMessage.text = messageObject?.messageBody
    }

    override fun getItemCount(): Int {
        return list.size
    }


}