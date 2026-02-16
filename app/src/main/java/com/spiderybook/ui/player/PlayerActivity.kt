package com.spiderybook.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.spiderybook.databinding.ActivityPlayerBinding
import com.spiderybook.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUi()
        
        val data = intent.getStringExtra("data")
        val apiName = intent.getStringExtra("apiName")
        
        if (data != null && apiName != null) {
            setupObservers()
            viewModel.loadLinks(apiName, data)
        } else {
            Toast.makeText(this, "Error: No data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.links.observe(this) { resource ->
            binding.progressBar.visibility = if (resource is Resource.Loading) View.VISIBLE else View.GONE
            
            if (resource is Resource.Success) {
                val link = resource.data.firstOrNull()
                if (link != null) {
                     initializePlayer(link.url, link.referer)
                } else {
                    Toast.makeText(this, "No links found", Toast.LENGTH_SHORT).show()
                }
            } else if (resource is Resource.Error) {
                 Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String, referer: String?) {
        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
        
        if (!referer.isNullOrEmpty()) {
            val headers = mutableMapOf<String, String>()
            headers["Referer"] = referer
            dataSourceFactory.setDefaultRequestProperties(headers)
        }

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
            )
            .build()
        
        binding.playerView.player = player
        
        // Add error listener for debugging
        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(this@PlayerActivity, "Playback Error: ${error.message}", Toast.LENGTH_LONG).show()
                error.printStackTrace()
            }
        })
        
        
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player?.setMediaItem(mediaItem)
        
        // DEBUG: Show the URL being played
        Toast.makeText(this, "Playing: $url", Toast.LENGTH_LONG).show()
        android.util.Log.d("PlayerActivity", "Initializing player with URL: $url and Referer: $referer")

        player?.prepare()
        player?.play()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
