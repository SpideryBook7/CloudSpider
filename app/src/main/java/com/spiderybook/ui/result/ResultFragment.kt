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
        
        binding.btnDownload.setOnClickListener {
             // For test, download the first episode or just a sample
             // Ideally we need to extract links first. 
             // Simplification: We download user selected episode. 
             // But button is global for now. Let's just download sample.
             downloadManager.download("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "sample_video.mp4")
             Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
        }
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
        
        // ... (Favorites Observer) ...
        viewModel.isFavorite(url).observe(viewLifecycleOwner) { isFav ->
            val fab = binding.fabFavorite
            if (isFav) {
                fab.setImageResource(android.R.drawable.btn_star_big_on)
                fab.contentDescription = "Remove from My List"
                fab.setOnClickListener {
                    viewModel.removeFromFavorites(url)
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                }
            } else {
                fab.setImageResource(android.R.drawable.btn_star_big_off)
                fab.contentDescription = "Add to My List"
                fab.setOnClickListener {
                    val currentData = (viewModel.result.value as? Resource.Success)?.data
                    if (currentData != null) {
                        viewModel.addToFavorites(currentData)
                        Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Wait for data to load", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
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
                
                binding.imgPoster.load(data.posterUrl) {
                    crossfade(true)
                }
                
                // Play Button Logic
                binding.btnPlay.setOnClickListener {
                     if (data.episodes.isNotEmpty()) {
                         val firstEpisode = data.episodes.first()
                         val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                            putExtra("data", firstEpisode.url)
                            putExtra("apiName", data.apiName)
                            putExtra("title", "${data.name} - ${firstEpisode.name}")
                            putExtra("poster", data.posterUrl)
                            putExtra("type", data.type)
                        }
                        startActivity(intent)
                     } else {
                         Toast.makeText(context, "No episodes available", Toast.LENGTH_SHORT).show()
                     }
                }
                
                episodeAdapter = EpisodeAdapter(
                    items = data.episodes,
                    onClick = { episode ->
                        val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                            putExtra("data", episode.url)
                            putExtra("apiName", data.apiName)
                            putExtra("title", "${data.name} - ${episode.name}")
                            putExtra("poster", data.posterUrl)
                            putExtra("type", data.type)
                        }
                        startActivity(intent)
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
        
        viewModel.downloadStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> Toast.makeText(context, "Obteniendo enlaces de descarga...", Toast.LENGTH_SHORT).show()
                is Resource.Success -> Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
