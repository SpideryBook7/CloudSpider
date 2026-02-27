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
        
        if (data != null) {
            // Save/Update History
            lifecycleScope.launch {
                    val existing = localRepository.getHistoryItem(data)
                    var savedPosition = 0L
                    
                    if (existing != null) {
                        savedPosition = existing.playbackPosition
                        // Store it in a member variable to use when player initializes
                        startPosition = savedPosition
                        Toast.makeText(this@PlayerActivity, "Resuming from ${generatedTime(savedPosition)}", Toast.LENGTH_SHORT).show()
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
                        // REVERTED BEHAVIOR FOR ANIME:
                        // Only show the selection dialog for "PelisPlus" (as requested).
                        // For AnimeFLV and others, Auto-Play the first link (automatic behavior).

                        // Actually, we have apiName in the activity scope from Intent
                        val currentApiName = intent.getStringExtra("apiName")
                        
                        if (links.size > 1 && currentApiName == "PelisPlus") {
                            showSourceSelectionDialog(links)
                        } else {
                            val link = links.first()
                            initializePlayer(link.url, link.referer)
                        }
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
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    btnPlay?.visibility = View.VISIBLE
                    btnPause?.visibility = View.GONE
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
        
        val mediaItem = MediaItem.fromUri(url.toUri())
        
        val alistUrlFallback = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
        if (url.startsWith(alistUrlFallback) || url.contains("terabox")) {
            // Apply strict ProgressiveMediaSource as requested to bypass anti-leech detection issues
            val mediaSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
        } else if (url.contains("streamwish")) {
            // Streamwish Strict HLS Extraction
            val mediaSource = getStreamwishSource(url, dataSourceFactory)
            player?.setMediaSource(mediaSource)
        } else {
            // Fallback for HLS streams (AnimeFLV, etc.)
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
                            duration = totalDuration
                        )
                    )
                }
            }
        }
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
    private fun getStreamwishSource(url: String, dataSourceFactory: androidx.media3.datasource.okhttp.OkHttpDataSource.Factory): androidx.media3.exoplayer.source.MediaSource {
        val headers = mutableMapOf<String, String>()
        
        // El disfraz que confirmamos en Linux
        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
        
        // Streamwish es muy estricto con el origen de la petición
        headers["Referer"] = "https://streamwish.to/"
        headers["Origin"] = "https://streamwish.to/"
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
}
