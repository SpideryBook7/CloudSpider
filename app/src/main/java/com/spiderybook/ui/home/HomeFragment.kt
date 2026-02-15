package com.spiderybook.ui.home

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.spiderybook.databinding.FragmentHomeBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.util.Resource
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var parentAdapter: ParentItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSpinner()
    }

    private fun setupRecyclerView() {
        parentAdapter = ParentItemAdapter { item ->
            // Toast.makeText(requireContext(), "Clicked: ${item.name}", Toast.LENGTH_SHORT).show()
            val bundle = Bundle().apply {
                putString("url", item.url)
                putString("apiName", item.apiName)
            }
            findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
        }
        binding.rvHome.adapter = parentAdapter
    }
    
    // ...

    private fun setupObservers() {
        // ...
        
        binding.btnDownloads.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_downloads)
        }
        
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_settings)
        }
    }
}
