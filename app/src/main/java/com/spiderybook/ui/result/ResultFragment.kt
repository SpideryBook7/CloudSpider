package com.spiderybook.ui.result

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import coil.load
import com.spiderybook.databinding.FragmentResultBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.util.Resource
import dagger.hilt.android.AndroidEntryPoint

import com.spiderybook.data.manager.AppDownloadManager
import javax.inject.Inject
// ...

@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>(FragmentResultBinding::inflate) {

    private val viewModel: ResultViewModel by viewModels()
    @Inject lateinit var downloadManager: AppDownloadManager
    
    // Keep adapter reference so we can feed it watch progress updates
    private var episodeAdapter: EpisodeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Temporarily fetching args manually
        val url = arguments?.getString("url") ?: return
        val apiName = arguments?.getString("apiName") ?: return

        setupObservers()
        viewModel.load(apiName, url)
    }

    override fun onResume() {
        super.onResume()
        // Hide status bar and navigation bar - Immersive mode
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        // Restore status bar
        activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onDestroyView() {
        // Clear adapter reference to avoid memory leaks
        episodeAdapter = null
        
        super.onDestroyView()
    }

    private fun setupObservers() {
        // Back Navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Tab Switching Logic
        binding.tvTabEpisodes.setOnClickListener {
            binding.rvEpisodes.isVisible = true
            binding.rvRecommendations.isVisible = false
            binding.rvRelated.isVisible = false
            binding.tvHeaderRelated.isVisible = false
            
            binding.tvTabEpisodes.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.on_primary))
            binding.tvTabEpisodes.setBackgroundResource(com.spiderybook.R.drawable.bg_tab_selected)
            
            binding.tvTabRecommendations.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.text_secondary))
            binding.tvTabRecommendations.setBackgroundResource(com.spiderybook.R.drawable.bg_tab_unselected)
        }
        
        binding.tvTabRecommendations.setOnClickListener {
            binding.rvEpisodes.isVisible = false
            binding.rvRecommendations.isVisible = true
            // Show related content if available
            val hasRelated = binding.rvRelated.adapter?.itemCount ?: 0 > 0
            binding.rvRelated.isVisible = hasRelated
            binding.tvHeaderRelated.isVisible = hasRelated
            
            binding.tvTabRecommendations.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.on_primary))
            binding.tvTabRecommendations.setBackgroundResource(com.spiderybook.R.drawable.bg_tab_selected)
            
            binding.tvTabEpisodes.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.text_secondary))
            binding.tvTabEpisodes.setBackgroundResource(com.spiderybook.R.drawable.bg_tab_unselected)
        }

        val url = arguments?.getString("url") ?: return
        
        // Observe History for Watch Progress
        viewModel.history.observe(viewLifecycleOwner) { historyList ->
            val progressMap = mutableMapOf<String, Int>()
            for (item in historyList) {
                if (item.duration > 0 && item.playbackPosition > 0) {
                    val percentage = ((item.playbackPosition.toDouble() / item.duration.toDouble()) * 100).toInt()
                    // Cap at 100 just in case
                    progressMap[item.url] = percentage.coerceAtMost(100)
                }
            }
            episodeAdapter?.setWatchProgress(progressMap)
        }

        viewModel.result.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.visibility = if (resource is Resource.Loading) View.VISIBLE else View.GONE
            
            if (resource is Resource.Success) {
                val data = resource.data
                
                binding.tvTitle.text = data.name

                binding.tvDescription.text = data.plot
                binding.tvYear.text = data.year?.toString() ?: ""
                binding.tvType.text = data.type?.name ?: "TV Series"
                binding.tvMetadata.text = "98% Match" 
                
                // Set Categories from tags
                val categories = data.tags?.joinToString(", ") ?: "Action, Adventure, Fantasy"
                binding.tvCategories.text = categories
                
                // Load backdrop poster
                binding.imgBackdrop.load(data.posterUrl) {
                    allowHardware(true)
                }
                binding.imgBackdrop.alpha = 1f
                
                // Favorites Observer (Moved here to use data.name)
                viewModel.isFavorite(url, data.name).observe(viewLifecycleOwner) { isFav ->
                    val btnFav = binding.btnFavorite
                    if (isFav) {
                        btnFav.setIconResource(android.R.drawable.btn_star_big_on)
                        btnFav.iconTint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                        btnFav.text = "Added"
                        btnFav.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.text_secondary))
                        btnFav.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.surface_dark))
                        btnFav.contentDescription = "Remove from My List"
                        btnFav.setOnClickListener {
                            viewModel.removeFromFavorites(url, data.name)
                            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        btnFav.setIconResource(android.R.drawable.ic_menu_add)
                        btnFav.iconTint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white))
                        btnFav.text = "My List"
                        btnFav.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white))
                        btnFav.backgroundTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), com.spiderybook.R.color.surface_light))
                        btnFav.contentDescription = "Add to My List"
                        btnFav.setOnClickListener {
                            viewModel.addToFavorites(url, data)
                            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                // Play Button Logic
                binding.btnPlay.setOnClickListener {
                     if (data.episodes.isNotEmpty()) {
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
                        }
                        startActivity(intent)
                     } else {
                         Toast.makeText(context, "No episodes available", Toast.LENGTH_SHORT).show()
                     }
                }
                
                episodeAdapter = EpisodeAdapter(
                    items = data.episodes,
                    onClick = { episode ->
                        val urls = java.util.ArrayList(data.episodes.map { it.url })
                        val names = java.util.ArrayList(data.episodes.map { it.name })
                        val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                            putExtra("data", episode.url)
                            putExtra("apiName", data.apiName)
                            putExtra("title", "${data.name} - ${episode.name}")
                            putExtra("poster", data.posterUrl)
                            putExtra("type", data.type)
                            putStringArrayListExtra("episodeUrls", urls)
                            putStringArrayListExtra("episodeNames", names)
                            putExtra("currentIndex", data.episodes.indexOf(episode))
                            putExtra("showName", data.name)
                        }
                        startActivity(intent)
                    },
                    onLongClick = { episode, isSeen ->
                        if (isSeen) {
                            viewModel.deleteEpisodeProgress(episode.url)
                            Toast.makeText(requireContext(), "Episodio desmarcado", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveEpisodeProgress(data, episode)
                            Toast.makeText(requireContext(), "Episodio marcado como visto", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDownloadClick = { episode ->
                        val cleanName = data.name.replace(Regex("[^A-Za-z0-9 ]"), "").trim()
                        val cleanEpName = episode.name.replace(Regex("[^A-Za-z0-9 ]"), "").trim()
                        val fileName = "${cleanName}_${cleanEpName}.mp4".replace(" ", "_")
                        viewModel.downloadEpisode(data.apiName, episode.url, fileName, downloadManager)
                    }
                )
                binding.rvEpisodes.adapter = episodeAdapter
                
                // Immediately feed any available history safely
                viewModel.history.value?.let { historyList ->
                    val progressMap = mutableMapOf<String, Int>()
                    for (item in historyList) {
                        if (item.duration > 0 && item.playbackPosition > 0) {
                            val percentage = ((item.playbackPosition.toDouble() / item.duration.toDouble()) * 100).toInt()
                            progressMap[item.url] = percentage.coerceAtMost(100)
                        }
                    }
                    episodeAdapter?.setWatchProgress(progressMap)
                }
                
                // Recommendations Adapter (Explicit Seasons)
                binding.rvRecommendations.adapter = com.spiderybook.ui.home.ChildItemAdapter(data.recommendations) { item ->
                     val bundle = Bundle().apply {
                         putString("url", item.url)
                         putString("apiName", item.apiName)
                     }
                     findNavController().navigate(com.spiderybook.R.id.action_nav_result_self, bundle)
                }

                // Related Adapter (Implicit Movies/Versions)
                binding.rvRelated.adapter = com.spiderybook.ui.home.ChildItemAdapter(data.related) { item ->
                     val bundle = Bundle().apply {
                         putString("url", item.url)
                         putString("apiName", item.apiName)
                     }
                     findNavController().navigate(com.spiderybook.R.id.action_nav_result_self, bundle)
                }
                
                // Show "Temporadas" tab only if ANY reference exists
                binding.tvTabRecommendations.text = if (data.apiName == "AnimeFLV") "Temporadas / Relacionados" else "More Like This"
                binding.tvTabRecommendations.isVisible = data.recommendations.isNotEmpty() || data.related.isNotEmpty()
                
            } else if (resource is Resource.Error) {
                 Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe TMDB Metadata
        viewModel.tmdbMetadata.observe(viewLifecycleOwner) { tmdbData ->
            if (tmdbData != null) {
                // Determine best high-res image available
                val bestImageUrl = tmdbData.getBackdropUrl() ?: tmdbData.getPosterUrl()
                bestImageUrl?.let { url ->
                    binding.imgBackdrop.load(url) {
                        allowHardware(true)
                    }
                    binding.imgBackdrop.animate().alpha(1f).setDuration(200).start()
                }
                
                // Update specific metadata if TMDB gives a better one
                if (!tmdbData.overview.isNullOrEmpty()) {
                    binding.tvDescription.text = tmdbData.overview
                }
                
                tmdbData.voteAverage?.let { rating ->
                    if (rating > 0.0) {
                        binding.tvMetadata.text = "⭐ ${String.format(java.util.Locale.US, "%.1f", rating)}/10"
                        binding.tvMetadata.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light))
                    }
                }
            }
        }
        
        // Setup Trailer Button Visibility and Intent
        viewModel.youtubeTrailerKey.observe(viewLifecycleOwner) { videoId ->
            if (!videoId.isNullOrEmpty()) {
                binding.btnTrailer.isVisible = true
                binding.btnTrailer.setOnClickListener {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com/watch?v=$videoId")
                        )
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir YouTube", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.btnTrailer.isVisible = false
            }
        }
        
        viewModel.downloadStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> Toast.makeText(context, "Obteniendo enlaces de descarga...", Toast.LENGTH_SHORT).show()
                is Resource.Success -> Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
