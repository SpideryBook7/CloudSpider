package com.spiderybook.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spiderybook.databinding.FragmentFavoritesBinding
import com.spiderybook.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(FragmentFavoritesBinding::inflate) {

    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var adapter: FavoritesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            items = emptyList(),
            onClick = { item ->
                val bundle = Bundle().apply {
                    putString("url", item.url)
                    putString("apiName", item.apiName)
                }
                findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
            },
            onSelectionModeChanged = { isSelecting ->
                binding.fabDeleteSelection.isVisible = isSelecting
                if (isSelecting) {
                    binding.fabDeleteSelection.text = "Eliminar Selección"
                }
            }
        )
        binding.rvFavorites.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabDeleteSelection.setOnClickListener {
            val urlsToDelete = adapter.selectedUrls.toList()
            if (urlsToDelete.isNotEmpty()) {
                viewModel.deleteFavoriteItems(urlsToDelete)
                android.widget.Toast.makeText(requireContext(), "${urlsToDelete.size} eliminados", android.widget.Toast.LENGTH_SHORT).show()
            }
            adapter.clearSelection()
            binding.fabDeleteSelection.isVisible = false
        }
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.tabWatching.setOnClickListener { selectTab(binding.tabWatching, "Watching") }
        binding.tabWantToWatch.setOnClickListener { selectTab(binding.tabWantToWatch, "Want to Watch") }
        binding.tabCompleted.setOnClickListener { selectTab(binding.tabCompleted, "Completed") }
        
        // Select the first tab explicitly on start to ensure visual match with ViewModel default
        selectTab(binding.tabWatching, "Watching")
    }

    private fun selectTab(activeTab: android.widget.TextView, status: String) {
        val allTabs = listOf(binding.tabWatching, binding.tabWantToWatch, binding.tabCompleted)
        val accentColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.brand_accent)
        val whiteColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.white)
        
        allTabs.forEach { tab ->
            if (tab == activeTab) {
                tab.setBackgroundResource(com.spiderybook.R.drawable.bg_pill_accent)
                tab.setTextColor(whiteColor)
            } else {
                tab.setBackgroundResource(android.R.color.transparent)
                tab.setTextColor(accentColor)
            }
        }
        viewModel.setTab(status)
    }

    private fun setupObservers() {
        viewModel.favorites.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }
}
