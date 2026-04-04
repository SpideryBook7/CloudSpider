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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URI

object HlsTracker {
    data class Progress(val fileName: String, val percentage: Int, val downloadedBytes: Long, val totalBytes: Long, val isComplete: Boolean, val error: String?)
    val activeDownloads = MutableStateFlow<Map<String, Progress>>(emptyMap())
    val cancelledDownloads = mutableSetOf<String>()
}

class HlsDownloadService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val client = OkHttpClient.Builder().connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS).build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("fileName") ?: "download.mp4"
        val referer = intent.getStringExtra("referer") ?: ""
        
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

    private fun processM3u8(masterUrl: String, fileName: String, referer: String, notificationId: Int) {
        updateNotification(notificationId, fileName, 0, "Analizando M3U8...")
        val req = Request.Builder().url(masterUrl).header("Referer", referer).build()
        val res = client.newCall(req).execute()
        val content = res.body?.string() ?: throw Exception("M3U8 vacío")
        res.close()

        val lines = content.lines()
        val tsUrls = mutableListOf<String>()
        var baseUri = URI(masterUrl).resolve(".")
        
        var isMaster = false
        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                isMaster = true
                val subUrl = lines.getOrNull(i + 1) ?: ""
                val resolvedSub = URI(masterUrl).resolve(subUrl).toString()
                
                val subReq = Request.Builder().url(resolvedSub).header("Referer", referer).build()
                val subRes = client.newCall(subReq).execute()
                val subContent = subRes.body?.string() ?: throw Exception("Sub-playlist vacía")
                subRes.close()
                baseUri = URI(resolvedSub).resolve(".")
                
                subContent.lines().forEach { subLine ->
                    if (!subLine.startsWith("#") && subLine.isNotBlank()) {
                        tsUrls.add(baseUri.resolve(subLine).toString())
                    }
                }
                break // Priority: Select highest default fallback
            }
        }
        
        if (!isMaster) {
            lines.forEach { line ->
                if (!line.startsWith("#") && line.isNotBlank()) {
                    tsUrls.add(baseUri.resolve(line).toString())
                }
            }
        }

        if (tsUrls.isEmpty()) throw Exception("No se encontraron fragmentos (.ts)")
        
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val spideryDir = File(downloadDir, "SpideryBook")
        if (!spideryDir.exists()) spideryDir.mkdirs()
        val outFile = File(spideryDir, fileName.replace(" ", "_"))
        
        updateNotification(notificationId, fileName, 0, "Descargando 0/${tsUrls.size}")
        
        var currentPercentage = 0
        var totalDownloaded = 0L
        FileOutputStream(outFile).use { fos ->
            for (i in tsUrls.indices) {
                if (!serviceJob.isActive || HlsTracker.cancelledDownloads.contains(fileName)) {
                    outFile.delete()
                    break
                }
                val tsUrl = tsUrls[i]
                
                try {
                    val tsReq = Request.Builder().url(tsUrl).header("Referer", referer).build()
                    val tsRes = client.newCall(tsReq).execute()
                    val bytes = tsRes.body?.bytes()
                    if (bytes != null) {
                        fos.write(bytes)
                        totalDownloaded += bytes.size
                    }
                    tsRes.close()
                    
                    val p = ((i + 1).toDouble() / tsUrls.size.toDouble() * 100).toInt()
                    if (p > currentPercentage) {
                        currentPercentage = p
                        val estimatedTotal = if (i > 0) (totalDownloaded / (i + 1)) * tsUrls.size else totalDownloaded * tsUrls.size
                        updateNotification(notificationId, fileName, p, "Descargando... $p%")
                        HlsTracker.activeDownloads.value += (fileName to HlsTracker.Progress(fileName, p, totalDownloaded, estimatedTotal, false, null))
                    }
                } catch(e: Exception) {
                    e.printStackTrace() // Silent retry/skip for corrupt TS chunk
                }
            }
        }
        
        HlsTracker.activeDownloads.value += (fileName to HlsTracker.Progress(fileName, 100, totalDownloaded, totalDownloaded, true, null))
        updateNotification(notificationId, fileName, 100, "Descarga Completada", true)
    }

    private fun updateNotification(id: Int, title: String, process: Int, text: String, finished: Boolean = false) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = createNotification(title, process, text)
        if (finished) {
            builder.setProgress(0, 0, false)
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
            builder.setOngoing(false)
        }
        nm.notify(id, builder.build())
    }

    private fun createNotification(title: String, process: Int, text: String = ""): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, "HLS_CHANNEL")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, process, process == 0 && text.contains("Ana"))
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("HLS_CHANNEL", "Descargas Offline", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
