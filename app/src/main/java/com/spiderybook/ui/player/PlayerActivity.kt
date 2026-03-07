package com.spiderybook.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.spiderybook.databinding.ActivityPlayerBinding
import com.spiderybook.util.Resource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var startPosition: Long = 0L
    
    private lateinit var dlnaManager: com.spiderybook.utils.dlna.DLNAManager
    private var currentMediaUrl: String? = null
    private var currentMediaReferer: String? = null
    private var currentMediaTitle: String? = null
    
    // Episode List support
    private var episodeUrls: java.util.ArrayList<String>? = null
    private var episodeNames: java.util.ArrayList<String>? = null
    private var currentIndex: Int = -1
    private var showName: String? = null
    
    private val viewModel: PlayerViewModel by viewModels()
    
    @javax.inject.Inject
    lateinit var localRepository: com.spiderybook.data.repository.LocalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUi()
        
        val data = intent.getStringExtra("data")
        val apiName = intent.getStringExtra("apiName") ?: "Local"
        val title = intent.getStringExtra("title") ?: "Unknown"
        val poster = intent.getStringExtra("poster") ?: ""
        val type = intent.getStringExtra("type") ?: com.spiderybook.domain.model.TvType.Movie.name
        
        currentMediaTitle = title
        dlnaManager = com.spiderybook.utils.dlna.DLNAManager(this)
        
        episodeUrls = intent.getStringArrayListExtra("episodeUrls")
        episodeNames = intent.getStringArrayListExtra("episodeNames")
        currentIndex = intent.getIntExtra("currentIndex", -1)
        showName = intent.getStringExtra("showName")
        
        if (data != null) {
            // Save/Update History
            lifecycleScope.launch {
                    val existing = localRepository.getHistoryItem(data)
                    var savedPosition = 0L
                    
                    if (existing != null) {
                        savedPosition = existing.playbackPosition
                        val savedDuration = existing.duration
                        
                        // If within 20 seconds of the video's end, assume strictly finished. Reset to 0.
                        if (savedDuration > 0L && (savedDuration - savedPosition) <= 20_000L) {
                            savedPosition = 0L
                        }
                        
                        // Store it in a member variable to use when player initializes
                        startPosition = savedPosition
                        if (savedPosition > 0L) {
                            Toast.makeText(this@PlayerActivity, "Resuming from ${generatedTime(savedPosition)}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    localRepository.insertHistory(
                        com.spiderybook.data.local.entity.HistoryEntity(
                            url = data,
                            name = title,
                            posterUrl = poster,
                            apiName = apiName,
                            type = type,
                            playbackPosition = savedPosition
                        )
                    )
                }
            
            if (intent.getBooleanExtra("isDirectLink", false)) {
                initializePlayer(data, "")
            } else {
                setupObservers()
                viewModel.loadLinks(apiName, data)
            }
        } else {
            Toast.makeText(this, "Error: No data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupObservers() {
        viewModel.links.observe(this) { resource ->
            binding.progressBar.visibility = if (resource is Resource.Loading) View.VISIBLE else View.GONE
            
            if (resource is Resource.Success) {
                val links = resource.data
                
                // Find the integrated button inside the PlayerView controls
                val btnSources = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_sources_control)
                
                if (btnSources != null) {
                    // Show button only if multiple links exist
                    btnSources.visibility = if (links.size > 1) View.VISIBLE else View.GONE
                    
                    // Set click listener to reopen the dialog
                    btnSources.setOnClickListener {
                        showSourceSelectionDialog(links)
                    }
                }
                
                // Fullscreen (Rotation) Logic
                val btnFullscreen = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_fullscreen)
                btnFullscreen?.setOnClickListener {
                    requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                
                // Aspect Ratio Logic (Toggle: Original <-> Stretch)
                val btnAspectRatio = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_aspect_ratio)
                btnAspectRatio?.setOnClickListener {
                    val currentMode = binding.playerView.resizeMode
                    val newMode = if (currentMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                        Toast.makeText(this, "Original (Fit)", Toast.LENGTH_SHORT).show()
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    } else {
                        Toast.makeText(this, "Stretch to Fill", Toast.LENGTH_SHORT).show()
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    binding.playerView.resizeMode = newMode
                }
                
                // Floating Next Episode Logic (Fades in 2 mins before end)
                val btnNextFloating = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_next_episode)
                btnNextFloating?.setOnClickListener {
                    playNextEpisode()
                }
                
                // Track Selection / Quality Dialog Logic
                val btnQuality = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_quality_settings)
                btnQuality?.setOnClickListener {
                    player?.let { exoPlayer ->
                        val trackSelectionDialogBuilder = androidx.media3.ui.TrackSelectionDialogBuilder(
                            this@PlayerActivity,
                            "Quality Options",
                            exoPlayer,
                            androidx.media3.common.C.TRACK_TYPE_VIDEO
                        )
                        trackSelectionDialogBuilder.setTheme(com.spiderybook.R.style.Theme_SpiderStream_Dialog)
                        trackSelectionDialogBuilder.build().show()
                    }
                }
                
                // Cast Logic
                val btnCast = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_cast_control)
                btnCast?.setOnClickListener {
                    val mediaUrl = currentMediaUrl
                    val mediaReferer = currentMediaReferer
                    if (mediaUrl.isNullOrEmpty()) {
                        Toast.makeText(this@PlayerActivity, "No media to cast", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(this@PlayerActivity)
                        .setTitle("Buscando Pantallas...")
                        .setMessage("Escaneando la red Wi-Fi local por TVs compatibles...")
                        .setCancelable(false)
                        .show()
                        
                    lifecycleScope.launch {
                        val devices = dlnaManager.discoverDevices(3000)
                        dialog.dismiss()
                        
                        if (devices.isEmpty()) {
                            Toast.makeText(this@PlayerActivity, "No se encontraron TVs LG/WebOS en la red", Toast.LENGTH_LONG).show()
                            return@launch
                        }
                        
                        val names = devices.map { it.name }.toTypedArray()
                        androidx.appcompat.app.AlertDialog.Builder(this@PlayerActivity)
                            .setTitle("Proyectar en:")
                            .setItems(names) { _, which ->
                                val device = devices[which]
                                lifecycleScope.launch {
                                    Toast.makeText(this@PlayerActivity, "Conectando con ${device.name}...", Toast.LENGTH_SHORT).show()
                                    val success = dlnaManager.playMedia(device, mediaUrl, mediaReferer)
                                    if (success) {
                                        Toast.makeText(this@PlayerActivity, "¡Reproduciendo en la TV! Ya puedes cerrar la aplicación.", Toast.LENGTH_LONG).show()
                                        player?.pause()
                                    } else {
                                        Toast.makeText(this@PlayerActivity, "Error al enviar el video a la TV", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setCancelable(true)
                            .show()
                    }
                }

                if (links.isNotEmpty()) {
                     // Check if player is already playing to avoid auto-restart
                     if (player == null) {
                        // Automatically play the first available link for all providers
                        // The user can still switch sources manually via the 'Sources' button on the player controls
                        val link = links.first()
                        initializePlayer(link.url, link.referer)
                     }
                } else {
                    Toast.makeText(this, "No links found", Toast.LENGTH_SHORT).show()
                }
            } else if (resource is Resource.Error) {
                 Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSourceSelectionDialog(links: List<com.spiderybook.plugins.MainAPI.ExtractorLink>) {
        val names = links.map { it.name }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Source")
            .setItems(names) { _, which ->
                val link = links[which]
                initializePlayer(link.url, link.referer)
            }
            .setCancelable(true)
            .setOnCancelListener { finish() } // Finish if user cancels selection
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String, referer: String?) {
        // Save URL for casting
        currentMediaUrl = url
        currentMediaReferer = referer
        
        // Release existing player to prevent audio overlap
        releasePlayer()

        // 1. Use OkHttp for robust header persistence across HTTP -> HTTPS redirects
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val dataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        
        val headers = mutableMapOf<String, String>()
        if (!referer.isNullOrEmpty()) {
            headers["Referer"] = referer
        }

        // Terabox Anti-Leech Headers for Native Mode
        val alistUrl = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
        if (url.startsWith(alistUrl) || url.contains("terabox")) {
            headers["Referer"] = "https://www.terabox.com/"
            headers["Origin"] = "https://www.terabox.com/"
        }

        if (headers.isNotEmpty()) {
            dataSourceFactory.setDefaultRequestProperties(headers)
        }

        player = ExoPlayer.Builder(this).build()
        
        binding.playerView.player = player
        
        // Manual Play/Pause Logic
        val btnPlay = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_play_custom)
        val btnPause = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_pause_custom)
        
        btnPlay?.setOnClickListener { player?.play() }
        btnPause?.setOnClickListener { player?.pause() }

        // Add listener for state changes
        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    btnPlay?.visibility = View.GONE
                    btnPause?.visibility = View.VISIBLE
                } else {
                    btnPlay?.visibility = View.VISIBLE
                    btnPause?.visibility = View.GONE
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == androidx.media3.common.Player.STATE_READY) {
                    startNextEpisodeTimer()
                }
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    btnPlay?.visibility = View.VISIBLE
                    btnPause?.visibility = View.GONE
                    playNextEpisode()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(this@PlayerActivity, "Playback Error: ${error.message}", Toast.LENGTH_LONG).show()
                error.printStackTrace()
            }
        })
        
        player?.addAnalyticsListener(object : androidx.media3.exoplayer.analytics.AnalyticsListener {
            override fun onPlayerError(eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime, error: androidx.media3.common.PlaybackException) {
                val cause = error.cause
                if (cause is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
                    android.util.Log.e("SpideryDebug", "Error HTTP: ${cause.responseCode}")
                    android.util.Log.e("SpideryDebug", "Headers del servidor: ${cause.headerFields}")
                } else {
                    android.util.Log.e("SpideryDebug", "Error de reproducción: ${error.message}")
                }
            }
        })
        
        // Removed redundant onPlaybackStateChanged checking for STATE_ENDED
        
        val mediaItemBuilder = androidx.media3.common.MediaItem.Builder().setUri(url.toUri())
        if (url.contains(".m3u8")) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
        }
        val mediaItem = mediaItemBuilder.build()
        
        val alistUrlFallback = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
        if ((alistUrlFallback.isNotEmpty() && url.startsWith(alistUrlFallback)) || url.contains("terabox")) {
            // Apply strict ProgressiveMediaSource as requested to bypass anti-leech detection issues
            val mediaSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
           // STREAMWISH (requires specific headers and referer)
        } else if (url.contains(".m3u8") && (referer?.contains("streamwish") == true || referer?.contains("filemoon") == true || referer?.contains("hglamioz") == true || referer?.contains("playnixes") == true)) {
            val mediaSource = getStreamwishSource(url, referer ?: "", dataSourceFactory)
            player?.setMediaSource(mediaSource)
        } else {
            // Fallback for generic streams
            // Thanks to forced MimeTypes.APPLICATION_M3U8 above, DefaultMediaSourceFactory will reliably pick HlsMediaSource
            val mediaSource = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
                .createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
        }
        
        // DEBUG: Show the URL being played
        Toast.makeText(this, "Playing: $url", Toast.LENGTH_LONG).show()
        android.util.Log.d("PlayerActivity", "Initializing player with URL: $url and Referer: $referer")

        if (startPosition > 0L) {
             player?.seekTo(startPosition)
        }
        player?.prepare()
        player?.play()
    }

    override fun onStop() {
        super.onStop()
        saveCurrentHistory()
        releasePlayer()
    }
    
    private var nextEpisodeJob: Job? = null

    private fun startNextEpisodeTimer() {
        nextEpisodeJob?.cancel()
        nextEpisodeJob = lifecycleScope.launch(Dispatchers.Main) {
            val btnNextFloating = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_next_episode)
            
            // Only prepare timer if there IS a next episode available
            if (episodeUrls != null && currentIndex - 1 >= 0 && btnNextFloating != null) {
                while (isActive && player != null) {
                    val duration = player!!.duration
                    val currentPos = player!!.currentPosition
                    
                    if (duration > 0 && (duration - currentPos) <= 120_000L) { // 2 Minutes
                        if (btnNextFloating.visibility == View.GONE) {
                            btnNextFloating.alpha = 0f
                            btnNextFloating.visibility = View.VISIBLE
                            btnNextFloating.animate().alpha(1f).setDuration(500).start()
                        }
                    } else {
                         if (btnNextFloating.visibility == View.VISIBLE) {
                             btnNextFloating.animate().alpha(0f).setDuration(500).withEndAction {
                                 btnNextFloating.visibility = View.GONE
                             }.start()
                         }
                    }
                    delay(1000) // Check every second
                }
            }
        }
    }

    private fun saveCurrentHistory() {
        if (player != null && player!!.playbackState != androidx.media3.common.Player.STATE_IDLE) {
            val currentPos = player!!.currentPosition
            var totalDuration = player!!.duration
            if (totalDuration == androidx.media3.common.C.TIME_UNSET) {
                totalDuration = 0L
            }
            val data = intent.getStringExtra("data")
            val apiName = intent.getStringExtra("apiName")
            val title = intent.getStringExtra("title")
            val poster = intent.getStringExtra("poster")
            val type = intent.getStringExtra("type")

            if (data != null && title != null && poster != null && apiName != null) {
                lifecycleScope.launch {
                    localRepository.insertHistory(
                        com.spiderybook.data.local.entity.HistoryEntity(
                            url = data,
                            name = title,
                            posterUrl = poster,
                            apiName = apiName,
                            type = type,
                            playbackPosition = currentPos,
                            duration = totalDuration,
                            showTitle = showName ?: ""
                        )
                    )
                }
            }
        }
    }

    private fun playNextEpisode() {
        if (episodeUrls != null && currentIndex - 1 >= 0) {
            currentIndex--
            val nextUrl = episodeUrls!![currentIndex]
            val nextName = episodeNames?.getOrNull(currentIndex) ?: ""
            val nextTitle = if (showName != null) "$showName - $nextName" else nextName
            
            saveCurrentHistory()

            intent.putExtra("data", nextUrl)
            intent.putExtra("title", nextTitle)

            player?.stop()
            player?.clearMediaItems()
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            val apiName = intent.getStringExtra("apiName") ?: ""
            viewModel.loadLinks(apiName, nextUrl)
            Toast.makeText(this@PlayerActivity, "Siguiente: $nextTitle", Toast.LENGTH_SHORT).show()
            
            val btnNextFloating = binding.playerView.findViewById<android.widget.ImageButton>(com.spiderybook.R.id.btn_next_episode)
            btnNextFloating?.visibility = android.view.View.GONE // Reset visibility for new episode
        }
    }

    private fun releasePlayer() {
        nextEpisodeJob?.cancel()
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

    private fun generatedTime(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", minutes, remainingSeconds)
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun getStreamwishSource(url: String, referer: String, dataSourceFactory: androidx.media3.datasource.okhttp.OkHttpDataSource.Factory): androidx.media3.exoplayer.source.MediaSource {
        val headers = mutableMapOf<String, String>()
        
        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
        
        // Use the dynamically extracted Referer from StreamwishExtractor
        if (referer.isNotEmpty()) {
            headers["Referer"] = referer
        }
        
        // DO NOT set Origin header, as Streamwish CDNs (premilkyway.com) often return 403 when it's present for direct M3U8 requests

        headers["Accept-Language"] = "es-MX,es;q=0.9,en;q=0.8"

        dataSourceFactory.setDefaultRequestProperties(headers)

        val mediaItem = androidx.media3.common.MediaItem.Builder()
            .setUri(url)
            .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8) // Streamwish suele usar HLS (.m3u8)
            .build()

        // Usamos HlsMediaSource porque Streamwish fragmenta el video para evitar descargas
        return androidx.media3.exoplayer.hls.HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        if (::dlnaManager.isInitialized) {
            dlnaManager.stop()
        }
    }
}
