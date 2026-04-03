package com.spiderybook.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spiderybook.databinding.FragmentSearchBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.ui.home.ChildItemAdapter
import com.spiderybook.ui.home.FilterAdapter
import com.spiderybook.ui.home.GridSpacingItemDecoration
import com.spiderybook.util.Resource
import com.spiderybook.domain.model.SearchResponse
import coil.load
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchResultsAdapter: ChildItemAdapter
    private lateinit var filterAdapter: FilterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupFilterRecyclerView()
        setupSearchView()
        setupObservers()
        
        // Trigger initial empty search if no query is set
        if (binding.etSearch.text.isNullOrEmpty()) {
             viewModel.search(null, "")
        }
    }

    private fun setupRecyclerViews() {
        // Results Adapter
        searchResultsAdapter = ChildItemAdapter(emptyList()) { item ->
            navigateToDetails(item)
        }
        binding.rvSearchResults.apply {
            adapter = searchResultsAdapter
            addItemDecoration(GridSpacingItemDecoration(3, resources.getDimensionPixelSize(com.spiderybook.R.dimen.grid_spacing), true))
        }
        
        binding.nsvSearchResults.setOnScrollChangeListener { v: androidx.core.widget.NestedScrollView, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && !v.canScrollVertically(1)) {
                // User scrolled to the bottom
                triggerLoadMore()
            }
        }
        
        binding.btnLoadMore.setOnClickListener {
            triggerLoadMore()
        }
    }
    
    private fun triggerLoadMore() {
        val currentQuery = binding.etSearch.text.toString()
        if (currentQuery.isNotEmpty()) {
            Toast.makeText(requireContext(), "Loading next page...", Toast.LENGTH_SHORT).show()
            viewModel.search(null, currentQuery, isLoadMore = true)
        } else {
            val categories = viewModel.selectedCategories.value
            if (!categories.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Loading next page...", Toast.LENGTH_SHORT).show()
                viewModel.search(null, categories.joinToString(","), isLoadMore = true)
            }
        }
    }

    private fun setupFilterRecyclerView() {
        filterAdapter = FilterAdapter { category, view ->
            viewModel.selectCategory(category)
            if (category == "All") {
                showGenreDropdown(view)
            } else {
                binding.etSearch.setText("")
                val categories = viewModel.selectedCategories.value
                if (!categories.isNullOrEmpty()) {
                    viewModel.search(null, categories.joinToString(","))
                } else {
                    val topHits = viewModel.topSearches.value
                    if (!topHits.isNullOrEmpty()) {
                        searchResultsAdapter.updateList(topHits)
                        binding.btnLoadMore.isVisible = false
                    } else {
                        viewModel.search(null, "")
                    }
                }
            }
        }
        binding.rvFilter.apply {
            adapter = filterAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        }
        
        // Handle mocked "Clear all"
        binding.tvClearFilters.setOnClickListener {
            viewModel.selectCategory("All")
            filterAdapter.setSelection("All")
            binding.etSearch.setText("")
            val topHits = viewModel.topSearches.value
            if (!topHits.isNullOrEmpty()) {
                searchResultsAdapter.updateList(topHits)
                binding.btnLoadMore.isVisible = false
            } else {
                viewModel.search(null, "")
            }
            Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGenreDropdown(anchor: android.view.View) {
        val genres = viewModel.genres.value ?: return
        if (genres.isEmpty()) return
        
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        genres.forEachIndexed { index, genre ->
            popup.menu.add(0, index, 0, genre)
        }
        
        filterAdapter.setDropdownOpen(true)
        
        popup.setOnMenuItemClickListener { menuItem ->
            val selectedGenre = genres[menuItem.itemId]
            binding.etSearch.setText("")
            viewModel.selectCategory(selectedGenre)
            filterAdapter.setSelection(selectedGenre)
            
            val categories = viewModel.selectedCategories.value
            if (!categories.isNullOrEmpty()) {
                viewModel.search(null, categories.joinToString(","))
            }
            true
        }
        popup.setOnDismissListener {
            filterAdapter.setDropdownOpen(false)
        }
        popup.show()
    }

    private fun navigateToDetails(item: SearchResponse) {
        val bundle = Bundle().apply {
            putString("url", item.url)
            putString("apiName", item.apiName)
        }
        findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
    }

    private fun setupSearchView() {
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.search(null, query)
                }
                
                // Hide keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                
                true
            } else {
                false
            }
        }
        
        binding.btnFilterOptions.setOnClickListener {
            val isNowVisible = !binding.rvFilter.isVisible
            binding.rvFilter.isVisible = isNowVisible
            
            // Only show active filters if the filters section is open AND a filter is actually selected
            if (isNowVisible && !viewModel.selectedCategories.value.isNullOrEmpty()) {
                binding.layoutActiveFilters.isVisible = true
            } else {
                binding.layoutActiveFilters.isVisible = false
            }
        }
    }

    private fun setupObservers() {
        viewModel.filterCategories.observe(viewLifecycleOwner) { categories ->
            filterAdapter.submitList(categories)
        }

        viewModel.selectedCategories.observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                if (binding.rvFilter.isVisible) {
                    binding.layoutActiveFilters.isVisible = true
                }
                binding.llActiveFiltersContainer.removeAllViews()
                
                categories.forEach { category ->
                    val tv = android.widget.TextView(requireContext()).apply {
                        text = category
                        gravity = android.view.Gravity.CENTER
                        setPadding(32, 0, 32, 0)
                        setTextColor(androidx.core.content.ContextCompat.getColor(context, com.spiderybook.R.color.brand_accent))
                        textSize = 12f
                        android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD)
                        setBackgroundResource(com.spiderybook.R.drawable.bg_pill_dark)
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_close_clear_cancel, 0)
                        compoundDrawablePadding = 8
                        compoundDrawables[2]?.setTint(androidx.core.content.ContextCompat.getColor(context, com.spiderybook.R.color.brand_accent))
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            (32 * resources.displayMetrics.density).toInt()
                        ).apply {
                            marginEnd = 16
                        }
                        
                        setOnClickListener {
                            viewModel.removeCategory(category)
                            if (viewModel.selectedCategories.value.isNullOrEmpty()) {
                                filterAdapter.setSelection("All")
                                binding.etSearch.setText("")
                                val topHits = viewModel.topSearches.value
                                if (!topHits.isNullOrEmpty()) {
                                    searchResultsAdapter.updateList(topHits)
                                    binding.btnLoadMore.isVisible = false
                                } else {
                                    viewModel.search(null, "")
                                }
                            } else {
                                // Trigger a new search with the remaining categories
                                viewModel.search(null, viewModel.selectedCategories.value!!.joinToString(","))
                            }
                        }
                    }
                    binding.llActiveFiltersContainer.addView(tv)
                }
            } else {
                binding.layoutActiveFilters.isVisible = false
                binding.llActiveFiltersContainer.removeAllViews()
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            
            when (resource) {
                is Resource.Loading -> {
                    // Do not clear the adapter here to avoid flickering during "Load More"
                }
                is Resource.Success -> {
                    val data = resource.data
                    if (data.isEmpty()) {
                        binding.btnLoadMore.isVisible = false
                        Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.btnLoadMore.isVisible = true
                    }
                    searchResultsAdapter.updateList(data)
                }
                is Resource.Error -> {
                    binding.btnLoadMore.isVisible = false
                    searchResultsAdapter.updateList(emptyList())
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.topSearches.observe(viewLifecycleOwner) { items ->
            // Populate grid with Top Searches by default if no search query
            if (binding.etSearch.text.isEmpty()) {
                searchResultsAdapter.updateList(items)
                binding.btnLoadMore.isVisible = false
            }
        }
    }
}
