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
import coil.load
import com.spiderybook.util.hideKeyboard

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var filterAdapter: FilterAdapter
    private lateinit var childAdapter: ChildItemAdapter // Reuse for Grid
    private lateinit var parentAdapter: ParentItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterRecyclerView()
        setupObservers()
        setupSpinner()
        setupFab()
    }

    private fun setupRecyclerView() {
        // Initialize adapters
        parentAdapter = ParentItemAdapter { item ->
            navigateToDetails(item.url, item.apiName, item.name, item.posterUrl, item.type?.name)
        }
        childAdapter = ChildItemAdapter(emptyList()) { item ->
             navigateToDetails(item.url, item.apiName, item.name, item.posterUrl, item.type?.name)
        }
        
        // Default to parent adapter
        binding.rvHome.apply {
            adapter = parentAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
    }
    
    private fun setupFilterRecyclerView() {
        filterAdapter = FilterAdapter { category ->
            viewModel.selectCategory(category)
        }
        binding.rvFilter.apply {
            adapter = filterAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        }
    }
    
    private fun navigateToDetails(url: String, apiName: String, title: String, poster: String?, type: String?) {
        val bundle = Bundle().apply {
            putString("url", url)
            putString("apiName", apiName)
            putString("title", title)
            putString("poster", poster)
            putString("type", type)
        }
        findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
    }

    private fun setupFab() {
        // Scroll Up FAB Logic - Return to Top
        binding.fabScrollUp.setOnClickListener {
            // Scroll the NestedScrollView to the top
            binding.nsvHome.smoothScrollTo(0, 0)
            // Always expand the header
            binding.appbar.setExpanded(true, true)
        }
        
        // Settings Button Logic (Moved to Toolbar)
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(com.spiderybook.R.id.nav_settings)
        }
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
            
            if (resource is Resource.Error) {
                binding.tvError.text = resource.message
            }
        }
        
        viewModel.filterCategories.observe(viewLifecycleOwner) { categories ->
            filterAdapter.submitList(categories)
        }
        
        viewModel.displayedContent.observe(viewLifecycleOwner) { content ->
            if (content is List<*>) {
                if (content.isNotEmpty() && content.first() is com.spiderybook.domain.model.HomePageList) {
                    // It's a list of sections (Inicio)
                    @Suppress("UNCHECKED_CAST")
                    val items = content as List<com.spiderybook.domain.model.HomePageList>
                    binding.rvHome.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                    binding.rvHome.adapter = parentAdapter
                    parentAdapter.submitList(items)
                } else if (content.isNotEmpty() && content.first() is com.spiderybook.domain.model.SearchResponse) {
                    // It's a flat list of items (Letter Grid)
                    @Suppress("UNCHECKED_CAST")
                    val items = content as List<com.spiderybook.domain.model.SearchResponse>
                    binding.rvHome.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 3)
                    binding.rvHome.adapter = childAdapter
                    childAdapter.updateList(items)
                } else if (content.isEmpty()) {
                     // Empty list handling
                     // Check selected category to decide layout? Or just clear adapter?
                     // Defaulting to grid/child adapter for empty letter results
                     binding.rvHome.adapter = childAdapter // or parent depending on context
                     childAdapter.updateList(emptyList())
                }
            }
        }
        
        viewModel.featuredItem.observe(viewLifecycleOwner) { featured ->
             if (featured != null) {
                 binding.appbar.setExpanded(true, true)
                 binding.imgBanner.load(featured.posterUrl) {
                     crossfade(true)
                 }
                 binding.tvBannerTitle.text = featured.name
                 
                 binding.btnBannerPlay.setOnClickListener {
                     navigateToDetails(featured.url, featured.apiName, featured.name, featured.posterUrl, featured.type?.name)
                 }
                 
                 binding.btnBannerInfo.setOnClickListener {
                     navigateToDetails(featured.url, featured.apiName, featured.name, featured.posterUrl, featured.type?.name)
                 }
             } else {
                 binding.appbar.setExpanded(false, false)
             }
        }
        
        viewModel.availableProviders.observe(viewLifecycleOwner) { providers ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, providers)
            binding.spinnerProvider.adapter = adapter
        }
        
        viewModel.selectedProvider.observe(viewLifecycleOwner) { selected ->
            @Suppress("UNCHECKED_CAST")
            val adapter = binding.spinnerProvider.adapter as? ArrayAdapter<String>
            if (adapter != null && selected != null) {
                val position = adapter.getPosition(selected)
                if (position >= 0 && binding.spinnerProvider.selectedItemPosition != position) {
                    binding.spinnerProvider.setSelection(position, false)
                }
            }
        }
        
    }
}
