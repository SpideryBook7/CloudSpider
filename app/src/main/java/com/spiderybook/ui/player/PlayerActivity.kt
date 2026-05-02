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
    private lateinit var episodeAdapter: PlayerEpisodeAdapter
    private var isEpisodesPanelOpen = false
    
    // Gestures
    private lateinit var audioManager: android.media.AudioManager
    private var maxVolume = 0
    private var currentBrightness = 0.5f
    private var currentVolumeFloat = 0f
    private var isLocked = false
    private lateinit var gestureDetector: android.view.GestureDetector
    private var isScrolling = false
    
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
        
        audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        setupGestures()
        setupEpisodesPanel()
        
        // Initialize Control Context
        val localShowName = showName
        val safeShowName = localShowName ?: title
        val parsedSubtitle = if (localShowName != null && title.contains(localShowName, ignoreCase = true)) {
            title.replace(localShowName, "", ignoreCase = true).removePrefix(" - ").removePrefix("- ").trim()
        } else {
            title
        }
        
        binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_player_title)?.text = safeShowName
        binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_player_subtitle)?.text = parsedSubtitle.ifEmpty { "Playing" }        
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
                            playbackPosition = savedPosition,
                            showTitle = showName ?: "",
                            showUrl = intent.getStringExtra("showUrl") ?: ""
                        )
                    )
                }
            
            if (intent.getBooleanExtra("isDirectLink", false)) {
                // Local offline file: skip extraction, jump straight to player
                binding.progressBar.visibility = View.GONE
                initializePlayer(data, "")
                setupPlayerControls(emptyList())
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
                val btnSources = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_sources_control)
                
                if (btnSources != null) {
                    // Show button only if multiple links exist
                    btnSources.visibility = if (links.size > 1) View.VISIBLE else View.GONE
                    
                    // Set click listener to reopen the dialog
                    btnSources.setOnClickListener {
                        showSourceSelectionDialog(links)
                    }
                }
                
                // Fullscreen (Rotation) Logic
                val btnFullscreen = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_fullscreen)
                btnFullscreen?.setOnClickListener {
                    requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
                
                // Back Logic
                val btnBack = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_back)
                btnBack?.setOnClickListener { finish() }
                
                // Lock Logic
                val btnLock = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_lock)
                val tvLock = binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_lock)
                val topBar = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.layout_top_bar)
                val btnEpisodes = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_episodes)
                btnEpisodes?.setOnClickListener { showEpisodesPanel() }
                
                val btnSpeed = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_speed)
                val exoProgress = binding.playerView.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)
                val btnNextEpisode = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_next_episode)
                
                btnLock?.setOnClickListener {
                    isLocked = !isLocked
                    if (isLocked) {
                        btnLock.setColorFilter(android.graphics.Color.parseColor("#B366FF")) // Purple
                        tvLock?.setTextColor(android.graphics.Color.parseColor("#B366FF"))
                        Toast.makeText(this@PlayerActivity, "Pantalla Bloqueada", Toast.LENGTH_SHORT).show()
                        
                        // Hide everything except the lock button so it natively auto-fades with ExoPlayer
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_play_custom)?.visibility = View.INVISIBLE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_pause_custom)?.visibility = View.INVISIBLE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_rewind_custom)?.visibility = View.INVISIBLE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_forward_custom)?.visibility = View.INVISIBLE
                        topBar?.visibility = View.INVISIBLE
                        btnEpisodes?.visibility = View.INVISIBLE
                        btnSpeed?.visibility = View.INVISIBLE
                        exoProgress?.visibility = View.INVISIBLE
                        btnNextEpisode?.visibility = View.INVISIBLE
                        
                    } else {
                        btnLock.setColorFilter(android.graphics.Color.parseColor("#808080")) // Grey
                        tvLock?.setTextColor(android.graphics.Color.parseColor("#808080"))
                        Toast.makeText(this@PlayerActivity, "Pantalla Desbloqueada", Toast.LENGTH_SHORT).show()
                        
                        // Show everything
                        val isPlaying = player?.isPlaying == true
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_play_custom)?.visibility = if(!isPlaying) View.VISIBLE else View.GONE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_pause_custom)?.visibility = if(isPlaying) View.VISIBLE else View.GONE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_rewind_custom)?.visibility = View.VISIBLE
                        binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_forward_custom)?.visibility = View.VISIBLE
                        topBar?.visibility = View.VISIBLE
                        btnEpisodes?.visibility = View.VISIBLE
                        btnSpeed?.visibility = View.VISIBLE
                        exoProgress?.visibility = View.VISIBLE
                        // Note: btnNextEpisode logic is handled automatically elsewhere
                    }
                }
                
                // Aspect Ratio Logic restored to the unified UI
                val btnAspectRatio = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_aspect_ratio)
                btnAspectRatio?.setOnClickListener {
                    val currentMode = binding.playerView.resizeMode
                    val newMode = if (currentMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                        Toast.makeText(this@PlayerActivity, "Original (Fit)", Toast.LENGTH_SHORT).show()
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    } else {
                        Toast.makeText(this@PlayerActivity, "Stretch to Fill", Toast.LENGTH_SHORT).show()
                        androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    binding.playerView.resizeMode = newMode
                }
                // Floating Next Episode Logic
                val btnNextFloating = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_next_episode)
                btnNextFloating?.setOnClickListener {
                    playNextEpisode()
                }
                
                // Skip Intro Logic (85 seconds scrub)
                val btnSkipIntro = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_skip_intro)
                btnSkipIntro?.setOnClickListener {
                    player?.let { p ->
                        val newPosition = p.currentPosition + 85_000L
                        p.seekTo(newPosition.coerceAtMost(p.duration))
                        btnSkipIntro.visibility = View.GONE
                    }
                }
                
                // Track Selection / Quality Dialog Logic
                val btnQuality = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_quality_settings)
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
                val btnCast = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_cast_control)
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
                     val bestLink = links.sortedByDescending { it.quality }.firstOrNull { it.url.contains(".mp4") && !it.isM3u8 } ?: links.sortedByDescending { it.quality }.firstOrNull { !it.isM3u8 } ?: links.sortedByDescending { it.quality }.first()
                     
                     if (!bestLink.isM3u8 && !bestLink.url.contains(".mp4") && !bestLink.url.contains("terabox")) {
                         Toast.makeText(this@PlayerActivity, "Desencriptando enlace pesado...", Toast.LENGTH_SHORT).show()
                         lifecycleScope.launch {
                             var resolvedM3u8: String? = null
                             val html = com.spiderybook.util.WebViewResolver.resolveCloudflareHtml(this@PlayerActivity, bestLink.url, bestLink.referer)
                             if (html != null && html.contains("eval(function")) {
                                 val dummyApi = object : com.spiderybook.plugins.MainAPI() {
                                     override val name = "Dummy"
                                     override val mainUrl = ""
                                 }
                                 val extractor = com.spiderybook.plugins.extractors.VidhideExtractor(dummyApi)
                                 val extractedLinks = extractor.extract(bestLink.url, overrideHtml = html)
                                 if (extractedLinks.isNotEmpty()) {
                                     resolvedM3u8 = extractedLinks.first().url
                                 }
                             }

                             if (resolvedM3u8 == null) {
                                  resolvedM3u8 = com.spiderybook.util.WebViewResolver.interceptVideoUrl(this@PlayerActivity, bestLink.url, bestLink.referer)
                             }

                             if (resolvedM3u8 != null) {
                                 initializePlayer(resolvedM3u8, bestLink.referer)
                             } else {
                                 Toast.makeText(this@PlayerActivity, "Error al desencriptar, enviando al navegador...", Toast.LENGTH_SHORT).show()
                                 val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(bestLink.url))
                                 startActivity(intent)
                                 finish()
                             }
                         }
                     } else {
                         initializePlayer(bestLink.url, bestLink.referer)
                     }
                } else {
                    Toast.makeText(this, "No links found", Toast.LENGTH_SHORT).show()
                }
                setupPlayerControls(links)
            } else if (resource is Resource.Error) {
                 Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Wire up all player UI controls. Safe to call with an empty list for local-file playback. */
    private fun setupPlayerControls(links: List<com.spiderybook.plugins.MainAPI.ExtractorLink>) {
        // Sources button — only shown when multiple streams available
        val btnSources = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_sources_control)
        if (btnSources != null) {
            btnSources.visibility = if (links.size > 1) View.VISIBLE else View.GONE
            btnSources.setOnClickListener { showSourceSelectionDialog(links) }
        }

        // Fullscreen / Rotation
        val btnFullscreen = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_fullscreen)
        btnFullscreen?.setOnClickListener {
            requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        // Back
        val btnBack = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_back)
        btnBack?.setOnClickListener { finish() }

        // Lock
        val btnLock     = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_lock)
        val tvLock      = binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_lock)
        val topBar      = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.layout_top_bar)
        val btnEpisodes = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_episodes)
        val btnSpeed    = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_speed)
        val exoProgress = binding.playerView.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)
        val btnNextEpisode = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_next_episode)

        btnEpisodes?.setOnClickListener { showEpisodesPanel() }

        btnLock?.setOnClickListener {
            isLocked = !isLocked
            if (isLocked) {
                btnLock.setColorFilter(android.graphics.Color.parseColor("#B366FF"))
                tvLock?.setTextColor(android.graphics.Color.parseColor("#B366FF"))
                Toast.makeText(this@PlayerActivity, "Pantalla Bloqueada", Toast.LENGTH_SHORT).show()
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_play_custom)?.visibility   = View.INVISIBLE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_pause_custom)?.visibility  = View.INVISIBLE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_rewind_custom)?.visibility = View.INVISIBLE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_forward_custom)?.visibility= View.INVISIBLE
                topBar?.visibility      = View.INVISIBLE
                btnEpisodes?.visibility = View.INVISIBLE
                btnSpeed?.visibility    = View.INVISIBLE
                exoProgress?.visibility = View.INVISIBLE
                btnNextEpisode?.visibility = View.INVISIBLE
            } else {
                btnLock.setColorFilter(android.graphics.Color.parseColor("#808080"))
                tvLock?.setTextColor(android.graphics.Color.parseColor("#808080"))
                Toast.makeText(this@PlayerActivity, "Pantalla Desbloqueada", Toast.LENGTH_SHORT).show()
                val isPlaying = player?.isPlaying == true
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_play_custom)?.visibility   = if (!isPlaying) View.VISIBLE else View.GONE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_pause_custom)?.visibility  = if (isPlaying) View.VISIBLE else View.GONE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_rewind_custom)?.visibility = View.VISIBLE
                binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_forward_custom)?.visibility= View.VISIBLE
                topBar?.visibility      = View.VISIBLE
                btnEpisodes?.visibility = View.VISIBLE
                btnSpeed?.visibility    = View.VISIBLE
                exoProgress?.visibility = View.VISIBLE
            }
        }

        // Aspect Ratio
        val btnAspectRatio = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_aspect_ratio)
        btnAspectRatio?.setOnClickListener {
            val newMode = if (binding.playerView.resizeMode == androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                Toast.makeText(this@PlayerActivity, "Original (Fit)", Toast.LENGTH_SHORT).show()
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            } else {
                Toast.makeText(this@PlayerActivity, "Stretch to Fill", Toast.LENGTH_SHORT).show()
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
            binding.playerView.resizeMode = newMode
        }

        // Next Episode
        binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_next_episode)
            ?.setOnClickListener { playNextEpisode() }

        // Skip Intro
        val btnSkipIntro = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_skip_intro)
        btnSkipIntro?.setOnClickListener {
            player?.let { p ->
                p.seekTo((p.currentPosition + 85_000L).coerceAtMost(p.duration))
                btnSkipIntro.visibility = View.GONE
            }
        }

        // Quality / Track Selection
        val btnQuality = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_quality_settings)
        btnQuality?.setOnClickListener {
            player?.let { exoPlayer ->
                androidx.media3.ui.TrackSelectionDialogBuilder(
                    this@PlayerActivity,
                    "Quality Options",
                    exoPlayer,
                    androidx.media3.common.C.TRACK_TYPE_VIDEO
                ).setTheme(com.spiderybook.R.style.Theme_SpiderStream_Dialog)
                    .build().show()
            }
        }

        // Cast to TV
        val btnCast = binding.playerView.findViewById<android.widget.ImageView>(com.spiderybook.R.id.btn_cast_control)
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
                .setCancelable(false).show()
            lifecycleScope.launch {
                val devices = dlnaManager.discoverDevices(3000)
                dialog.dismiss()
                if (devices.isEmpty()) {
                    Toast.makeText(this@PlayerActivity, "No se encontraron TVs en la red", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val names = devices.map { it.name }.toTypedArray()
                androidx.appcompat.app.AlertDialog.Builder(this@PlayerActivity)
                    .setTitle("Proyectar en:")
                    .setItems(names) { _, which ->
                        lifecycleScope.launch {
                            val success = dlnaManager.playMedia(devices[which], mediaUrl, mediaReferer)
                            if (success) {
                                Toast.makeText(this@PlayerActivity, "Reproduciendo en la TV", Toast.LENGTH_LONG).show()
                                player?.pause()
                            } else {
                                Toast.makeText(this@PlayerActivity, "Error al enviar el video a la TV", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.setCancelable(true).show()
            }
        }
    } // end setupPlayerControls

    private fun showSourceSelectionDialog(links: List<com.spiderybook.plugins.MainAPI.ExtractorLink>) {
        val names = links.map { it.name }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Source")
            .setItems(names) { _, which ->
                val link = links[which]
                if (!link.isM3u8 && !link.url.contains(".mp4") && !link.url.contains("terabox")) {
                    Toast.makeText(this@PlayerActivity, "Desencriptando enlace pesado...", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        var resolvedM3u8: String? = null
                        val html = com.spiderybook.util.WebViewResolver.resolveCloudflareHtml(this@PlayerActivity, link.url, link.referer)
                        if (html != null && html.contains("eval(function")) {
                            val dummyApi = object : com.spiderybook.plugins.MainAPI() {
                                override val name = "Dummy"
                                override val mainUrl = ""
                            }
                            val extractor = com.spiderybook.plugins.extractors.VidhideExtractor(dummyApi)
                            val extractedLinks = extractor.extract(link.url, overrideHtml = html)
                            if (extractedLinks.isNotEmpty()) {
                                resolvedM3u8 = extractedLinks.first().url
                            }
                        }

                        if (resolvedM3u8 == null) {
                            resolvedM3u8 = com.spiderybook.util.WebViewResolver.interceptVideoUrl(this@PlayerActivity, link.url, link.referer)
                        }

                        if (resolvedM3u8 != null) {
                            initializePlayer(resolvedM3u8, link.referer)
                        } else {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link.url))
                            startActivity(intent)
                        }
                    }
                } else {
                    initializePlayer(link.url, link.referer)
                }
            }
            .setCancelable(true)
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
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val dataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
        
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

        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(this)
            .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            
        // Enhanced Buffering for Legacy/Slow Devices
        val loadControl = if (com.spiderybook.BuildConfig.FLAVOR == "legacy") {
            androidx.media3.exoplayer.DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    50000,  // Min buffer audio/video (50s)
                    100000, // Max buffer (100s)
                    2500,   // Buffer for playback to start (2.5s)
                    5000    // Buffer for playback to resume (5s)
                ).build()
        } else {
            androidx.media3.exoplayer.DefaultLoadControl()
        }
            
        val trackSelector = androidx.media3.exoplayer.trackselection.DefaultTrackSelector(this)
        trackSelector.parameters = trackSelector.buildUponParameters()
            .setForceHighestSupportedBitrate(true)
            .build()
            
        player = ExoPlayer.Builder(this, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .build()
        
        // Log track info for diagnosis
        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                var audioCount = 0; var videoCount = 0
                for (group in tracks.groups) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        if (format.sampleMimeType?.startsWith("audio") == true) { audioCount++ }
                        if (format.sampleMimeType?.startsWith("video") == true) { videoCount++ }
                        android.util.Log.d("SpideryDebug", "Track[$i] mime=${format.sampleMimeType} selected=${group.isTrackSelected(i)}")
                    }
                }
                android.util.Log.d("SpideryDebug", "Total tracks: video=$videoCount audio=$audioCount")
            }
        })
        
        binding.playerView.player = player
        
        // Manual Play/Pause & Seek Logic
        val btnPlay = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_play_custom)
        val btnPause = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_pause_custom)
        val btnRew = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_rewind_custom)
        val btnFfw = binding.playerView.findViewById<View>(com.spiderybook.R.id.btn_forward_custom)
        
        btnPlay?.setOnClickListener { player?.play() }
        btnPause?.setOnClickListener { player?.pause() }
        
        btnRew?.setOnClickListener { 
            player?.let { p ->
                p.seekTo((p.currentPosition - 10_000L).coerceAtLeast(0L))
            }
        }
        
        btnFfw?.setOnClickListener { 
            player?.let { p ->
                p.seekTo((p.currentPosition + 10_000L).coerceAtMost(p.duration))
            }
        }

        // Add listener for state changes
        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isLocked) return // Prevent Play logic from overwriting INVISIBLE locks
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
                    
                    // AutoPlay Next Episode
                    if (episodeUrls != null && currentIndex - 1 >= 0) {
                         Toast.makeText(this@PlayerActivity, "Siguiente episodio en 3 segundos...", Toast.LENGTH_SHORT).show()
                         lifecycleScope.launch {
                             delay(3000L)
                             // Re-check state to ensure user didn't leave or explicitly pause
                             if (player?.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                                 playNextEpisode()
                             }
                         }
                    } else {
                         playNextEpisode() // Let it default to finish()
                    }
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
            android.util.Log.d("SpideryDebug", "Terabox URL: $url")
            // ProgressiveMediaSource: fuerza descarga progresiva MP4/MKV con todos los tracks
            val mediaSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
        } else if (url.contains(".m3u8") && (
                referer?.contains("streamwish") == true ||
                referer?.contains("embedwish") == true ||
                referer?.contains("filemoon") == true ||
                referer?.contains("hglamioz") == true ||
                referer?.contains("playnixes") == true ||
                referer?.contains("vidhide") == true ||
                referer?.contains("vidhideplus") == true ||
                referer?.contains("vhide") == true
            )) {
            // HLS streams that require Referer on segment requests — use explicit HlsMediaSource
            val mediaSource = getHlsSource(url, referer ?: "", dataSourceFactory)
            player?.setMediaSource(mediaSource)
        } else if (url.startsWith("file://") || url.startsWith("/")) {
            // LOCAL FILE: OkHttpDataSource cannot read file:// URIs.
            // Use DefaultDataSourceFactory which handles file://, content://, and http://.
            val localUri = if (url.startsWith("/")) android.net.Uri.fromFile(java.io.File(url)) else url.toUri()
            val localDataFactory = androidx.media3.datasource.DefaultDataSourceFactory(this, "SpideryBookPlayer")
            val localMediaItem = androidx.media3.common.MediaItem.Builder().setUri(localUri).build()
            val localSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(localDataFactory)
                .createMediaSource(localMediaItem)
            player?.setMediaSource(localSource)
        } else {
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

    override fun onResume() {
        super.onResume()
        // Intencionalmente no llamamos a player?.play() aquí.
        // El usuario prefiere que el video se mantenga pausado al desbloquear el teléfono.
    }

    override fun onStop() {
        super.onStop()
        saveCurrentHistory()
        player?.pause()
    }
    
    private var nextEpisodeJob: Job? = null

    private fun startNextEpisodeTimer() {
        nextEpisodeJob?.cancel()
        nextEpisodeJob = lifecycleScope.launch(Dispatchers.Main) {
            val btnNextFloating = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_next_episode)
            val btnSkipIntro = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_skip_intro)
            
            while (isActive && player != null) {
                val duration = player!!.duration
                val currentPos = player!!.currentPosition
                
                // -- Skip Intro Button Logic --
                // Show during the first 3 minutes (180_000 ms) while controls are visible
                if (btnSkipIntro != null && currentPos < 180_000L && binding.playerView.isControllerFullyVisible) {
                    if (btnSkipIntro.visibility == View.GONE) {
                        btnSkipIntro.visibility = View.VISIBLE
                    }
                } else if (btnSkipIntro != null) {
                    btnSkipIntro.visibility = View.GONE
                }
                
                // -- Next Episode Button Logic --
                if (episodeUrls != null && currentIndex - 1 >= 0 && btnNextFloating != null) {
                    if (duration > 0 && (duration - currentPos) <= 120_000L) { // Fades in 2 Mins before ending
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
                }
                delay(1000) // Check loop every second
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
            playEpisodeAt(currentIndex - 1)
        }
    }

    private fun playEpisodeAt(index: Int) {
        if (episodeUrls != null && index >= 0 && index < episodeUrls!!.size) {
            currentIndex = index
            if (::episodeAdapter.isInitialized) {
                episodeAdapter.updateCurrentIndex(currentIndex)
            }
            
            val nextUrl = episodeUrls!![currentIndex]
            val nextName = episodeNames?.getOrNull(currentIndex) ?: ""
            val nextTitle = if (showName != null) "$showName - $nextName" else nextName
            
            saveCurrentHistory()

            intent.putExtra("data", nextUrl)
            intent.putExtra("title", nextTitle)
            intent.putExtra("currentIndex", currentIndex)

            player?.stop()
            player?.clearMediaItems()
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            val apiName = intent.getStringExtra("apiName") ?: ""
            viewModel.loadLinks(apiName, nextUrl)
            Toast.makeText(this@PlayerActivity, "Reproduciendo: $nextTitle", Toast.LENGTH_SHORT).show()
            
            val btnNextFloating = binding.playerView.findViewById<com.google.android.material.button.MaterialButton>(com.spiderybook.R.id.btn_next_episode)
            btnNextFloating?.visibility = android.view.View.GONE
            
            val localShowName = showName
            val safeShowName = localShowName ?: nextTitle
            val parsedSubtitle = if (localShowName != null && nextTitle.contains(localShowName, ignoreCase = true)) {
                nextTitle.replace(localShowName, "", ignoreCase = true).removePrefix(" - ").removePrefix("- ").trim()
            } else {
                nextTitle
            }
            binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_player_title)?.text = safeShowName
            binding.playerView.findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_player_subtitle)?.text = parsedSubtitle.ifEmpty { "Playing" }
        }
    }

    private fun setupEpisodesPanel() {
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(com.spiderybook.R.id.rv_player_episodes)
        val tvTitle = findViewById<android.widget.TextView>(com.spiderybook.R.id.tv_episodes_panel_title)
        
        tvTitle?.text = (currentMediaTitle?.take(30) ?: "") + (if ((currentMediaTitle?.length ?: 0) > 30) "..." else "")
        
        val eNames = episodeNames ?: emptyList()
        val pUrl = intent.getStringExtra("poster") ?: ""
        episodeAdapter = PlayerEpisodeAdapter(eNames, pUrl, currentIndex) { clickedIndex ->
            playEpisodeAt(clickedIndex)
            hideEpisodesPanel()
        }
        
        rv?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rv?.adapter = episodeAdapter
    }
    
    private fun showEpisodesPanel() {
        val panel = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(com.spiderybook.R.id.layout_episodes_panel)
        panel?.visibility = View.VISIBLE
        panel?.translationX = 320f * resources.displayMetrics.density
        panel?.animate()?.translationX(0f)?.setDuration(250)?.start()
        isEpisodesPanelOpen = true
        
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(com.spiderybook.R.id.rv_player_episodes)
        rv?.scrollToPosition(currentIndex.coerceAtLeast(0))
    }
    
    private fun hideEpisodesPanel() {
        val panel = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(com.spiderybook.R.id.layout_episodes_panel)
        panel?.animate()?.translationX(panel.width.toFloat())?.setDuration(250)?.withEndAction {
            panel.visibility = View.GONE
            isEpisodesPanelOpen = false
        }?.start()
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
        
        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        
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

    /**
     * Generic HLS source factory for any server requiring Referer on segment requests.
     * This ensures ExoPlayer sends the Referer with EVERY .ts segment request, not just the .m3u8.
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun getHlsSource(url: String, referer: String, dataSourceFactory: androidx.media3.datasource.okhttp.OkHttpDataSource.Factory): androidx.media3.exoplayer.source.MediaSource {
        val headers = mutableMapOf<String, String>()
        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        if (referer.isNotEmpty()) {
            headers["Referer"] = referer
        }
        dataSourceFactory.setDefaultRequestProperties(headers)

        val mediaItem = androidx.media3.common.MediaItem.Builder()
            .setUri(url)
            .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
            .build()

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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupGestures() {
        val layoutBrightness = binding.layoutBrightness
        val layoutVolume = binding.layoutVolume
        val progressBrightness = binding.progressBrightness
        val progressVolume = binding.progressVolume
        
        var scrollType = 0 // 1: Brightness, 2: Volume
        
        val gestureListener = object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: android.view.MotionEvent): Boolean {
                if (isLocked) return true
                if (!isScrolling) {
                    currentVolumeFloat = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat()
                }
                isScrolling = false
                scrollType = 0
                return true
            }

            override fun onScroll(
                 e1: android.view.MotionEvent?,
                 e2: android.view.MotionEvent,
                 distanceX: Float,
                 distanceY: Float
             ): Boolean {
                 if (isLocked || e1 == null) return false

                 val deltaY = e1.y - e2.y
                 val deltaX = e1.x - e2.x

                 if (kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                     // Vertical scroll
                     if (!isScrolling) {
                         isScrolling = true
                         val screenWidth = resources.displayMetrics.widthPixels
                         if (e1.x < screenWidth / 2) {
                             scrollType = 1 // Brightness (Left half)
                             layoutBrightness.visibility = View.VISIBLE
                             layoutVolume.visibility = View.GONE
                             
                             // Initial brightness
                             var lparams = window.attributes
                             if (lparams.screenBrightness < 0) {
                                 // System brightness
                                 val resolver = contentResolver
                                 try {
                                     val bright = android.provider.Settings.System.getInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
                                     currentBrightness = bright / 255f
                                 } catch (e: Exception) {
                                     currentBrightness = 0.5f
                                 }
                             } else {
                                 currentBrightness = lparams.screenBrightness
                             }
                             progressBrightness.progress = (currentBrightness * 100).toInt()
                             
                         } else {
                             scrollType = 2 // Volume (Right half)
                             layoutVolume.visibility = View.VISIBLE
                             layoutBrightness.visibility = View.GONE
                             
                             val currVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                             progressVolume.progress = ((currVol.toFloat() / maxVolume) * 100).toInt()
                         }
                     }
                     
                     // Handle scroll movement - Use distanceY (frame delta) not deltaY (absolute delta)
                     val height = resources.displayMetrics.heightPixels
                     if (scrollType == 1) { // Brightness
                         val step = (distanceY / height) * 1.5f 
                         currentBrightness += step
                         currentBrightness = currentBrightness.coerceIn(0f, 1f)
                         
                         val lparams = window.attributes
                         lparams.screenBrightness = currentBrightness
                         window.attributes = lparams
                         
                         progressBrightness.progress = (currentBrightness * 100).toInt()
                         
                     } else if (scrollType == 2) { // Volume
                         val step = (distanceY / height) * maxVolume * 1.5f 
                         currentVolumeFloat += step
                         val newVol = currentVolumeFloat.toInt().coerceIn(0, maxVolume)
                         
                         audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
                         progressVolume.progress = ((newVol.toFloat() / maxVolume) * 100).toInt()
                         
                         // Update volume icon based on level
                         val imgVolume = binding.imgVolumeIcon
                         if (newVol == 0) {
                             imgVolume.setImageResource(android.R.drawable.ic_lock_silent_mode)
                         } else {
                             imgVolume.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
                         }
                     }
                     return true
                 }
                 return false
             }
             
            override fun onSingleTapConfirmed(e: android.view.MotionEvent): Boolean {
                if (isLocked) {
                    if (!binding.playerView.isControllerFullyVisible) {
                        binding.playerView.showController()
                    } else {
                        binding.playerView.hideController()
                    }
                    return true
                }
                
                // If not locked, let ExoPlayer handle taps naturally. Return false to ignore.
                return false
            }
        }
        
        gestureDetector = android.view.GestureDetector(this, gestureListener)
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (isEpisodesPanelOpen) {
            val panel = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(com.spiderybook.R.id.layout_episodes_panel)
            if (panel != null) {
                val rect = android.graphics.Rect()
                panel.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    if (ev.action == android.view.MotionEvent.ACTION_DOWN) {
                        hideEpisodesPanel()
                    }
                    return true // block touch outside
                } else {
                    // Tap is inside episodes panel. Do NOT process gestureDetector
                    // so we don't accidentally trigger volume sliders.
                    return super.dispatchTouchEvent(ev)
                }
            }
        }
        
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        
        val isUpOrCancel = ev.action == android.view.MotionEvent.ACTION_UP || ev.action == android.view.MotionEvent.ACTION_CANCEL
        
        if (isScrolling) {
            if (isUpOrCancel) {
                // User released finger. Fade out sliders
                lifecycleScope.launch {
                    delay(600) // Keep visible for a split second
                    binding.layoutBrightness.animate().alpha(0f).setDuration(300).withEndAction {
                        binding.layoutBrightness.visibility = View.GONE
                        binding.layoutBrightness.alpha = 1f
                    }.start()
                    binding.layoutVolume.animate().alpha(0f).setDuration(300).withEndAction {
                        binding.layoutVolume.visibility = View.GONE
                        binding.layoutVolume.alpha = 1f
                    }.start()
                }
                isScrolling = false
            }
            return true // Consume all touch events while scrolling to prevent clicks from leaking
        }
        
        if (isLocked) {
             val layoutLock = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.layout_lock)
             val btnLock = binding.playerView.findViewById<android.view.View>(com.spiderybook.R.id.btn_lock)
             
             if (layoutLock != null && binding.playerView.isControllerFullyVisible) {
                 val rect = android.graphics.Rect()
                 btnLock?.getGlobalVisibleRect(rect)
                 if (rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                     return super.dispatchTouchEvent(ev) // Allow clicking unlock
                 }
             }
             return true // block everywhere else
        }

        return super.dispatchTouchEvent(ev)
    }
}
