package com.neupanesushant.kurakani.activities.main.fragments.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingFragment
import com.neupanesushant.kurakani.classes.User
import com.neupanesushant.kurakani.databinding.FragmentSearchBinding
import com.neupanesushant.kurakani.services.AuthenticatedUser
import org.koin.android.ext.android.inject


class SearchFragment : Fragment() {

    private lateinit var _binding: FragmentSearchBinding
    private val binding get() = _binding
    private val mainViewModel: MainViewModel by inject()
    private val viewModel: SearchViewModel by inject()

    private val onClickOpenChatMessaging: (uid: String) -> Unit = { uid ->
        val chatMessagingFragment =
            ChatMessagingFragment(AuthenticatedUser.getInstance().getUser()!!, uid)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatMessagingFragment)
            commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupEventListener()
        setupObserver()
    }

    private fun setupView() {
        binding.rvSearchedList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchedList.animation =
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
    }

    @SuppressLint("SetTextI18n")
    private fun setupObserver() {

        viewModel.allUser.observe(viewLifecycleOwner) { users ->
            users?.let {
                setUsersData(users)
            }
        }

        viewModel.searchedList.observe(viewLifecycleOwner) {
            if (it == null || it.isEmpty()) {
                binding.tvInfoText.text = "Couldn't find a match"
                binding.rvSearchedList.visibility = View.GONE
                binding.tvInfoText.visibility = View.VISIBLE
            } else {
                setUsersData(it)
            }
        }
    }

    private fun setUsersData(users: List<User>) {
        val adapter =
            SearchedListAdapter(requireContext(), users, onClickOpenChatMessaging)
        binding.rvSearchedList.adapter = adapter
        binding.rvSearchedList.visibility = View.VISIBLE
        binding.tvInfoText.visibility = View.GONE
    }

    private fun setupEventListener() {
        setupSearchBar()
        searchBarAction()
    }

    private fun setupSearchBar() {
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchbar, InputMethodManager.SHOW_IMPLICIT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.etSearchbar.setTextCursorDrawable(0)
        }
        binding.etSearchbar.requestFocus()
    }

    private fun searchBarAction() {

        binding.etSearchbar.addTextChangedListener {
            if (it != null && it.isNotEmpty()) {
                viewModel.filterSearch(it.toString())
            }
        }

    }

}