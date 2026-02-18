package com.spiderybook.ui.browse

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spiderybook.databinding.FragmentBrowseBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.ui.home.ChildItemAdapter
import com.spiderybook.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseFragment : BaseFragment<FragmentBrowseBinding>(FragmentBrowseBinding::inflate) {

    private val viewModel: BrowseViewModel by viewModels()
    private lateinit var adapter: ChildItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = ChildItemAdapter(emptyList()) { item ->
             val bundle = Bundle().apply {
                putString("url", item.url)
                putString("apiName", item.apiName)
                putString("title", item.name)
                putString("poster", item.posterUrl)
                putString("type", item.type?.name)
            }
            findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
        }
        
        val layoutManager = GridLayoutManager(context, 3)
        binding.rvBrowse.layoutManager = layoutManager
        binding.rvBrowse.adapter = adapter

        // Infinite Scroll Listener
        binding.rvBrowse.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= 20 // Only load if we have some items
                ) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.items.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            
            when (resource) {
                is Resource.Success -> {
                    android.util.Log.d("BrowseFragment", "Loaded ${resource.data.size} items")
                    adapter.updateList(resource.data)
                }
                is Resource.Error -> {
                    android.util.Log.e("BrowseFragment", "Error: ${resource.message}")
                    android.widget.Toast.makeText(context, "Error: ${resource.message}", android.widget.Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}
