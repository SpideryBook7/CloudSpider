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
    
    private fun setupSpinner() {
        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val providerName = parent?.getItemAtPosition(position) as? String
                providerName?.let { viewModel.loadHomePage(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.homePage.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            binding.tvError.isVisible = resource is Resource.Error
            binding.rvHome.isVisible = resource is Resource.Success
            
            when (resource) {
                is Resource.Success -> {
                    parentAdapter.submitList(resource.data.items)
                }
                is Resource.Error -> {
                    binding.tvError.text = resource.message
                }
                else -> {}
            }
        }
        
        viewModel.availableProviders.observe(viewLifecycleOwner) { providers ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, providers)
            binding.spinnerProvider.adapter = adapter
            
            // Should probably select the current one
        }
        
        binding.btnDownloads.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_downloads)
        }
        
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_settings)
        }
    }
}
