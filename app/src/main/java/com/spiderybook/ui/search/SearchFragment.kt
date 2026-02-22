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
import com.spiderybook.domain.model.SearchResponse
import coil.load
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchResultsAdapter: ChildItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSearchView()
        setupObservers()
    }

    private fun setupRecyclerViews() {
        // Results Adapter
        searchResultsAdapter = ChildItemAdapter(emptyList()) { item ->
            navigateToDetails(item)
        }
        binding.rvSearchResults.adapter = searchResultsAdapter
        
        binding.rvSearchResults.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 6) { // Load more when 6 items away
                        val currentQuery = binding.searchView.query.toString()
                        if (currentQuery.isNotEmpty()) {
                            viewModel.search(null, currentQuery, isLoadMore = true)
                        }
                    }
                }
            }
        })
    }

    private fun navigateToDetails(item: SearchResponse) {
        val bundle = Bundle().apply {
            putString("url", item.url)
            putString("apiName", item.apiName)
        }
        findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    binding.searchView.clearFocus() // Hide keyboard
                    viewModel.search(null, query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    showDefaultView(true)
                }
                return true
            }
        })
    }

    private fun showDefaultView(show: Boolean) {
        binding.scrollDefaultView.isVisible = show
        binding.rvSearchResults.isVisible = !show
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            
            when (resource) {
                is Resource.Loading -> {
                    showDefaultView(false)
                    // Do not clear the adapter here to avoid flickering during "Load More"
                }
                is Resource.Success -> {
                    showDefaultView(false)
                    val data = resource.data
                    if (data.isEmpty()) {
                        Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                    }
                    searchResultsAdapter.updateList(data)
                }
                is Resource.Error -> {
                    showDefaultView(false)
                    searchResultsAdapter.updateList(emptyList())
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.genres.observe(viewLifecycleOwner) { genres ->
            // Use a simple adapter for genres
            setupGenreAdapter(genres)
        }

        viewModel.topSearches.observe(viewLifecycleOwner) { items ->
            setupTopSearchesAdapter(items)
        }
    }

    private fun setupGenreAdapter(genres: List<String>) {
        binding.rvGenres.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<GenreViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): GenreViewHolder {
                val b = com.spiderybook.databinding.ItemGenreBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false
                )
                return GenreViewHolder(b)
            }
            override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
                val genre = genres[position]
                holder.binding.tvGenreName.text = genre
                holder.binding.root.setOnClickListener {
                    // Start search for the selected genre
                    binding.searchView.setQuery(genre, true)
                }
            }
            override fun getItemCount(): Int = genres.size
        }
    }

    private fun setupTopSearchesAdapter(items: List<SearchResponse>) {
        binding.rvTopSearches.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<TopSearchViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TopSearchViewHolder {
                val b = com.spiderybook.databinding.ItemTopSearchBinding.inflate(
                    android.view.LayoutInflater.from(parent.context), parent, false
                )
                return TopSearchViewHolder(b)
            }
            override fun onBindViewHolder(holder: TopSearchViewHolder, position: Int) {
                val item = items[position]
                holder.binding.tvTitle.text = item.name
                holder.binding.tvMetadata.text = "${item.type?.name ?: "Unknown"} • ${item.year ?: "N/A"}"
                holder.binding.imgPoster.load(item.posterUrl) {
                    placeholder(com.spiderybook.R.color.surface_variant)
                }
                holder.binding.root.setOnClickListener { navigateToDetails(item) }
            }
            override fun getItemCount(): Int = items.size
        }
    }

    class GenreViewHolder(val binding: com.spiderybook.databinding.ItemGenreBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
        
    class TopSearchViewHolder(val binding: com.spiderybook.databinding.ItemTopSearchBinding) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
}
