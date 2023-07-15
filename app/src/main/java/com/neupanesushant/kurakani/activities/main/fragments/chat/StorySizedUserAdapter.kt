package com.neupanesushant.kurakani.activities.main.fragments.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.StorySizedUserRecyclerViewLayoutBinding


class StorySizedUserAdapter(
    val context: Context,
    val list: List<User>, val onClickOpenChatMessaging: (String) -> Unit
) : RecyclerView.Adapter<StorySizedUserAdapter.ViewHolder>() {

    inner class ViewHolder(binding: StorySizedUserRecyclerViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val profileImage = binding.ivUserProfilePicture
        val fullName = binding.tvfullName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StorySizedUserRecyclerViewLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.profileImage.setImageResource(R.drawable.ic_pen)
            holder.fullName.text = "New Message"
            holder.itemView.setOnClickListener {
            }
        } else {
            val userObject = list[position - 1]
            Glide.with(context).load(userObject.profileImage).apply(RequestOptions().circleCrop())
                .error(R.drawable.ic_user).into(holder.profileImage)
            holder.fullName.text = userObject.fullName
            holder.itemView.setOnClickListener {
                onClickOpenChatMessaging(userObject.uid!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

}