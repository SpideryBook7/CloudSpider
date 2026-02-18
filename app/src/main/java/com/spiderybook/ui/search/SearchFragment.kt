package com.spiderybook.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spiderybook.databinding.FragmentSearchBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.ui.home.ChildItemAdapter
import com.spiderybook.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ChildItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = ChildItemAdapter(emptyList()) { item ->
            val bundle = Bundle().apply {
                putString("url", item.url)
                putString("apiName", item.apiName)
            }
            findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
        }
        binding.rvSearchResults.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // Pass null to let ViewModel pick the first available provider
                    viewModel.search(null, query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            
            if (resource is Resource.Success) {
                // Ugly hack to update adapter list since it's immutable in ChildItemAdapter 
                // We should refactor ChildItemAdapter to use ListAdapter or similar
                // For now, re-creating adapter or adding update method
                binding.rvSearchResults.adapter = ChildItemAdapter(resource.data) { item ->
                     Toast.makeText(context, "Selected: ${item.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
