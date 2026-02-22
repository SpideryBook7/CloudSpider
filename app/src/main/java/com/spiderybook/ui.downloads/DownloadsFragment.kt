package com.spiderybook.ui.downloads

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spiderybook.databinding.FragmentDownloadsBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.ui.result.EpisodeAdapter
import com.spiderybook.domain.model.Episode
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding>(FragmentDownloadsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val spideryDir = File(downloadDir, "SpideryBook")
        
        if (spideryDir.exists()) {
            val files = spideryDir.listFiles()?.filter { it.isFile }?.mapIndexed { index, file ->
                Episode(
                     name = file.name,
                     url = file.absolutePath, // Local path
                     episode = index + 1
                )
            } ?: emptyList()
            
            binding.rvDownloads.layoutManager = LinearLayoutManager(context)
            binding.rvDownloads.adapter = EpisodeAdapter(
                items = files,
                onClick = { episode ->
                     // Play local file
                     val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                            putExtra("data", "file://${episode.url}")
                            putExtra("apiName", "Local")
                     }
                     startActivity(intent)
                },
                onDownloadClick = {
                     Toast.makeText(context, "El archivo ya está descargado", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "No downloads found", Toast.LENGTH_SHORT).show()
        }
    }
}
