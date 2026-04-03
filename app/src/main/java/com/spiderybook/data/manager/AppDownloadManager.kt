package com.spiderybook.data.manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun download(url: String, fileName: String, referer: String? = null) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Descargando archivo media...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SpideryBook/$fileName")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        // Inject crucial headers to prevent 403 Forbidden on CDNs
        if (!referer.isNullOrEmpty()) {
            request.addRequestHeader("Referer", referer)
        }
        request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")

        downloadManager.enqueue(request)
    }
}
