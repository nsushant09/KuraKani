package com.neupanesushant.kurakani.activities.main.fragments.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.activities.main.fragments.chat.ChatViewModel
import com.neupanesushant.kurakani.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private lateinit var _binding : FragmentSearchBinding
    private val binding get() = _binding
    private lateinit var viewModel : SearchViewModel

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

    }

    fun setupSearchBar(){
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchbar, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.etSearchbar.setTextCursorDrawable(0)
        }
        binding.etSearchbar.requestFocus()
    }

}