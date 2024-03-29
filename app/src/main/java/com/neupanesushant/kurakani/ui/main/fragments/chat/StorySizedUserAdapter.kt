package com.neupanesushant.kurakani.ui.main.fragments.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.databinding.StorySizedUserRecyclerViewLayoutBinding
import com.neupanesushant.kurakani.domain.model.User


class StorySizedUserAdapter(
    val context: Context,
    val list: List<User>, val onClickOpenChatMessaging: (User) -> Unit
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
        val userObject = list[position]
        Glide.with(context).load(userObject.profileImage).apply(RequestOptions().circleCrop())
            .error(R.drawable.ic_user).into(holder.profileImage)
        holder.fullName.text = userObject.fullName
        holder.itemView.setOnClickListener {
            onClickOpenChatMessaging(userObject)
        }
    }

    override fun getItemCount(): Int {
        // return list.size + 1 for new message icons
        return list.size
    }

}