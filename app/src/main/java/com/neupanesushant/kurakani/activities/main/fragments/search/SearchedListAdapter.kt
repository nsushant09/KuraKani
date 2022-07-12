package com.neupanesushant.kurakani.activities.main.fragments.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.SearchedListRecyclerViewLayoutBinding

class SearchedListAdapter(val context : Context, val viewModel: SearchViewModel, val list : List<User>, val onClickOpenChatMessaging : (uid : String) -> Unit) : RecyclerView.Adapter<SearchedListAdapter.ViewHolder>(){

    inner class ViewHolder(binding : SearchedListRecyclerViewLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val profileImage = binding.ivUserProfilePicture
        val fullName = binding.tvUserFullName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SearchedListRecyclerViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userObject = list.get(position)
        Glide.with(context).load(userObject.profileImage).centerCrop().error(R.drawable.ic_user).into(holder.profileImage)
        holder.fullName.text = userObject.fullName
        holder.itemView.setOnClickListener{
            onClickOpenChatMessaging(userObject.uid!!)
        }
    }

    override fun getItemCount(): Int = list.size
}