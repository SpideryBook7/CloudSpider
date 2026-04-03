package com.spiderybook.ui.downloads

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.spiderybook.databinding.FragmentDownloadsBinding
import com.spiderybook.ui.common.BaseFragment
import com.spiderybook.ui.result.EpisodeAdapter
import com.spiderybook.domain.model.Episode
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding>(FragmentDownloadsBinding::inflate) {

    private lateinit var activeAdapter: ActiveDownloadAdapter
    private var lastActiveCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupStorageStats()
        setupActiveDownloads()
        loadDownloads()
    }

    private fun setupActiveDownloads() {
        activeAdapter = ActiveDownloadAdapter(emptyList()) { item ->
            val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.remove(item.id)
            Toast.makeText(context, "Descarga cancelada", Toast.LENGTH_SHORT).show()
        }
        binding.rvActiveDownloads.layoutManager = LinearLayoutManager(context)
        binding.rvActiveDownloads.adapter = activeAdapter
        
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                checkActiveDownloads()
                delay(1000)
            }
        }
    }

    private fun checkActiveDownloads() {
        if (!isAdded) return
        val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_PAUSED)
        val cursor = dm.query(query)
        
        val activeList = mutableListOf<ActiveDownload>()
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)) ?: "Desconocido"
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                
                val statusText = when (status) {
                    DownloadManager.STATUS_PENDING -> "Pendiente..."
                    DownloadManager.STATUS_PAUSED -> "Pausado"
                    else -> "Descargando..."
                }
                
                activeList.add(ActiveDownload(id, title, bytesDownloaded, bytesTotal, statusText))
            }
            cursor.close()
        }
        
        if (activeList.isNotEmpty()) {
            binding.layoutActiveDownloads.visibility = View.VISIBLE
            activeAdapter.updateDownloads(activeList)
            lastActiveCount = activeList.size
        } else {
            binding.layoutActiveDownloads.visibility = View.GONE
            if (lastActiveCount > 0) {
                // Hay descargas que acaban de terminar o cancelarse
                loadDownloads()
                setupStorageStats()
            }
            lastActiveCount = 0
            activeAdapter.updateDownloads(emptyList())
        }
    }

    private fun setupStorageStats() {
        val path = Environment.getExternalStorageDirectory().path
        val stat = android.os.StatFs(path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalSpace = totalBlocks * blockSize
        val availableSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - availableSpace
        
        val totalGb = totalSpace / (1024.0 * 1024 * 1024)
        val usedGb = usedSpace / (1024.0 * 1024 * 1024)
        val availableGb = availableSpace / (1024.0 * 1024 * 1024)
        val pct = if (totalSpace > 0) ((usedSpace.toDouble() / totalSpace.toDouble()) * 100).toInt() else 0
        
        binding.tvStorageUsed.text = String.format(java.util.Locale.US, "%.1f GB used of %.0f GB", usedGb, totalGb)
        binding.tvStoragePercent.text = "$pct% Full"
        binding.tvStorageFree.text = String.format(java.util.Locale.US, "%.1f GB Free", availableGb)
        binding.progressStorage.max = 100
        binding.progressStorage.progress = pct
    }

    private fun loadDownloads() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val spideryDir = File(downloadDir, "SpideryBook")
        
        if (spideryDir.exists()) {
            val allFiles = spideryDir.listFiles()?.filter { it.isFile && it.extension == "mp4" } ?: emptyList()
            
            if (allFiles.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.layoutAnimes.visibility = View.GONE
                binding.layoutMovies.visibility = View.GONE
                return
            }
            
            // Group files logically by breaking off the Episode number specifiers
            val groups = allFiles.groupBy { file ->
                val name = file.nameWithoutExtension
                if (name.contains(" - ")) {
                    name.substringBefore(" - ").trim()
                } else {
                    name
                }
            }.map { (title, files) ->
                val totalSize = files.sumOf { it.length() }
                DownloadGroup(
                    title = title,
                    episodesCount = files.size,
                    totalSizeBytes = totalSize,
                    firstFile = files.first()
                )
            }
            
            val movies = groups.filter { it.episodesCount == 1 }
            val animes = groups.filter { it.episodesCount > 1 }
            
            binding.layoutAnimes.visibility = if (animes.isNotEmpty()) View.VISIBLE else View.GONE
            binding.layoutMovies.visibility = if (movies.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvEmpty.visibility = View.GONE
            
            binding.rvAnimes.layoutManager = LinearLayoutManager(context)
            binding.rvAnimes.adapter = DownloadGroupAdapter(animes, { group -> deleteGroup(group) }, { playFirst(it) })
            
            binding.rvMovies.layoutManager = LinearLayoutManager(context)
            binding.rvMovies.adapter = DownloadGroupAdapter(movies, { group -> deleteGroup(group) }, { playFirst(it) })
            
        } else {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.layoutAnimes.visibility = View.GONE
            binding.layoutMovies.visibility = View.GONE
        }
    }
    
    private fun deleteGroup(group: DownloadGroup) {
         val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
         val spideryDir = File(downloadDir, "SpideryBook")
         spideryDir.listFiles()?.forEach { file ->
             if (file.name.startsWith(group.title)) {
                 file.delete()
             }
         }
         loadDownloads()
         setupStorageStats()
         Toast.makeText(context, "${group.title} Eliminado", Toast.LENGTH_SHORT).show()
    }
    
    private fun playFirst(group: DownloadGroup) {
         val intent = android.content.Intent(requireContext(), com.spiderybook.ui.player.PlayerActivity::class.java).apply {
                putExtra("data", "file://${group.firstFile.absolutePath}")
                putExtra("apiName", "Local")
                putExtra("title", group.firstFile.nameWithoutExtension)
                putExtra("isDirectLink", true)
         }
         startActivity(intent)
    }
}
