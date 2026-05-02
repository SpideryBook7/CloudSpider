package com.spiderybook.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object HlsTracker {
    data class Progress(
        val fileName: String,
        val percentage: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val isComplete: Boolean,
        val error: String?
    )
    val activeDownloads = MutableStateFlow<Map<String, Progress>>(emptyMap())
    val cancelledDownloads = mutableSetOf<String>()
}

class HlsDownloadService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Shared client — keep-alive pool reused across all chunk requests for max throughput
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .connectionPool(okhttp3.ConnectionPool(16, 5, TimeUnit.MINUTES))
        .build()

    private val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    // Max simultaneous chunk downloads — any higher risks IP ban from aggressive CDNs
    private val PARALLEL_CHUNKS = 8

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url      = intent?.getStringExtra("url")      ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("fileName")  ?: "download.mp4"
        val referer  = intent.getStringExtra("referer")   ?: ""

        createNotificationChannel()
        startForeground(startId, createNotification(fileName, 0, "Preparando...").build())

        serviceScope.launch {
            try {
                processM3u8(url, fileName, referer, startId)
            } catch (e: Exception) {
                e.printStackTrace()
                updateNotification(startId, fileName, 0, "Error: ${e.message}", true)
                HlsTracker.activeDownloads.value += (fileName to HlsTracker.Progress(fileName, 0, 0L, 0L, true, e.message))
            } finally {
                withContext(Dispatchers.Main) {
                    stopForeground(false)
                    stopSelf(startId)
                }
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun processM3u8(masterUrl: String, fileName: String, referer: String, notificationId: Int) {
        updateNotification(notificationId, fileName, 0, "Analizando M3U8...")
        val content = fetchText(masterUrl, referer) ?: throw Exception("M3U8 vacío")

        val lines = content.lines()
        val tsUrls = mutableListOf<String>()
        var baseUri = URI(masterUrl).resolve(".")

        // ── Master playlist: pick the highest-bandwidth variant ──────────────
        var isMaster = false
        var bestBandwidth = -1
        var bestVariantUrl: String? = null

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                isMaster = true
                val bw = Regex("BANDWIDTH=(\\d+)").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val next = lines.getOrNull(i + 1)?.trim() ?: continue
                if (next.isNotEmpty() && !next.startsWith("#") && bw > bestBandwidth) {
                    bestBandwidth = bw
                    bestVariantUrl = next
                }
            }
        }

        if (isMaster && bestVariantUrl != null) {
            val variantUrl = URI(masterUrl).resolve(bestVariantUrl).toString()
            val subContent = fetchText(variantUrl, referer) ?: throw Exception("Sub-playlist vacía")
            baseUri = URI(variantUrl).resolve(".")
            subContent.lines().forEach { subLine ->
                if (!subLine.startsWith("#") && subLine.isNotBlank())
                    tsUrls.add(baseUri.resolve(subLine).toString())
            }
        } else {
            // Single-level media playlist
            lines.forEach { line ->
                if (!line.startsWith("#") && line.isNotBlank())
                    tsUrls.add(baseUri.resolve(line).toString())
            }
        }

        if (tsUrls.isEmpty()) throw Exception("No se encontraron fragmentos (.ts)")

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val spideryDir = File(downloadDir, "SpideryBook").apply { mkdirs() }
        val outFile = File(spideryDir, fileName.replace(" ", "_"))

        // Pre-allocate temp files per chunk so we can stitch in order
        val tempDir = File(spideryDir, "${outFile.nameWithoutExtension}_tmp").apply { mkdirs() }

        updateNotification(notificationId, fileName, 0, "Descargando 0/${tsUrls.size} segmentos")

        val totalChunks = tsUrls.size
        val completedChunks = AtomicLong(0)
        val downloadedBytes = AtomicLong(0)
        // Rough estimate: first guess based on typical ~200KB / chunk
        val estimatedTotal = AtomicLong(totalChunks * 200_000L)

        // ── Parallel download with Semaphore throttle ────────────────────────
        val semaphore = Semaphore(PARALLEL_CHUNKS)
        val chunkFiles = Array<File?>(totalChunks) { null }
        var cancelled = false

        coroutineScope {
            tsUrls.forEachIndexed { i, tsUrl ->
                if (HlsTracker.cancelledDownloads.contains(fileName)) {
                    cancelled = true
                    return@forEachIndexed
                }
                launch {
                    semaphore.withPermit {
                        if (HlsTracker.cancelledDownloads.contains(fileName)) return@withPermit
                        val tempFile = File(tempDir, "%05d.ts".format(i))
                        try {
                            val bytes = fetchBytes(tsUrl, referer)
                            if (bytes != null) {
                                tempFile.writeBytes(bytes)
                                chunkFiles[i] = tempFile
                                val dl = downloadedBytes.addAndGet(bytes.size.toLong())
                                val done = completedChunks.incrementAndGet()
                                // Refine estimated total progressively
                                estimatedTotal.set((dl / done) * totalChunks)
                                val pct = ((done.toDouble() / totalChunks) * 100).toInt()
                                HlsTracker.activeDownloads.value += (fileName to HlsTracker.Progress(fileName, pct, dl, estimatedTotal.get(), false, null))
                                if (done % 5 == 0L || done == totalChunks.toLong()) {
                                    updateNotification(notificationId, fileName, pct, "$done/$totalChunks segmentos — $pct%")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("HlsService", "Chunk $i failed: ${e.message}")
                        }
                    }
                }
            }
        }

        if (cancelled) {
            tempDir.deleteRecursively()
            HlsTracker.cancelledDownloads.remove(fileName)
            HlsTracker.activeDownloads.value -= fileName
            return
        }

        // ── Stitch chunks in order ───────────────────────────────────────────
        updateNotification(notificationId, fileName, 99, "Procesando video final...")
        outFile.outputStream().buffered(1024 * 1024).use { out ->
            chunkFiles.forEach { chunk ->
                chunk?.let { out.write(it.readBytes()) }
            }
        }
        tempDir.deleteRecursively()

        val finalBytes = outFile.length()
        HlsTracker.activeDownloads.value += (fileName to HlsTracker.Progress(fileName, 100, finalBytes, finalBytes, true, null))
        updateNotification(notificationId, fileName, 100, "Descarga completada \u2014 ${finalBytes / 1_048_576} MB", true)
    }

    // ── Network helpers ──────────────────────────────────────────────────────

    private fun buildRequest(url: String, referer: String) = Request.Builder()
        .url(url)
        .header("User-Agent", UA)
        .header("Referer", referer)
        .header("Accept", "*/*")
        .apply {
            try {
                val origin = URI(referer).let { "${it.scheme}://${it.host}" }
                header("Origin", origin)
            } catch (_: Exception) {}
        }
        .build()

    private fun fetchText(url: String, referer: String): String? {
        return try {
            val res = client.newCall(buildRequest(url, referer)).execute()
            val body = res.body?.string()
            res.close()
            body
        } catch (e: Exception) {
            android.util.Log.w("HlsService", "fetchText failed: $url — ${e.message}")
            null
        }
    }

    private fun fetchBytes(url: String, referer: String): ByteArray? {
        return try {
            val res = client.newCall(buildRequest(url, referer)).execute()
            val bytes = res.body?.bytes()
            res.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    // ── Notifications ────────────────────────────────────────────────────────

    private fun updateNotification(id: Int, title: String, progress: Int, text: String, finished: Boolean = false) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = createNotification(title, progress, text)
        if (finished) {
            builder.setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setOngoing(false)
        }
        nm.notify(id, builder.build())
    }

    private fun createNotification(title: String, progress: Int, text: String = ""): NotificationCompat.Builder =
        NotificationCompat.Builder(this, "HLS_CHANNEL")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, progress, progress == 0 && text.contains("Ana"))
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("HLS_CHANNEL", "Descargas Offline", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
