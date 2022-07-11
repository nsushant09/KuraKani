package com.neupanesushant.kurakani.activities.main.fragments.chatmessaging

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.classes.Message
import com.neupanesushant.kurakani.databinding.ChatMessageFromLayoutBinding
import com.neupanesushant.kurakani.databinding.ChatMessageToLayoutBinding

class ChatMessageAdapter(val context : Context, val viewModel:  MainViewModel, val list : List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal val FROM = 1
    internal val TO = 2

    private inner class ViewHolderFrom(binding : ChatMessageFromLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        var profileImage : ImageView
        var messageBody : TextView

        init{
            profileImage = binding.ivProfileImage
            messageBody = binding.tvMessageBody
        }

        internal fun bind(position: Int){
            Glide.with(context).load(viewModel.user.value?.profileImage).centerCrop().error(R.drawable.ic_user).into(profileImage)
            messageBody.text = list.get(position).messageBody
        }
    }

    private inner class ViewHolderTo(binding : ChatMessageToLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        var profileImage : ImageView
        var messageBody : TextView

        init{
            profileImage = binding.ivProfileImage
            messageBody = binding.tvMessageBody
        }

        internal fun bind(position: Int){
            Glide.with(context).load(viewModel.friendUser.value?.profileImage).centerCrop().error(R.drawable.ic_user).into(profileImage)
            messageBody.text = list.get(position).messageBody
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == FROM){
            ViewHolderFrom(
                ChatMessageFromLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }else{
            ViewHolderTo(
                ChatMessageToLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(list.get(position).fromUid == viewModel.user.value?.uid){
            (holder as ViewHolderFrom).bind(position)
        }else{
            (holder as ViewHolderTo).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if(list.get(position).fromUid == viewModel.user.value?.uid){
            return FROM
        }else{
            return TO
        }
    }

}