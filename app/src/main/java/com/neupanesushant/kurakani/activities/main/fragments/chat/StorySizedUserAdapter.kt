package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.StorySizedUserRecyclerViewLayoutBinding


class StorySizedUserAdapter(val context : Context, val viewModel : ChatViewModel, val list : List<User>) : RecyclerView.Adapter<StorySizedUserAdapter.ViewHolder>() {

    inner class ViewHolder(binding : StorySizedUserRecyclerViewLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val profileImage = binding.ivUserProfilePicture
        val fullName = binding.tvfullName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StorySizedUserRecyclerViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == 0){
            holder.profileImage.setImageResource(R.drawable.ic_pen)
            holder.fullName.text = "New Message"
            holder.itemView.setOnClickListener{
                viewModel.setNewMessageUIClicked(true)
            }
        }else{
            val userObject = list.get(position-1)
//            Picasso.get().load(userObject.profileImage).error(R.drawable.ic_user).into(holder.profileImage)
            Glide.with(context).load(userObject.profileImage).centerCrop().error(R.drawable.ic_user).into(holder.profileImage)
            holder.fullName.text = userObject.fullName
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

}