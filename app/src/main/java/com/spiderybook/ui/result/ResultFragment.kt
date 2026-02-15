package com.spiderybook.ui.result

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
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

    private fun setupObservers() {
        viewModel.result.observe(viewLifecycleOwner) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            binding.tvError.isVisible = resource is Resource.Error
            
            if (resource is Resource.Success) {
                val data = resource.data
                binding.tvTitle.text = data.name
                binding.tvDescription.text = data.plot
                binding.imgPoster.load(data.posterUrl)
                
                binding.rvEpisodes.adapter = EpisodeAdapter(data.episodes) { episode ->
                    // Toast.makeText(context, "Play ${episode.name}", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                        putExtra("data", episode.url)
                        putExtra("apiName", data.apiName)
                    }
                    startActivity(intent)
                }
            } else if (resource is Resource.Error) {
                binding.tvError.text = resource.message
            }
        }
    }
}
