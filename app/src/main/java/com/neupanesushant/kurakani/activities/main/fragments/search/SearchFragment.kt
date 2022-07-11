package com.neupanesushant.kurakani.activities.main.fragments.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.MainViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chat.ChatViewModel
import com.neupanesushant.kurakani.activities.main.fragments.chatmessaging.ChatMessagingFragment
import com.neupanesushant.kurakani.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private lateinit var _binding : FragmentSearchBinding
    private val binding get() = _binding
    private lateinit var viewModel : SearchViewModel
    private val mainViewModel : MainViewModel by activityViewModels()
    private val chatMessagingFragment = ChatMessagingFragment()
    val onClickOpenChatMessaging : (uid : String) -> Unit = {
        mainViewModel.getFriendUserFromDatabase(it)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.fragment_container_view_tag, chatMessagingFragment)
            commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchBar()
        searchBarAction()

        binding.rvSearchedList.layoutManager = LinearLayoutManager(requireContext())
        viewModel.searchedList.observe(viewLifecycleOwner, Observer{
            if(it == null || it.size == 0){
                binding.tvInfoText.text = "Couldn't find a match"
                binding.rvSearchedList.visibility = View.GONE
                binding.tvInfoText.visibility = View.VISIBLE
            }else{
                val adapter = SearchedListAdapter(requireContext(), viewModel, it, onClickOpenChatMessaging)
                binding.rvSearchedList.adapter = adapter
                binding.rvSearchedList.visibility = View.VISIBLE
                binding.tvInfoText.visibility = View.GONE

            }
        })
    }

    fun setupSearchBar(){
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchbar, InputMethodManager.SHOW_IMPLICIT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.etSearchbar.setTextCursorDrawable(0)
        }
        binding.etSearchbar.requestFocus()
    }

    fun searchBarAction(){

        binding.etSearchbar.addTextChangedListener {
            if(it != null && it.length != 0){
                viewModel.filterSearch(it.toString())
            }
        }

    }

}