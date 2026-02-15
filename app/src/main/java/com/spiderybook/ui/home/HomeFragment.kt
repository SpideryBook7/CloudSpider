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
import com.spiderybook.util.hideKeyboard

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var parentAdapter: ParentItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSpinner()
        setupSearch()
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
    
    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                viewModel.search(query)
                com.spiderybook.util.hideKeyboard(v)
                return@setOnEditorActionListener true
            }
            false
        }
        
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            viewModel.search("") // Reload home
            binding.btnClearSearch.isVisible = false
            com.spiderybook.util.hideKeyboard(it)
        }
        
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearSearch.isVisible = !s.isNullOrEmpty()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupSpinner() {
        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val providerName = parent?.getItemAtPosition(position) as? String
                // Only load if it's a DIFFERENT provider, to avoid reloading on rotation/recreation if handled by VM
                // But VM handles checks too.
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
        }
        
        viewModel.selectedProvider.observe(viewLifecycleOwner) { selected ->
            val adapter = binding.spinnerProvider.adapter as? ArrayAdapter<String>
            if (adapter != null && selected != null) {
                val position = adapter.getPosition(selected)
                if (position >= 0 && binding.spinnerProvider.selectedItemPosition != position) {
                    binding.spinnerProvider.setSelection(position, false)
                }
            }
        }
        
        binding.btnDownloads.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_downloads)
        }
        
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_settings)
        }
    }
}
