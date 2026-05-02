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
import android.content.Context
import android.view.inputmethod.InputMethodManager

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
            if (com.spiderybook.BuildConfig.FLAVOR == "legacy") {
                android.widget.Toast.makeText(requireContext(), "Cargando ${item.name}...", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.playDirectItem(item.apiName, item.url)
            } else {
                navigateToDetails(item.url, item.apiName, item.name, item.posterUrl, item.type?.name)
            }
        }
        childAdapter = ChildItemAdapter(emptyList()) { item ->
            if (com.spiderybook.BuildConfig.FLAVOR == "legacy") {
                android.widget.Toast.makeText(requireContext(), "Cargando ${item.name}...", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.playDirectItem(item.apiName, item.url)
            } else {
                navigateToDetails(item.url, item.apiName, item.name, item.posterUrl, item.type?.name)
            }
        }
        
        // Default to parent adapter
        binding.rvHome.apply {
            adapter = parentAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addItemDecoration(GridSpacingItemDecoration(3, resources.getDimensionPixelSize(com.spiderybook.R.dimen.grid_spacing), true))
        }
    }
    
    private fun setupFilterRecyclerView() {
        filterAdapter = FilterAdapter { category, _ ->
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
                     binding.rvHome.adapter = childAdapter 
                     childAdapter.updateList(emptyList())
                }
            }
        }
        
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            binding.spinnerProvider.isVisible = true
            
            if (viewModel.featuredItem.value != null) {
                binding.appbar.setExpanded(true, true)
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
                     viewModel.playFeaturedItem()
                 }
                 
                 binding.btnBannerInfo.setOnClickListener {
                     viewModel.toggleFeaturedFavorite()
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
        
        viewModel.featuredIsFavorite.observe(viewLifecycleOwner) { isFav ->
            val btnInfo = binding.btnBannerInfo
            if (isFav) {
                btnInfo.setIconResource(android.R.drawable.btn_star_big_on)
                btnInfo.iconTint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
            } else {
                btnInfo.setIconResource(android.R.drawable.ic_input_add)
                btnInfo.iconTint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        }

        viewModel.playFirstEpisodeEvent.observe(viewLifecycleOwner) { data ->
            if (data != null && data.episodes.isNotEmpty()) {
                val urls = java.util.ArrayList(data.episodes.map { it.url })
                val names = java.util.ArrayList(data.episodes.map { it.name })
                val firstEpisode = data.episodes.last()
                
                val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                    putExtra("data", firstEpisode.url)
                    putExtra("apiName", data.apiName)
                    putExtra("title", "${data.name} - ${firstEpisode.name}")
                    putExtra("poster", data.posterUrl)
                    putExtra("type", data.type)
                    putStringArrayListExtra("episodeUrls", urls)
                    putStringArrayListExtra("episodeNames", names)
                    putExtra("currentIndex", data.episodes.indexOf(firstEpisode))
                    putExtra("showName", data.name)
                    putExtra("showUrl", data.url)
                }
                startActivity(intent)
                viewModel.clearPlayFirstEpisodeEvent()
            }
        }

        viewModel.playSmartEpisodeEvent.observe(viewLifecycleOwner) { eventData ->
            if (eventData != null) {
                val data = eventData.first
                val targetIndex = eventData.second
                val urls = java.util.ArrayList(data.episodes.map { it.url })
                val names = java.util.ArrayList(data.episodes.map { it.name })
                val targetEpisode = data.episodes[targetIndex]
                
                val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                    putExtra("data", targetEpisode.url)
                    putExtra("apiName", data.apiName)
                    putExtra("title", "${data.name} - ${targetEpisode.name}")
                    putExtra("poster", data.posterUrl)
                    putExtra("type", data.type)
                    putStringArrayListExtra("episodeUrls", urls)
                    putStringArrayListExtra("episodeNames", names)
                    putExtra("currentIndex", targetIndex)
                    putExtra("showName", data.name)
                    putExtra("showUrl", data.url)
                }
                startActivity(intent)
                viewModel.clearPlaySmartEpisodeEvent()
            }
        }
        
    }
}
